package org.finra.gatekeeper.services.group.service;

import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.group.interfaces.IGatekeeperGroupAuthService;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GatekeeperGroupAuthService implements IGatekeeperGroupAuthService {

    private GatekeeperLdapGroupLookupService ldapGroupLookupService;
    private final GatekeeperRdsAuthProperties rdsAuthProperties;
    @Autowired
    public GatekeeperGroupAuthService(GatekeeperLdapGroupLookupService ldapGroupLookupService, GatekeeperRdsAuthProperties rdsAuthProperties){
        this.ldapGroupLookupService = ldapGroupLookupService;
        this.rdsAuthProperties = rdsAuthProperties;

    }


    @Override
    public boolean hasGroupAuth(AccessRequestWrapper request, GatekeeperUserEntry requestor) {
        AWSRdsDatabase instance = request.getInstances().get(0);
        List <User> users = request.getUsers();
        Map<String, Set<GatekeeperADGroupEntry>> adGroups = ldapGroupLookupService.getLdapAdGroups();

        //If the AGS isn't in the more restrictive groups we can continue as normal
        if(adGroups.get(instance.getApplication())==null){
            return true;
        }

        //If the account SDLC matches an unrestricted tag
        for (char sdlc : rdsAuthProperties.getUnrestrictedSDLC()){
            if(request.getAccountSdlc().toUpperCase().toCharArray()[0] == sdlc){
                return true;
            }
        }
        //User's should only be able to request access for themselves
        if(users.size() > 1){
            return false;
        }
        if(!users.get(0).getUserId().equals(requestor.getUserId())){
            return false;
        }









        return true;
    }
}
