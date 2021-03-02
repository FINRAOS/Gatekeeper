package org.finra.gatekeeper.services.group.service;

import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.group.interfaces.IGatekeeperGroupAuthService;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GatekeeperGroupAuthService implements IGatekeeperGroupAuthService {

    private GatekeeperLdapGroupLookupService ldapGroupLookupService;
    private final GatekeeperRdsAuthProperties rdsAuthProperties;
    private final GatekeeperLdapRoleLookupService gatekeeperLdapRoleLookupService;

    private final Logger logger = LoggerFactory.getLogger(GatekeeperGroupAuthService.class);

    @Autowired
    public GatekeeperGroupAuthService(GatekeeperLdapGroupLookupService ldapGroupLookupService, GatekeeperRdsAuthProperties rdsAuthProperties, GatekeeperLdapRoleLookupService gatekeeperLdapRoleLookupService){
        this.ldapGroupLookupService = ldapGroupLookupService;
        this.rdsAuthProperties = rdsAuthProperties;
        this.gatekeeperLdapRoleLookupService = gatekeeperLdapRoleLookupService;

    }


    @Override
    public String hasGroupAuth(AccessRequestWrapper request, GatekeeperUserEntry requestor) {
        AWSRdsDatabase instance = request.getInstances().get(0);
        List <User> users = request.getUsers();
        Map<String, Set<GatekeeperADGroupEntry>> adGroups = ldapGroupLookupService.getLdapAdGroups();
        String ags = instance.getApplication();

        //If the AGS isn't in the more restrictive groups we can continue as normal
        if(adGroups.get(ags)==null){
            logger.info("AGS doesn't exist, allowing");
            return "Allowed";
        }

        //If the account SDLC matches an unrestricted tag
        for (char sdlc : rdsAuthProperties.getUnrestrictedSDLC()){
            if(request.getAccountSdlc().toUpperCase().toCharArray()[0] == sdlc){
                logger.info("Environment is unrestricted, allowing");
                return "Allowed";
            }
        }
        //User's should only be able to request access for themselves
        if(users.size() > 1 || !users.get(0).getUserId().equals(requestor.getUserId())){
            logger.info(requestor.getUserId() + " attempted to request access for a person other than themselves in a restricted AGS");
            return "User may only request access for themselves for this AGS";
        }

        List<UserRole> roles = request.getRoles();
        Set<GatekeeperADGroupEntry> adGroupRoles = adGroups.get(ags);

        //Assmeble all roles found in the request
        Set<GatekeeperADGroupEntry> requestGroups = new HashSet<>();
        for(UserRole role : roles){
            String stringRole = formatRole(role.getRole().toUpperCase());
            String sdlc = Character.toString(request.getAccountSdlc().toUpperCase().toCharArray()[0]);
            String name  = new StringBuilder("APP_GK_").append(ags.toUpperCase()).append("_").append(stringRole).append("_").append(sdlc).toString();
            GatekeeperADGroupEntry entry = new GatekeeperADGroupEntry(ags.toUpperCase(), stringRole, sdlc , name);
            requestGroups.add(entry);
        }

        //Remove roles not needed based upon the cache
        for(GatekeeperADGroupEntry entry : requestGroups){
            //If it isnt contained, it isn't needed
            if(!adGroupRoles.contains(entry)){
                requestGroups.remove(entry);
            }
        }
        Set<GatekeeperADGroupEntry> userRoles = gatekeeperLdapRoleLookupService.getLdapAdRoles(requestor.getUserId()).get(ags);
        //Check if the user has all the roles
        if(userRoles == null){
            return missingGroupsMessage(requestor, requestGroups);
        }
        boolean permission = true;
       
        Iterator<GatekeeperADGroupEntry> iter = requestGroups.iterator();
        while(iter.hasNext()){
           //If the role isn't in the users roles, they don't have permission
            GatekeeperADGroupEntry entry = iter.next();
            if(!userRoles.contains(entry)){
                permission = false;
            }
            else{
                iter.remove();
            }
        }
        if(!permission){
            return missingGroupsMessage(requestor, requestGroups);
        }
        logger.info(requestor.getUserId() + " has all permissions for the request");
        return "Allowed";
    }

    private String missingGroupsMessage(GatekeeperUserEntry requestor, Set<GatekeeperADGroupEntry> requestGroups) {
        StringBuilder response = new StringBuilder("User does not have the following groups: ");
        for(GatekeeperADGroupEntry entry : requestGroups){
            response.append(entry.getNAME()).append(" , ");
        }
        response.delete(response.length()-2, response.length() -1);
        logger.info(requestor.getUserId() + " " + response);
        return response.toString().trim();
    }

    private String formatRole(String role){
        switch (role){
            case "DATAFIX":
                return "DF";
            case "DBA_CONFIDENTIAL":
                return "DBAC";
            case "READONLY":
                return "RO";
            case "DBA":
                return "DBA";
            case "READONLY_CONFIDENTIAL":
                return "ROC";
            default:
                return role;

        }
    }
}
