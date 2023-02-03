/*
 * Copyright 2022. Gatekeeper Contributors
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

package org.finra.gatekeeper.services.db.connections;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.interfaces.DBConnection;
import org.finra.gatekeeper.rds.interfaces.GKUserCredentialsProvider;
import org.finra.gatekeeper.rds.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.*;

/**
 * Interface for dealing with Postgres RDS Connections
 */
@Component
public class DocumentDBConnection implements DBConnection {

    private final Logger logger = LoggerFactory.getLogger(DocumentDBConnection.class);
    private final GKUserCredentialsProvider gkUserCredentialsProvider;

    private final String gkUserName;
    private final Boolean ssl;
    private final String replicaSet;
    private final String readPreference;
    private final Boolean retryWrites;

    @Autowired
    public DocumentDBConnection(GatekeeperProperties gatekeeperProperties,
                                @Qualifier("credentialsProvider") GKUserCredentialsProvider gkUserCredentialsProvider){
        this.gkUserCredentialsProvider = gkUserCredentialsProvider;
        GatekeeperProperties.GatekeeperDbProperties db = gatekeeperProperties.getDb();
        GatekeeperProperties.GatekeeperDbProperties.DocumentDbProperties documentdb = db.getDocumentdb();
        this.gkUserName = db.getGkUser();
        this.ssl = documentdb.getSsl();
        this.replicaSet = documentdb.getReplicaSet();
        this.readPreference = documentdb.getReadPreference();
        this.retryWrites = documentdb.getRetryWrites();
    }

    public boolean grantAccess(RdsGrantAccessQuery rdsGrantAccessQuery) throws MongoException {
        String address = rdsGrantAccessQuery.getAddress();
        String user = rdsGrantAccessQuery.getUser();
        RoleType role = rdsGrantAccessQuery.getRole();
        String password = rdsGrantAccessQuery.getPassword();
        MongoClient client = null;

        try{
            client = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsGrantAccessQuery));
            String userWithSuffix = user + "_" + role.getShortSuffix();
            //Try to revoke the user
            boolean revoked = true;
            if(userExists(client, userWithSuffix)) {
                logger.info("User " + userWithSuffix + " already exists, try to remove the user.");
                try {
                    // try to delete the user if they already are present
                    revoked = revokeUser(client, userWithSuffix);
                } catch (Exception ex) {
                    logger.error("Could not remove the existing user from the database. Falling back by trying to rotate the existing user's password", ex);
                    revoked = false;
                }
            }
            if(revoked) {
                // Update the gk roles on the DB
                createUser(client, address, userWithSuffix, password, role);
            }else{
                // Rotate the password and expiration time for te existing user.
                updateUser(client, address, userWithSuffix, password, role);
            }
            return true;
        }catch(Exception ex){
            logger.error("An exception was thrown while trying to grant access to user " + user + "_" + role.getShortSuffix() + " on address " + address , ex);
            return false;
        }finally{
            if(client != null) {
                client.close();
            }
        }
    }

    public boolean revokeAccess(RdsRevokeAccessQuery rdsRevokeAccessQuery) throws MongoException{
        String address = rdsRevokeAccessQuery.getAddress();
        String user = rdsRevokeAccessQuery.getUser();
        RoleType role = rdsRevokeAccessQuery.getRole();

        MongoClient client = null;
        try {
            client = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsRevokeAccessQuery));
            logger.info("Removing " + user + " from " + address + " if they exist.");
            if(role != null) {
                //if roles is provided revoke the user with the suffix (from activiti)
                revokeUser(client, user + "_" + role.getShortSuffix());
            }else{
                //if roles is not provided just revoke the user (forced removal)
                revokeUser(client, user);
            }
            return true;

        }catch(Exception ex){
            String username = role == null ? user : user + "_" + role.getShortSuffix();
            logger.error("An exception was thrown while trying to revoke user " + username + " from address " + address, ex);
            return false;
        } finally {
            if(client != null) {
                client.close();
            }
        }
    }

    public Map<RoleType, List<String>> getAvailableTables(RdsQuery rdsQuery) throws MongoException {
        String address = rdsQuery.getAddress();
        Map<RoleType, List<String>> results = new HashMap<>();

        logger.info("Getting available schema information for " + address);
        try (MongoClient client = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery))) {
            ArrayList<Document> jsonRoles = (ArrayList<Document>) client.getDatabase("admin").runCommand(new Document("rolesInfo", 1)).get("roles");
            Map<String, List<Document>> roles = new HashMap<>();
            for (Document role : jsonRoles){
                roles.put(role.get("role").toString(),(List) role.get("roles"));
            }
            for (RoleType roleType : RoleType.values()) {
                List<String> schemas = new ArrayList<>();
                for(Document role : roles.get(roleType.getDbRole())){
                    schemas.add(role.get("db").toString());
                }
                results.put(roleType, !schemas.isEmpty() ? schemas : Collections.singletonList("No Schemas are available for role " + roleType.getDbRole() + " at this time."));
            }
            logger.info("Retrieved available schema information for database " + address);
        } catch (Exception ex) {
            logger.error("Could not retrieve available role information for database " + address, ex);
        }
        return results;
    }

    public List<String> checkDb(RdsQuery rdsQuery) throws GKUnsupportedDBException {
        String address = rdsQuery.getAddress();
        List<String> issues = new ArrayList<>();
        List<String> gkRoles = new ArrayList<>();
        gkRoles.addAll(Arrays.asList("gk_datafix", "gk_readonly", "gk_dba"));
        MongoClient client = null;
        try{
            logger.info("Checking the gatekeeper setup for " + address);
            client = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
            MongoDatabase adminDB = client.getDatabase("admin");
            Document getRolesCommand = adminDB.runCommand(new Document("rolesInfo", 1));
            ArrayList<Document> roles = (ArrayList<Document>) getRolesCommand.get("roles");
            List<String> roleCheckResult = new ArrayList<>();
            for (Document role : roles){
                roleCheckResult.add(role.get("role").toString());
            }
            gkRoles.removeAll(roleCheckResult);
            if(!gkRoles.isEmpty()) {
                issues.add("missing the following roles: " + gkRoles);
            }
            Document getGatekeeperUserCommand = adminDB.runCommand(new Document("usersInfo", 1));
            ArrayList<Document> users = (ArrayList<Document>) getGatekeeperUserCommand.get("users");
            Boolean createRolePermCheckResult = false;
            for(Document user: users){
                if(user.get("_id").equals(gkUserName)){
                    ArrayList<Document> userRoles = (ArrayList<Document>) user.get("roles");
                    for (Document role: userRoles){
                        if(role.get("db").equals("admin") && role.get("role").equals("root")){
                            createRolePermCheckResult = true;
                        }
                    }
                }
            }
            if(!createRolePermCheckResult){
                issues.add("gatekeeper user missing root role in admin db");
            }
            client.close();

        } catch(MongoException ex){
            logger.error("Failed to connect to DB", ex);
            logger.error(ex.getMessage());
            logger.error(String.valueOf(ex.getMessage().contains("authenticating")));
            if(ex.getMessage().contains("authenticating")) {
                issues.add("Password authentication failed for gatekeeper user");
            }else{
                issues.add("Unable to connect to DB (" + ex.getCause().getMessage() + ")");
            }
        }

        return issues;

    }

    /**
     * Check to see if this user is the owner of any tables on the DB
     *
     * @param rdsCheckUsersTableQuery - the query details for the db
     * @return List of String - List of users that still own tables
     *
     * @throws SQLException - if there's an issue executing the query on the database
     */
    public List<String> checkIfUsersHasTables(RdsCheckUsersTableQuery rdsCheckUsersTableQuery) throws MongoException{
        return Collections.EMPTY_LIST;
    }

    public List<DbUser> getUsers(RdsQuery rdsQuery) throws MongoException{
        String address = rdsQuery.getAddress();
        MongoClient client = null;
        List<DbUser> results = new ArrayList<>();
        logger.info("Getting available schema information for " + address);
        try {
            client = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
            Document getUsersCommand = client.getDatabase("admin").runCommand(new Document("usersInfo", 1));
            ArrayList<Document> users = (ArrayList<Document>) getUsersCommand.get("users");
            for(Document user: users){
                ArrayList<Document> jsonRoles = (ArrayList<Document>) user.get("roles");
                List<String> roles = new ArrayList<>();
                for (Document role: jsonRoles){
                    roles.add(role.get("role").toString());
                 }
                results.add(new DbUser(user.get("_id").toString(), roles));
            }
            logger.info("Retrieved users for database " + address);
        } catch (Exception ex) {
            logger.error("Could not retrieve list of users for database " + address, ex);
            results = Collections.emptyList();
        }finally{
            if(client != null) {
                client.close();
            }
        }
        return results;
    }

    public List<String> getAvailableRoles(RdsQuery rdsQuery) throws MongoException{
        String address = rdsQuery.getAddress();
        MongoClient client = null;
        List<String> results = new ArrayList<>();
        logger.info("Getting available roles for " + address);
        try {
            client = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
            Document getRolesCommand = client.getDatabase("admin").runCommand(new Document("rolesInfo", 1));
            ArrayList<Document> roles = (ArrayList<Document>) getRolesCommand.get("roles");
            for (Document role : roles){
                if(role.get("role").toString().startsWith("gk_"))
                results.add(role.get("role").toString());
            }
        } catch (Exception ex) {
            logger.error("Could not retrieve list of roles for database " + address, ex);
            throw ex;
        }finally{
            if(client != null) {
                client.close();
            }
        }
        return results;
    }

    private MongoClient connect(String url, String gkUserPassword){
        String connectionTemplate = "mongodb://%s:%s@%s?ssl=%s&replicaSet=%s&readPreference=%s&retryWrites=%s";
        String connectionString = String.format(connectionTemplate, gkUserName, gkUserPassword, url, ssl, replicaSet, readPreference, retryWrites.toString());
        MongoClient mongoClient = MongoClients.create(connectionString);
        return mongoClient;
    }

    private void updateUser(MongoClient client, String address, String user, String password, RoleType role) throws MongoException{
        logger.info("Updating the password for " + user + " on " + address + " with role " + role.getDbRole());
        final BasicDBObject updateUserCommand = new BasicDBObject("updateUser", user)
                .append("pwd", password)
                .append("roles",
                        Collections.singletonList(new BasicDBObject("role", role.getDbRole()).append("db", "admin"))
                );
        client.getDatabase("admin").runCommand(updateUserCommand);
        logger.info("Done Updating user " + user + " on " + address + " with role " + role.getDbRole());
    }

    private void createUser(MongoClient client, String address, String user, String password, RoleType role) throws MongoException{
        logger.info("Creating user " + user + " on " + address + " with role " + role.getDbRole());
        final BasicDBObject createUserCommand = new BasicDBObject("createUser", user)
                .append("pwd", password)
                .append("roles",
                    Collections.singletonList(new BasicDBObject("role", role.getDbRole()).append("db", "admin"))
                );
        client.getDatabase("admin").runCommand(createUserCommand);
        logger.info("Done Creating user " + user + " on " + address + " with role " + role.getDbRole());
    }

    private boolean userExists(MongoClient client, String user){
        logger.info("Checking to see if user " + user + " exists");
        Document getUsersCommand = client.getDatabase("admin").runCommand(new Document("usersInfo", 1));
        ArrayList<Document> users = (ArrayList<Document>) getUsersCommand.get("users");
        for(Document userJSON: users){
            if(userJSON.get("_id").equals(user)){
                 return true;
            }
        }
        return false;
    }
    private boolean revokeUser(MongoClient client, String user){
        final BasicDBObject dropUserCommand = new BasicDBObject("dropUser", user);
        client.getDatabase("admin").runCommand(dropUserCommand);
        return !userExists(client, user);
    }
}
