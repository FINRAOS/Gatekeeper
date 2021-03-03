package org.finra.gatekeeper.controllers;

import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;
import org.finra.gatekeeper.services.group.service.GatekeeperLdapGroupLookupService;
import org.finra.gatekeeper.services.group.service.GatekeeperLdapRoleLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
public class GroupLookupController {

    private static final Logger logger = LoggerFactory.getLogger(org.finra.gatekeeper.common.controllers.LdapLookupController.class);

    private final GatekeeperLdapGroupLookupService gatekeeperLdapGroupLookupService;
    private final GatekeeperLdapRoleLookupService gatekeeperLdapRoleLookupService;


    @Autowired
    public GroupLookupController(GatekeeperLdapGroupLookupService gatekeeperLdapGroupLookupService, GatekeeperLdapRoleLookupService gatekeeperLdapRoleLookupService){
        this.gatekeeperLdapGroupLookupService = gatekeeperLdapGroupLookupService;
        this.gatekeeperLdapRoleLookupService = gatekeeperLdapRoleLookupService;
    }

    @RequestMapping(value = "/loadAD", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Set<GatekeeperADGroupEntry>> loadAD() {
        return gatekeeperLdapGroupLookupService.getLdapAdGroups();

    }
    @RequestMapping(value = "/loadUser", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Set<GatekeeperADGroupEntry>> loadUser() {
        return gatekeeperLdapRoleLookupService.getLdapAdRoles();
    }

}
