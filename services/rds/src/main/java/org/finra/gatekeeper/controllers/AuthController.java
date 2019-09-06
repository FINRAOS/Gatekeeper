package org.finra.gatekeeper.controllers;

import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperApprovalProperties;
import org.finra.gatekeeper.configuration.GatekeeperOverrideProperties;
import org.finra.gatekeeper.controllers.wrappers.GetRoleResponseWrapper;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Authorization Controller for Gatekeeper RDS
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final GatekeeperRoleService gatekeeperRoleService;
    private final GatekeeperApprovalProperties gatekeeperApprovalProperties;
    private final GatekeeperOverrideProperties gatekeeperOverrideProperties;

    @Autowired
    public AuthController(GatekeeperRoleService gatekeeperRoleService, GatekeeperApprovalProperties gatekeeperApprovalProperties, GatekeeperOverrideProperties gatekeeperOverrideProperties) {
        this.gatekeeperRoleService = gatekeeperRoleService;
        this.gatekeeperApprovalProperties = gatekeeperApprovalProperties;
        this.gatekeeperOverrideProperties = gatekeeperOverrideProperties;

    }

    @RequestMapping(value="/getRole", method= RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    public GetRoleResponseWrapper getRole(){
        GetRoleResponseWrapper result = new GetRoleResponseWrapper();
        GatekeeperUserEntry user = gatekeeperRoleService.getUserProfile();
        logger.info("Getting user " + user.getUserId() + "'s memberships and permissions.");
        result.setUserId(user.getUserId());
        result.setName(user.getName());
        result.setEmail(user.getEmail());
        result.setRoleMemberships(gatekeeperRoleService.getRoleMemberships());
        logger.info("Retrieving approval policy for user: " + user.getUserId());
        result.setApprovalThreshold(gatekeeperApprovalProperties.getApprovalPolicy(result.getRoleMemberships()));
        logger.info("User " + user.getUserId() + "'s approval thresholds: " + result.getApprovalThreshold());
        result.setMaxDays(gatekeeperOverrideProperties.getMaxDays());
        logger.info("Retrieving override policy for user: " + user.getUserId());
        result.setOverridePolicy(gatekeeperOverrideProperties.getOverrides(result.getRoleMemberships()).getOverridePolicy());
        logger.info("User " + user.getUserId() + "'s override policy: " + result.getOverridePolicy());
        result.setApprover(gatekeeperRoleService.isApprover());
        if(result.getApprover()) {
            logger.info("User " + user.getUserId() + " is an approver.");
        } else {
            logger.info("User " + user.getUserId() + " is not an approver.");
        }
        return result;
    }
}
