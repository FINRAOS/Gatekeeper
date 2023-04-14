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

import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.interfaces.DBConnection;
import org.finra.gatekeeper.rds.interfaces.GKUserCredentialsProvider;
import org.finra.gatekeeper.rds.model.*;
import org.finra.gatekeeper.services.aws.RdsIamAuthService;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Interface for dealing with Postgres RDS Connections
 */
@Component
public class PostgresDBConnection implements DBConnection {

    private final Logger logger = LoggerFactory.getLogger(PostgresDBConnection.class);
    private final GKUserCredentialsProvider gkUserCredentialsProvider;
    private RdsIamAuthService rdsIamAuthService;
    private final String EXPIRATION_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
    private final String getSchemas = "SELECT distinct table_schema||'.'||table_name FROM information_schema.role_table_grants "
    + "where grantee = ? order by table_schema||'.'||table_name";
    private final String getUsers = "select rolname from pg_roles where rolcanlogin = true";

    private final String gkUserName;
    private final Boolean ssl;
    private final String sslMode;
    private final String sslCert;
    private final Integer connectTimeout;

    @Autowired
    public PostgresDBConnection(GatekeeperProperties gatekeeperProperties,
                                RdsIamAuthService rdsIamAuthService,
                                @Qualifier("credentialsProvider") GKUserCredentialsProvider gkUserCredentialsProvider){
        this.gkUserCredentialsProvider = gkUserCredentialsProvider;
        this.rdsIamAuthService = rdsIamAuthService;
        GatekeeperProperties.GatekeeperDbProperties db = gatekeeperProperties.getDb();
        GatekeeperProperties.GatekeeperDbProperties.PostgresDbProperties postgres = db.getPostgres();
        this.gkUserName = db.getGkUser();
        this.ssl = postgres.getSsl();
        this.sslMode = postgres.getSslMode();
        this.sslCert = postgres.getSslCert();
        this.connectTimeout = postgres.getConnectTimeout();
    }

    public boolean grantAccess(RdsGrantAccessQuery rdsGrantAccessQuery) throws SQLException {
        String address = rdsGrantAccessQuery.getAddress();
        String user = rdsGrantAccessQuery.getUser();
        RoleType role = rdsGrantAccessQuery.getRole();
        String password = rdsGrantAccessQuery.getPassword();
        int length = rdsGrantAccessQuery.getTime();

        PGPoolingDataSource dataSource = null;

        try {
            dataSource = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsGrantAccessQuery), rdsGrantAccessQuery);
            JdbcTemplate conn = new JdbcTemplate(dataSource);

            String expirationTime = LocalDateTime.now().plusDays(length).format(DateTimeFormatter.ofPattern(EXPIRATION_TIMESTAMP));
            String userWithSuffix = user + "_" + role.getShortSuffix();
            //Try to revoke the user
            boolean revoked = true;

            // If the user already exists then we'll need to try to remove it, if they don't we'll just create the user.
            if(userExists(conn, userWithSuffix)) {
                logger.info("User " + userWithSuffix + " already exists, try to remove the user.");
                try {
                    // try to delete the user if they already are present
                    revoked = revokeUser(conn, userWithSuffix);
                } catch (Exception ex) {
                    logger.error("Could not remove the existing user from the database. Falling back by trying to rotate the existing user's password", ex);
                    revoked = false;
                }
            }
            if(revoked) {
                // Update the gk roles on the DB
                createUser(conn, address, userWithSuffix, password, role, expirationTime);
            }else{
                // Rotate the password and expiration time for te existing user.
                updateUser(conn, address, userWithSuffix, password, role, expirationTime);
            }
            return true;
        }catch(Exception ex){
            logger.error("An exception was thrown while trying to grant access to user " + user + "_" + role.getShortSuffix() + " on address " + address , ex);
            return false;
        }finally{
            if(dataSource != null) {
                dataSource.close();
            }
        }
    }

    public boolean revokeAccess(RdsRevokeAccessQuery rdsRevokeAccessQuery) throws SQLException{
        String address = rdsRevokeAccessQuery.getAddress();
        String user = rdsRevokeAccessQuery.getUser();
        RoleType role = rdsRevokeAccessQuery.getRole();

        PGPoolingDataSource dataSource = null;
        try {
            dataSource = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsRevokeAccessQuery), rdsRevokeAccessQuery);
            JdbcTemplate conn = new JdbcTemplate(dataSource);
            logger.info("Removing " + user + " from " + address + " if they exist.");
            if(role != null) {
                //if roles is provided revoke the user with the suffix (from activiti)
                revokeUser(conn, user + "_" + role.getShortSuffix());
            }else{
                //if roles is not provided just revoke the user (forced removal)
                revokeUser(conn, user);
            }
            return true;

        }catch(Exception ex){
            String username = role == null ? user : user + "_" + role.getShortSuffix();
            logger.error("An exception was thrown while trying to revoke user " + username + " from address " + address, ex);
            return false;
        } finally {
            if(dataSource != null) {
                dataSource.close();
            }
        }
    }

    public Map<RoleType, List<String>> getAvailableTables(RdsQuery rdsQuery) throws SQLException{
        String address = rdsQuery.getAddress();
        Map<RoleType, List<String>> results = new HashMap<>();
        PGPoolingDataSource dataSource = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery), rdsQuery);
        JdbcTemplate conn = new JdbcTemplate(dataSource);

        logger.info("Getting available schema information for " + address);
        for(RoleType roleType : RoleType.values()) {
            try {
                List<String> schemas = conn.queryForList(getSchemas, new Object[]{roleType.getDbRole()}, String.class);
                results.put(roleType, !schemas.isEmpty() ? schemas : Collections.singletonList("No Schemas are available for role " + roleType.getDbRole() + " at this time."));
                logger.info("Retrieved available schema information for database " + address + " for role " + roleType.getDbRole());
            } catch (Exception ex) {
                logger.error("Could not retrieve available role information for database " + address + " for role " +  roleType.getDbRole(), ex);
                results.put(roleType, Collections.singletonList("Unable to get available schemas for role " + roleType.getDbRole()));
            }
        }
        dataSource.close();
        return results;
    }

    public List<String> checkDb(RdsQuery rdsQuery) throws GKUnsupportedDBException {
        String address = rdsQuery.getAddress();

        String gkUserCreateRoleCheck = "select rolcreaterole from pg_roles where rolname = 'gatekeeper'";
        String gkRoleCheck = "select rolname from pg_roles where rolname in ('gk_datafix','gk_dba','gk_readonly')";

        List<String> issues = new ArrayList<>();
        List<String> gkRoles = new ArrayList<>();
        gkRoles.addAll(Arrays.asList("gk_datafix", "gk_readonly", "gk_dba"));
        PGPoolingDataSource dataSource = null;

        try{
            logger.info("Checking the gatekeeper setup for " + address);
            dataSource = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery), rdsQuery);
            JdbcTemplate conn = new JdbcTemplate(dataSource);
            Boolean createRolePermCheckResult = conn.queryForObject(gkUserCreateRoleCheck, Boolean.class);
            List<String> roleCheckResult = conn.queryForList(gkRoleCheck, String.class);

            if(!createRolePermCheckResult){
                issues.add("gatekeeper user missing createrole");
            }
            gkRoles.removeAll(roleCheckResult);
            if(!gkRoles.isEmpty()) {
                issues.add("missing the following roles: " + gkRoles);
            }
        }catch(SQLException e){
            logger.error("Error running check query", e);
        } catch(CannotGetJdbcConnectionException ex){
            logger.error("Failed to connect to DB", ex);
            if(ex.getMessage().contains("password")) {
                issues.add("Password authentication failed for gatekeeper user");
            }else{
                issues.add("Unable to connect to DB (" + ex.getCause().getMessage() + ")");
            }
        }finally{
            if(dataSource != null) {
                dataSource.close();
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
    public List<String> checkIfUsersHasTables(RdsCheckUsersTableQuery rdsCheckUsersTableQuery) throws SQLException{
        String address = rdsCheckUsersTableQuery.getAddress();
        List<String> users = rdsCheckUsersTableQuery.getUsers();
        PGPoolingDataSource dataSource = null;
        try {
            dataSource = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsCheckUsersTableQuery), rdsCheckUsersTableQuery);
            JdbcTemplate conn = new JdbcTemplate(dataSource);
            StringBuilder sb = new StringBuilder();
            users.forEach(user -> {
                sb.append("?,");
            });

            String query = "SELECT distinct tableowner FROM pg_tables t where t.tableowner in ("+ sb.deleteCharAt(sb.length() - 1).toString() + ")";

            List<String> outcome = conn.queryForList(query, users.toArray(), String.class);

            return outcome;

        }catch(SQLException ex){
            logger.error("An Error occured while checking to see if the user owns any tables on the database", ex);
            return users;
        }finally {
            if(dataSource != null) {
                dataSource.close();
            }
        }
    }

    public List<DbUser> getUsers(RdsQuery rdsQuery) throws SQLException{
        String address = rdsQuery.getAddress();
        PGPoolingDataSource dataSource = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery), rdsQuery);
        JdbcTemplate conn = new JdbcTemplate(dataSource);
        List<DbUser> results;
        logger.info("Getting available schema information for " + address);
        try {
            results = conn.query(getUsers, new PostgresDbUserMapper());
            logger.info("Retrieved users for database " + address);
        } catch (Exception ex) {
            logger.error("Could not retrieve list of users for database " + address, ex);
            results = Collections.emptyList();
        }
        dataSource.close();
        return results;
    }

    public List<String> getAvailableRoles(RdsQuery rdsQuery) throws SQLException{
        String address = rdsQuery.getAddress();

        PGPoolingDataSource dataSource = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery), rdsQuery);
        JdbcTemplate conn = new JdbcTemplate(dataSource);
        List<String> results;
        logger.info("Getting available roles for " + address);
        try {
            results = conn.queryForList("select rolname from pg_catalog.pg_roles where rolname like 'gk_%' and rolcanlogin = false", String.class);
        } catch (Exception ex) {
            logger.error("Could not retrieve list of roles for database " + address, ex);
            throw ex;
        }finally {
            dataSource.close();
        }
        return results;
    }

    private PGPoolingDataSource connect(String url, String gkUserPassword, RdsQuery rdsQuery) throws SQLException {
        String dbUrl = url.split("/")[0];
        logger.info("Getting connection for " + dbUrl);
        logger.info("Creating Datasource connection for " + dbUrl);
        String pgUrl = dbUrl + "/postgres"; // url with postgres instead of whatever was on the AWS console
        if(rdsQuery.isRdsIAMAuth()){
            String address= dbUrl.split(":")[0];
            String port = dbUrl.split(":")[1];
            AWSEnvironment environment = new AWSEnvironment(rdsQuery.getAccount(), rdsQuery.getRegion(), rdsQuery.getSdlc());
            String iamToken = rdsIamAuthService.fetchIamAuthToken(environment, address, port, gkUserName);
            try {
                try {
                    return connectHelper(pgUrl, iamToken); // Try postgres first since it is a default db.
                } catch (Exception e){
                    logger.info("postgres database not present for " + dbUrl.split("/")[0] + " Attempting connection to " + url + " as fallback.");
                    return connectHelper(url, iamToken); // Fall-back if postgres isn't there
                }
            }
            catch (Exception e){
                if(e.getCause().toString().contains("FATAL: password authentication failed for user \"gatekeeper\"")){
                    logger.info("Failed to connect with IAM Auth. Attempting to connect with stored password.");
                } else {
                    throw e;
                }
            }
        }

        try {
            return connectHelper(pgUrl, gkUserPassword); // Try postgres first since it is a default db.
        } catch (Exception e){
            logger.info("postgres database not present for " + dbUrl.split("/")[0] + " Attempting connection to " + url + " as fallback.");
            return connectHelper(url, gkUserPassword); // Fall-back if postgres isn't there
        }
    }

    private PGPoolingDataSource connectHelper(String address, String gkUserPassword) {
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        String dbUrl = "jdbc:postgresql://" + address;

        dataSource.setDataSourceName(address);
        dataSource.setUrl(dbUrl);
        dataSource.setUser(gkUserName);
        dataSource.setPassword(gkUserPassword);
        dataSource.setConnectTimeout(connectTimeout);
        dataSource.setSsl(ssl);
        dataSource.setSslMode(sslMode);
        dataSource.setSslRootCert(sslCert);
        //Do not want to keep the connection after execution

        try {
            new JdbcTemplate(dataSource).queryForList("select 1"); // Tests the connection
        } catch (Exception e) {
            logger.error("Failed to connect to " + address);
            dataSource.close(); // close the datasource
            throw e;
        }
        logger.info("Using the following properties with the connection: " + ssl);
        return dataSource;

    }

    private void updateUser(JdbcTemplate conn, String address, String user, String password, RoleType role, String expirationTime ) throws SQLException{
        logger.info("Rotating the password for " + user + " on " + address + " with role " + role.getDbRole());
        conn.execute("ALTER USER " + user + " PASSWORD '" + password + "' VALID UNTIL " + " '" + expirationTime + "'", new PostgresCallableStatementExecutor());
        logger.info("Done Updating user " + user + " on " + address + " with role " + role.getDbRole());
    }

    private void createUser(JdbcTemplate conn, String address, String user, String password, RoleType role, String expirationTime ) throws SQLException{
        logger.info("Creating user " + user + " on " + address + " with role " + role.getDbRole());
        conn.execute("CREATE USER " + user, new PostgresCallableStatementExecutor());
        conn.execute("SET log_statement='none'", new PostgresCallableStatementExecutor());
        conn.execute("ALTER USER " + user + " PASSWORD '" + password + "' VALID UNTIL " + " '" + expirationTime + "'", new PostgresCallableStatementExecutor());
        conn.execute("SET log_statement='ddl'", new PostgresCallableStatementExecutor());
        conn.execute("GRANT " + role.getDbRole() + " TO " + user, new PostgresCallableStatementExecutor());
        logger.info("Done Creating user " + user + " on " + address + " with role " + role.getDbRole());
    }

    private boolean userExists(JdbcTemplate conn, String user){
        logger.info("Checking to see if user " + user + " exists");
        return conn.queryForList("SELECT 1 FROM pg_roles WHERE rolname='" + user+"'").size() > 0;
    }
    private boolean revokeUser(JdbcTemplate conn, String user){
        conn.execute("DROP USER IF EXISTS " + user, new PostgresCallableStatementExecutor());
        return !userExists(conn, user);
    }

    private class PostgresCallableStatementExecutor implements CallableStatementCallback<Boolean> {
        public Boolean doInCallableStatement(CallableStatement callableStatement) throws SQLException, DataAccessException {
            return callableStatement.execute();
        }
    }

    private class PostgresDbUserMapper implements  RowMapper<DbUser> {
        public DbUser mapRow(ResultSet rs, int rowNum) throws SQLException{
            return new DbUser()
                    .setUsername(rs.getString(1));
        }
    }
}
