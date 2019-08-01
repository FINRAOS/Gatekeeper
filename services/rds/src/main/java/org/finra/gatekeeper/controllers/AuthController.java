package org.finra.gatekeeper.controllers;

import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperUserProfile;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperApprovalProperties;
import org.finra.gatekeeper.configuration.GatekeeperOverrideProperties;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


/**
 * Authorization Controller for Gatekeeper RDS
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GatekeeperRoleService gatekeeperRoleService;
    private final GatekeeperApprovalProperties approvalThreshold;
    private final GatekeeperOverrideProperties gatekeeperOverrideProperties;

    @Autowired
    public AuthController(GatekeeperRoleService gatekeeperRoleService, GatekeeperApprovalProperties gatekeeperApprovalProperties, GatekeeperOverrideProperties gatekeeperOverrideProperties) {
        this.gatekeeperRoleService = gatekeeperRoleService;
        this.approvalThreshold = gatekeeperApprovalProperties;
        this.gatekeeperOverrideProperties = gatekeeperOverrideProperties;

    }

    @RequestMapping(value="/getRole", method= RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getRole(){
        Map<String, Object> result = new HashMap<>();
        GatekeeperUserEntry user = gatekeeperRoleService.getUserProfile();
        result.put("userId", user.getUserId());
        result.put("name", user.getName());
        GatekeeperRdsRole role = gatekeeperRoleService.getRole();
        result.put("email", user.getEmail());
        result.put("approvalThreshold", approvalThreshold.getApprovalPolicy(role));
        result.put("maxDays", gatekeeperOverrideProperties.getMaxDays());
        result.put("overridePolicy", gatekeeperOverrideProperties.getOverridePolicy(role));
        result.put("role", role);
        switch(role){
            case APPROVER:
            case DBA:
                result.put("memberships", gatekeeperRoleService.getDbaMemberships());
                return result;
            default:{
                result.put("memberships", gatekeeperRoleService.getDevMemberships());
                return result;
            }
        }
    }
}
