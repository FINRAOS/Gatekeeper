/*
 * Copyright 2018. Gatekeeper Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.finra.gatekeeper.controllers;

import org.finra.gatekeeper.common.services.user.auth.GatekeeperAuthorizationService;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.properties.GatekeeperApprovalProperties;
import org.finra.gatekeeper.configuration.properties.GatekeeperOverrideProperties;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller that hosts basic Auth services for gatekeeper
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final GatekeeperRoleService gatekeeperRoleService;
    private final GatekeeperApprovalProperties approvalPolicy;
    private final GatekeeperOverrideProperties overridePolicy;
    @Autowired
    public AuthController(GatekeeperRoleService gatekeeperRoleService,
                          GatekeeperApprovalProperties gatekeeperApprovalProperties,GatekeeperOverrideProperties gatekeeperOverrideProperties){
        this.gatekeeperRoleService = gatekeeperRoleService;
        this.approvalPolicy = gatekeeperApprovalProperties;
        this.overridePolicy = gatekeeperOverrideProperties;
    }

    @RequestMapping(value="/getRole", method= RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getRole(){
        Map<String, Object> result = new HashMap<>();
        GatekeeperUserEntry user = gatekeeperRoleService.getUserProfile();
        result.put("userId", user.getUserId());
        result.put("name", user.getName());
        result.put("role", gatekeeperRoleService.getRole());
        result.put("email", user.getEmail());
        result.put("approvalThreshold", approvalPolicy.getApprovalPolicy(gatekeeperRoleService.getRole()));
        result.put("maxHours", overridePolicy.getMaxHours());
        logger.info("Retrieving override policy for user: " + user.getUserId());
        result.put("overridePolicy", overridePolicy.getOverrides(gatekeeperRoleService.getRole()).getOverridePolicy());
        logger.info("User " + user.getUserId() + "'s override policy: " + result.get("overridePolicy"));

        switch(gatekeeperRoleService.getRole()){
            case APPROVER:
            case SUPPORT:
                return result;
            default:{
                result.put("memberships", gatekeeperRoleService.getMemberships());
                return result;
            }
        }
    }
}
