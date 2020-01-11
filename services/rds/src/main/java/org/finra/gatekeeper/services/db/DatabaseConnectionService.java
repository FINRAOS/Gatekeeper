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

import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.Endpoint;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.rds.model.*;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperRDSInstance;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.services.db.factory.DatabaseConnectionFactory;
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

    private AccountInformationService accountInformationService;
    private DatabaseConnectionFactory databaseConnectionFactory;

    @Autowired
    public DatabaseConnectionService(DatabaseConnectionFactory databaseConnectionFactory, AccountInformationService accountInformationService){
        this.databaseConnectionFactory = databaseConnectionFactory;
        this.accountInformationService = accountInformationService;
    }

    public Boolean grantAccess(AWSRdsDatabase db, AWSEnvironment awsEnvironment, String user, RoleType roleType, String password, Integer timeDays) throws GKUnsupportedDBException{
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        boolean status = false;
        //initialize a map that will show the status of dbs in the request
        logger.info("Granting access to " + db + " for user {" + user + "} ");
        try{
            status = databaseConnectionFactory.getConnection(db.getEngine()).grantAccess(
                    new RdsGrantAccessQuery(account.getAlias(), account.getAccountId(), awsEnvironment.getRegion(), account.getSdlc(),
                            getAddress(db.getEndpoint(), db.getDbName()), db.getName())
                    .withUser(user)
                    .withPassword(password)
                    .withRole(roleType)
                    .withTime(timeDays));
            logger.info("User for " + user + " for role " + roleType.getRoleDescription() + " successfully created on " + db.getName() + "(" + db.getInstanceId() + ")");
        }catch(GKUnsupportedDBException e){
            logger.info("Skipping access for " + db.getName() + " as Engine " + db.getEngine() + " is not supported");
        }catch(Exception e){
            logger.info("Failed to create user on DB, exception is as follows: ");
            e.printStackTrace();
        }

        return status;
    }

    public Boolean revokeAccess(AWSRdsDatabase db, AWSEnvironment awsEnvironment, RoleType roleType, String user) throws GKUnsupportedDBException{
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        //initialize a map that will show the status of dbs in the request
        boolean status = false;
        logger.info("Revoking access on " + db.getName() + "(" + db.getEndpoint() + ")");
        try{
            status = databaseConnectionFactory.getConnection(db.getEngine())
                    .revokeAccess(new RdsRevokeAccessQuery(account.getAlias(), account.getAccountId(), awsEnvironment.getRegion(), awsEnvironment.getSdlc(),
                            getAddress(db.getEndpoint(), db.getDbName()), db.getName())
                            .withUser(user)
                            .withRole(roleType));
            logger.info("User " + user + " removed from " + db.getName() + "(" + db.getInstanceId() + ")? " + status);
        }catch(GKUnsupportedDBException e){
            logger.info("Skipping access for " + db.getName() + " as Engine " + db.getEngine() + " is not supported");
        }catch(Exception e){
            logger.info("Failed to remove user from " + db.getName() + "(" + db.getInstanceId() + "), exception is as follows: ");
            e.printStackTrace();
        }

        return status;
    }

    /**
     * Revokes a list of users from a given database
     * @param db - the database to revoke access from
     * @return the list of user ids that did not get removed
     * @throws Exception if there is any issue removing the users
     */
    @PreAuthorize("@gatekeeperRoleService.isApprover()")
    public List<String> forceRevokeAccessUsersOnDatabase(GatekeeperRDSInstance db, AWSEnvironment awsEnvironment, List<DbUser> users ) throws Exception {
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());

        List<DbUser> nonGkUsers = users.stream()
                .filter(user -> !user.getUsername().toLowerCase().startsWith("gk_"))
                .collect(Collectors.toList());

        if(!nonGkUsers.isEmpty()){
            throw new GatekeeperException("Forced removal of non-gatekeeper users is not supported. The following unsupported users are: " + nonGkUsers.toString() );
        }

        List<String> usersNotRemoved = new ArrayList<>();
        for(DbUser user:  users){
            boolean outcome = databaseConnectionFactory.getConnection(db.getEngine())
                    .revokeAccess(new RdsRevokeAccessQuery(account.getAlias(), account.getAccountId(), awsEnvironment.getRegion(), awsEnvironment.getSdlc(),
                            getAddress(db.getEndpoint(), db.getDbName()), db.getName())
                            .withUser(user.getUsername()));
            if(!outcome){
                usersNotRemoved.add(user.getUsername());
            }
        }

        return usersNotRemoved;
    }

    //UI will usually call this one
    public Map<RoleType, List<String>> getAvailableSchemasForDb(GatekeeperRDSInstance database, AWSEnvironment awsEnvironment) throws Exception {
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        return databaseConnectionFactory.getConnection(database.getEngine()).getAvailableTables(
                new RdsQuery()
                    .withAccount(account.getAlias())
                    .withAccountId(account.getAccountId())
                    .withRegion(awsEnvironment.getRegion())
                    .withSdlc(awsEnvironment.getSdlc())
                    .withAddress(getAddress(database.getEndpoint(), database.getDbName()))
                    .withDbInstanceName(database.getName()));
    }

    //This is usually called through the Activiti workflow
    public Map<RoleType, List<String>> getAvailableSchemasForDb(AWSRdsDatabase database, AWSEnvironment awsEnvironment) throws Exception {
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        return databaseConnectionFactory.getConnection(database.getEngine()).getAvailableTables(
                new RdsQuery()
                        .withAccount(account.getAlias())
                        .withAccountId(account.getAccountId())
                        .withRegion(awsEnvironment.getRegion())
                        .withSdlc(awsEnvironment.getSdlc())
                        .withAddress(getAddress(database.getEndpoint(), database.getDbName()))
                        .withDbInstanceName(database.getName()));
    }

    public String checkDb(DBInstance db, AWSEnvironment awsEnvironment) throws GKUnsupportedDBException{
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        List<String> issues = databaseConnectionFactory.getConnection(db.getEngine()).checkDb(
                new RdsQuery()
                        .withAccount(account.getAlias())
                        .withAccountId(account.getAccountId())
                        .withRegion(awsEnvironment.getRegion())
                        .withSdlc(awsEnvironment.getSdlc())
                        .withAddress(getAddress(db.getEndpoint(), db.getDBName()))
                        .withDbInstanceName(db.getDBInstanceIdentifier())
        );
        return issues.stream().collect(Collectors.joining(","));
    }

    public String checkDb(DBCluster db, AWSEnvironment awsEnvironment) throws GKUnsupportedDBException{
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        List<String> issues = databaseConnectionFactory.getConnection(db.getEngine()).checkDb(
                new RdsQuery()
                        .withAccount(account.getAlias())
                        .withAccountId(account.getAccountId())
                        .withRegion(awsEnvironment.getRegion())
                        .withSdlc(awsEnvironment.getSdlc())
                        .withAddress(getAddress(String.format("%s:%s", db.getEndpoint(), db.getPort()), db.getDatabaseName()))
                        .withDbInstanceName(db.getDBClusterIdentifier())
        );
        return issues.stream().collect(Collectors.joining(","));
    }

    public List<DbUser> getUsersForDb(GatekeeperRDSInstance db, AWSEnvironment awsEnvironment) throws Exception {
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        return databaseConnectionFactory.getConnection(db.getEngine()).getUsers(
                new RdsQuery()
                        .withAccount(account.getAlias())
                        .withAccountId(account.getAccountId())
                        .withRegion(awsEnvironment.getRegion())
                        .withSdlc(awsEnvironment.getSdlc())
                        .withAddress(getAddress(db.getEndpoint(), db.getDbName()))
                        .withDbInstanceName(db.getName()));
    }

    public List<String> getAvailableRolesForDb(DBInstance db, AWSEnvironment awsEnvironment) throws Exception {
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        return databaseConnectionFactory.getConnection(db.getEngine()).getAvailableRoles( new RdsQuery()
                .withAccount(account.getAlias())
                .withAccountId(account.getAccountId())
                .withRegion(awsEnvironment.getRegion())
                .withSdlc(awsEnvironment.getSdlc())
                .withAddress(getAddress(db.getEndpoint(), db.getDBName()))
                .withDbInstanceName(db.getDBInstanceIdentifier()));
    }

    public List<String> getAvailableRolesForDb(DBCluster db, AWSEnvironment awsEnvironment) throws Exception {
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        return databaseConnectionFactory.getConnection(db.getEngine()).getAvailableRoles( new RdsQuery()
                .withAccount(account.getAlias())
                .withAccountId(account.getAccountId())
                .withRegion(awsEnvironment.getRegion())
                .withSdlc(awsEnvironment.getSdlc())
                .withAddress(getAddress(String.format("%s:%s", db.getEndpoint(), db.getPort()), db.getDatabaseName()))
                .withDbInstanceName(db.getDBClusterIdentifier()));
    }

    /**
     * Check to see if the users in the request exist in the databases and have some kind of dependency constraint that could cause
     * gatekeeper to fail
     * @return
     */
    public List<String> checkUsersAndDbs(List<UserRole> roles, List<User> users, AWSRdsDatabase db, AWSEnvironment awsEnvironment) throws Exception{
        Account account = accountInformationService.getAccountByAlias(awsEnvironment.getAccount());
        List<String> userWithRoles = new ArrayList<>();
        roles.forEach(role -> {
            users.forEach(user -> {
                userWithRoles.add(user.getUserId()+"_"+RoleType.valueOf(role.getRole().toUpperCase()).getShortSuffix());
            });
        });
        List<String> outcome = new ArrayList<>();
        try {
            outcome = databaseConnectionFactory.getConnection(db.getEngine())
                    .checkIfUsersHasTables(
                            new RdsCheckUsersTableQuery(account.getAlias(), account.getAccountId(), awsEnvironment.getRegion(), awsEnvironment.getSdlc(),
                                    getAddress(db.getEndpoint(), db.getDbName()), db.getName())
                                        .withUsers(userWithRoles));
        }catch(Exception ex){
            logger.error("Failed to check users on database: ", ex);
        }
        return outcome;
    }

    private String getAddress(String endpoint, String dbName){
        return endpoint + "/" + dbName;
    }

    /*
     * helper method for the checkDB and getAvailableRolesForDB methods
     */
    private String getAddress(Endpoint endpoint, String dbName){
        return getAddress(endpoint.getAddress(), endpoint.getPort(), dbName);
    }

    private String getAddress(String endpoint, Integer port, String dbName){
        return String.format("%s:%s/%s", endpoint, port, dbName);
    }

}
