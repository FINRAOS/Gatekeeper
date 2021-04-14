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
 *
 */
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
import org.finra.gatekeeper.rds.model.RoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GatekeeperGroupAuthService implements IGatekeeperGroupAuthService {

    private GatekeeperLdapGroupLookupService ldapGroupLookupService;
    private final GatekeeperRdsAuthProperties rdsAuthProperties;
    private final GatekeeperRoleService gatekeeperRoleService;

    private final Logger logger = LoggerFactory.getLogger(GatekeeperGroupAuthService.class);

    @Autowired
    public GatekeeperGroupAuthService(GatekeeperLdapGroupLookupService ldapGroupLookupService, GatekeeperRdsAuthProperties rdsAuthProperties,  GatekeeperRoleService gatekeeperRoleService){
        this.ldapGroupLookupService = ldapGroupLookupService;
        this.rdsAuthProperties = rdsAuthProperties;
        this.gatekeeperRoleService = gatekeeperRoleService;

    }


    @Override
    public String hasGroupAuth(AccessRequestWrapper request, GatekeeperUserEntry requestor) {
        AWSRdsDatabase instance = request.getInstances().get(0);
        List <User> users = request.getUsers();
        Map<String, Set<GatekeeperADGroupEntry>> adGroups = ldapGroupLookupService.getLdapAdGroups();
        String application = instance.getApplication();

        //If the Application isn't in the more restrictive groups we can continue as normal
        if(adGroups.get(application)==null){
            logger.info("Application doesn't exist, allowing");
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
            logger.info(requestor.getUserId() + " attempted to request access for a person other than themselves in a restricted application in: " + application);
            return "User may only request access for themselves for this Application";
        }

        List<UserRole> roles = request.getRoles();
        Set<GatekeeperADGroupEntry> adGroupRoles = adGroups.get(application);

        //Assmeble all roles found in the request
        Set<GatekeeperADGroupEntry> requestGroups = new HashSet<>();
        for(UserRole role : roles){
            String stringRole = RoleType.valueOf(role.getRole().toUpperCase()).getShortSuffix().toUpperCase();
            String sdlc = Character.toString(request.getAccountSdlc().toUpperCase().toCharArray()[0]);
            String name  = new StringBuilder(rdsAuthProperties.getRestrictedPrefix()).append(application.toUpperCase()).append("_").append(stringRole).append("_").append(sdlc).toString();
            GatekeeperADGroupEntry entry = new GatekeeperADGroupEntry(application.toUpperCase(), stringRole, sdlc , name);
            requestGroups.add(entry);
        }

        //Remove roles not needed based upon the cache
        for(GatekeeperADGroupEntry entry : requestGroups){
            //If it isnt contained, it isn't needed
            if(!adGroupRoles.contains(entry)){
                requestGroups.remove(entry);
            }
        }
        Set<GatekeeperADGroupEntry> userRoles = gatekeeperRoleService.getRestrictedRoleMemberships().get(application);
        //Check if the user has all the roles
        if(userRoles == null){
            userRoles = new HashSet<>();
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
            return missingGroupsMessage(requestor, requestGroups, application);
        }
        logger.info(requestor.getUserId() + " has all permissions for request in application: " + application);
        return "Allowed";
    }

    private String missingGroupsMessage(GatekeeperUserEntry requestor, Set<GatekeeperADGroupEntry> requestGroups, String application) {
        StringBuilder response = new StringBuilder("User does not have the following groups: ");
        for(GatekeeperADGroupEntry entry : requestGroups){
            response.append(entry.getName()).append(" , ");
        }
        response.delete(response.length()-2, response.length() -1);
        logger.info(requestor.getUserId() + " " + response + "for request in application: " + application);
        return response.toString().trim();
    }

}
