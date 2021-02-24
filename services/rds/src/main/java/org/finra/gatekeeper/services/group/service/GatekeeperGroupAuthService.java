package org.finra.gatekeeper.services.group.service;

import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.group.interfaces.IGatekeeperGroupAuthService;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GatekeeperGroupAuthService implements IGatekeeperGroupAuthService {

    private GatekeeperLdapGroupLookupService ldapGroupLookupService;
    private final GatekeeperRdsAuthProperties rdsAuthProperties;
    private final GatekeeperLdapRoleLookupService gatekeeperLdapRoleLookupService;

    @Autowired
    public GatekeeperGroupAuthService(GatekeeperLdapGroupLookupService ldapGroupLookupService, GatekeeperRdsAuthProperties rdsAuthProperties, GatekeeperLdapRoleLookupService gatekeeperLdapRoleLookupService){
        this.ldapGroupLookupService = ldapGroupLookupService;
        this.rdsAuthProperties = rdsAuthProperties;
        this.gatekeeperLdapRoleLookupService = gatekeeperLdapRoleLookupService;

    }


    @Override
    public boolean hasGroupAuth(AccessRequestWrapper request, GatekeeperUserEntry requestor) {
        AWSRdsDatabase instance = request.getInstances().get(0);
        List <User> users = request.getUsers();
        Map<String, Set<GatekeeperADGroupEntry>> adGroups = ldapGroupLookupService.getLdapAdGroups();
        String ags = instance.getApplication();

        System.out.println(adGroups.get(ags));

        //If the AGS isn't in the more restrictive groups we can continue as normal
        if(adGroups.get(ags)==null){
            return true;
        }

        //If the account SDLC matches an unrestricted tag
        for (char sdlc : rdsAuthProperties.getUnrestrictedSDLC()){
            if(request.getAccountSdlc().toUpperCase().toCharArray()[0] == sdlc){
                return true;
            }
        }
        //User's should only be able to request access for themselves
        if(users.size() > 1 || !users.get(0).getUserId().equals(requestor.getUserId())){
            return false;
        }

        List<UserRole> roles = request.getRoles();
        Set<GatekeeperADGroupEntry> adGroupRoles = adGroups.get(ags);

        //Assmeble all roles found in the request
        Set<GatekeeperADGroupEntry> requestGroups = new HashSet<>();
        for(UserRole role : roles){
            String name  = new StringBuilder("APP_GK_").append(ags.toUpperCase()).append("_").append(role.getRole().toUpperCase()).append("_").append(request.getAccountSdlc().toUpperCase()).toString();
            GatekeeperADGroupEntry entry = new GatekeeperADGroupEntry(ags.toUpperCase(), role.getRole().toUpperCase(), request.getAccountSdlc().toUpperCase(), name);
            requestGroups.add(entry);
        }
        //Remove roles not needed based upon the cache
        for(GatekeeperADGroupEntry entry : requestGroups){
            //If it isnt contained, it isn't needed
            if(!adGroupRoles.contains(entry)){
                requestGroups.remove(entry);
            }
        }

        Set<GatekeeperADGroupEntry> userRoles = gatekeeperLdapRoleLookupService.loadRoles(requestor.getUserId()).get(ags);
        //Check if the user has all the roles
        for(GatekeeperADGroupEntry entry : requestGroups){
            //If the role isn't in the users roles, they don't have permission
            if(!userRoles.contains(entry)){
                return false;
            }
        }

        return true;
    }
}
