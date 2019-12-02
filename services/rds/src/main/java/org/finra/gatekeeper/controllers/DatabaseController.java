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

package org.finra.gatekeeper.controllers;

import org.finra.gatekeeper.controllers.wrappers.RemoveUsersWrapper;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.rds.model.DbUser;
import org.finra.gatekeeper.services.aws.RdsLookupService;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperRDSInstance;
import org.finra.gatekeeper.services.db.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/db")
public class DatabaseController {

    private final DatabaseConnectionService databaseConnectionService;
    private final RdsLookupService rdsLookupService;

    @Autowired
    public DatabaseController(DatabaseConnectionService databaseConnectionService, RdsLookupService rdsLookupService){
        this.databaseConnectionService = databaseConnectionService;
        this.rdsLookupService = rdsLookupService;
    }

    @RequestMapping(value="/removeUsers", method= RequestMethod.DELETE, produces= MediaType.APPLICATION_JSON_VALUE)
    public List<DbUser> removeUsersFromDatabase(@RequestBody RemoveUsersWrapper removeUsersWrapper) throws Exception {
        AWSEnvironment awsEnvironment = new AWSEnvironment(removeUsersWrapper.getAccount(), removeUsersWrapper.getRegion());
        try {
            GatekeeperRDSInstance rdsInstance = rdsLookupService.getOneInstance(awsEnvironment, removeUsersWrapper.getInstanceId(), removeUsersWrapper.getInstanceName()).get();
            List<String> result = this.databaseConnectionService.forceRevokeAccessUsersOnDatabase( rdsInstance, removeUsersWrapper.getUsers());
            if(!result.isEmpty()){
                throw new GatekeeperException("Failed to remove the following users: " + result + ". Please verify that they do not have any dependent objects; " +
                        "If they do have dependent objects, they need to be removed.");
            }
        } catch (Exception e ) {
            throw new GatekeeperException("Error while trying to revoke the following users: " + removeUsersWrapper.getUsers()
                    + " On database " + removeUsersWrapper.getInstanceName() + " on account " + removeUsersWrapper.getAccount() + " ("
                    + removeUsersWrapper.getRegion() + ") - " + e.getMessage() , e);
        }

        return rdsLookupService.getUsersForInstance(awsEnvironment, removeUsersWrapper.getInstanceId(), removeUsersWrapper.getInstanceName());
    }
}
