package org.finra.gatekeeper.controllers;

import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperUserProfile;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperApprovalProperties;
import org.finra.gatekeeper.configuration.GatekeeperOverrideProperties;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.services.auth.model.GetRoleResponseDTO;
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
 * Authorization Controller for Gatekeeper RDS
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GatekeeperRoleService gatekeeperRoleService;
    private final GatekeeperApprovalProperties approvalThreshold;
    private final GatekeeperOverrideProperties gatekeeperOverrideProperties;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(GatekeeperRoleService gatekeeperRoleService, GatekeeperApprovalProperties gatekeeperApprovalProperties, GatekeeperOverrideProperties gatekeeperOverrideProperties) {
        this.gatekeeperRoleService = gatekeeperRoleService;
        this.approvalThreshold = gatekeeperApprovalProperties;
        this.gatekeeperOverrideProperties = gatekeeperOverrideProperties;

    }

    @RequestMapping(value="/getRole", method= RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    public GetRoleResponseDTO getRole(){
        GetRoleResponseDTO result = new GetRoleResponseDTO();
        GatekeeperUserEntry user = gatekeeperRoleService.getUserProfile();
        result.setUserId(user.getUserId());
        result.setName(user.getName());
        result.setApprover(gatekeeperRoleService.isApprover());
        result.setRoleMemberships(gatekeeperRoleService.getRoleMemberships());
        result.setEmail(user.getEmail());
        result.setApprovalThreshold(gatekeeperRoleService.getApprovalPolicy(result.getRoleMemberships()));
        logger.info("Approval threshold: " + result.getApprovalThreshold());
        result.setMaxDays(gatekeeperOverrideProperties.getMaxDays());
        result.setOverridePolicy(gatekeeperOverrideProperties.getOverridePolicy(result.getRoleMemberships(), result.isApprover()));
        logger.info("Override Policy: " + result.getOverridePolicy());
        return result;
    }
}
