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

package org.finra.gatekeeper.services.db;

import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.RoleType;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperRDSInstance;
import org.finra.gatekeeper.services.db.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.services.db.factory.DatabaseConnectionFactory;
import org.finra.gatekeeper.services.db.model.DbUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that will interact with the factory to grant users access
 */
@Component
public class DatabaseConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionService.class);

    private DatabaseConnectionFactory databaseConnectionFactory;

    @Autowired
    public DatabaseConnectionService(DatabaseConnectionFactory databaseConnectionFactory){
        this.databaseConnectionFactory = databaseConnectionFactory;
    }

    public Map<String, Boolean> grantAccess(List<AWSRdsDatabase> databases, String user, RoleType roleType, String password, Integer timeDays) throws GKUnsupportedDBException{
        //initialize a map that will show the status of dbs in the request
        Map<String, Boolean > statusMap = databases.stream().collect(
                Collectors.toMap(AWSRdsDatabase::getName, db -> Boolean.FALSE ));
        logger.info("Granting access to " + databases + " for user {" + user + "} ");
        databases.forEach(db -> {
            logger.info("Granting access on " + db.getName() + "(" + db.getEndpoint() + ")");
            try{
                statusMap.put(db.getName(), databaseConnectionFactory.getConnection(db.getEngine()).grantAccess(user, password, roleType, getAddress(db.getEndpoint(), db.getDbName()), timeDays));
                logger.info("User for " + user + " for role " + roleType.getRoleDescription() + " successfully created on " + db.getName() + "(" + db.getInstanceId() + ")");
            }catch(GKUnsupportedDBException e){
                logger.info("Skipping access for " + db.getName() + " as Engine " + db.getEngine() + " is not supported");
            }catch(Exception e){
                logger.info("Failed to create user on DB, exception is as follows: ");
                e.printStackTrace();
            }
        });

        return statusMap;
    }

    public Map<String, Boolean> revokeAccess(List<AWSRdsDatabase> databases, RoleType roleType, String user) throws GKUnsupportedDBException{
        //initialize a map that will show the status of dbs in the request
        Map<String, Boolean > statusMap = databases.stream().collect(
                Collectors.toMap(AWSRdsDatabase::getName, db -> Boolean.FALSE ));
        logger.info("Revoking access to " + databases + " for user {" + user + "} ");
        databases.forEach(db -> {
            logger.info("Revoking access on " + db.getName() + "(" + db.getEndpoint() + ")");
            try{
                statusMap.put(db.getName(), databaseConnectionFactory.getConnection(db.getEngine()).revokeAccess(user, roleType, getAddress(db.getEndpoint(), db.getDbName())));
                logger.info("User " + user + " successfully removed from " + db.getName() + "(" + db.getInstanceId() + ")");
            }catch(GKUnsupportedDBException e){
                logger.info("Skipping access for " + db.getName() + " as Engine " + db.getEngine() + " is not supported");
            }catch(Exception e){
                logger.info("Failed to remove user from " + db.getName() + "(" + db.getInstanceId() + "), exception is as follows: ");
                e.printStackTrace();
            }
        });

        return statusMap;
    }

    /**
     * Revokes a list of users from a given database
     * @param database - the database to revoke access from
     * @return
     * @throws Exception
     */
    @PreAuthorize("@gatekeeperRoleService.isApprover()")
    public List<String> forceRevokeAccessUsersOnDatabase(GatekeeperRDSInstance database, List<DbUser> users ) throws Exception {
        List<DbUser> nonGkUsers = users.stream()
                .filter(user -> !user.getUsername().startsWith("gk_"))
                .collect(Collectors.toList());

        if(!nonGkUsers.isEmpty()){
            throw new GatekeeperException("Forced removal of non-gatekeeper users is not supported. The following unsupported users are: " + nonGkUsers.toString() );
        }

        List<String> usersRemoved = new ArrayList<>();
        for(DbUser user:  users){
            boolean outcome = databaseConnectionFactory.getConnection(database.getEngine()).revokeAccess(user.getUsername(), null, getAddress(database.getEndpoint(), database.getDbName()));
            if(!outcome){
                usersRemoved.add(user.getUsername());
            }
        }

        return usersRemoved;
    }

    //UI will usually call this one
    public Map<RoleType, List<String>> getAvailableSchemasForDb(GatekeeperRDSInstance database) throws Exception {
        return databaseConnectionFactory.getConnection(database.getEngine()).getAvailableTables(getAddress(database.getEndpoint(), database.getDbName()));
    }

    //This is usually called through the Activiti workflow
    public Map<RoleType, List<String>> getAvailableSchemasForDb(AWSRdsDatabase database) throws Exception {
        return databaseConnectionFactory.getConnection(database.getEngine()).getAvailableTables(getAddress(database.getEndpoint(), database.getDbName()));
    }

    public String checkDb(String engine, String address) throws GKUnsupportedDBException{
        List<String> issues = databaseConnectionFactory.getConnection(engine).checkDb(address);
        return issues.stream().collect(Collectors.joining(","));
    }

    public List<DbUser> getUsersForDb(GatekeeperRDSInstance database) throws Exception {
        return databaseConnectionFactory.getConnection(database.getEngine()).getUsers(getAddress(database.getEndpoint(), database.getDbName()));
    }

    /**
     * Check to see if the users in the request exist in the databases and have some kind of dependency constraint that could cause
     * gatekeeper to fail
     * @return
     */
    public Map<String, List<String>> checkUsersAndDbs(List<UserRole> roles, List<User> users, List<AWSRdsDatabase> databases) throws Exception{
        List<String> userWithRoles = new ArrayList<>();
        roles.forEach(role -> {
            users.forEach(user -> {
                userWithRoles.add(user.getUserId()+"_"+RoleType.valueOf(role.getRole().toUpperCase()).getShortSuffix());
            });
        });
        Map<String, List<String>> badActors = new HashMap<>();
        databases.forEach(db ->{
            try {
                List<String> outcome = databaseConnectionFactory.getConnection(db.getEngine()).checkIfUsersHasTables(getAddress(db.getEndpoint(), db.getDbName()), userWithRoles);
                if(!outcome.isEmpty()){
                    badActors.put(db.getName(), outcome);
                }
            }catch(Exception ex){
                logger.error("Failed to check users on database: ", ex);
            }
        });
        return badActors;
    }

    private String getAddress(String endpoint, String dbName){
        return endpoint + "/" + dbName;
    }
}
