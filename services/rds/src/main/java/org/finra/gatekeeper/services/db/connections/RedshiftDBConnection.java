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

import org.apache.commons.dbcp.BasicDataSource;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.interfaces.DBConnection;
import org.finra.gatekeeper.rds.interfaces.GKUserCredentialsProvider;
import org.finra.gatekeeper.rds.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Interface for dealing with MySQL RDS Instances.
 */
@Component
public class RedshiftDBConnection implements DBConnection {
    private final Logger logger = LoggerFactory.getLogger(RedshiftDBConnection.class);

    private final GKUserCredentialsProvider gkUserCredentialsProvider;
    private final String gkUserName;
    private final String ssl;
    private final String EXPIRATION_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    public RedshiftDBConnection(GatekeeperProperties gatekeeperProperties,
                                @Qualifier("credentialsProvider") GKUserCredentialsProvider gkUserCredentialsProvider){
        this.gkUserCredentialsProvider = gkUserCredentialsProvider;
        this.gkUserName = gatekeeperProperties.getDb().getGkUser();
        this.ssl = gatekeeperProperties.getDb().getRedshift().getSsl();

    }

    public boolean grantAccess(RdsGrantAccessQuery rdsGrantAccessQuery) throws Exception{
        String address = rdsGrantAccessQuery.getAddress();
        String user = rdsGrantAccessQuery.getUser();
        RoleType role = rdsGrantAccessQuery.getRole();
        String password = rdsGrantAccessQuery.getPassword();
        int length = rdsGrantAccessQuery.getTime();

        JdbcTemplate conn = null;

        try {
            conn = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsGrantAccessQuery));

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
        }
    }

    public boolean revokeAccess(RdsRevokeAccessQuery rdsRevokeAccessQuery) throws Exception{
        String user = rdsRevokeAccessQuery.getUser();
        String address = rdsRevokeAccessQuery.getAddress();
        RoleType role = rdsRevokeAccessQuery.getRole();

        JdbcTemplate conn = null;
        try {
            conn = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsRevokeAccessQuery));
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
        }
    }

    public List<String> checkDb(RdsQuery rdsQuery) throws GKUnsupportedDBException{
        String address = rdsQuery.getAddress();

        String gkUserCreateRoleCheck = "select usesuper from pg_user where usename = 'gatekeeper'";
        String gkRoleCheck = "select groname from pg_group where groname in ('gk_datafix','gk_dba','gk_readonly')";

        List<String> issues = new ArrayList<>();
        List<String> gkRoles = new ArrayList<>();
        gkRoles.addAll(Arrays.asList("gk_datafix", "gk_readonly", "gk_dba"));

        try{
            logger.info("Checking the gatekeeper setup for " + address);
            JdbcTemplate conn = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
            Boolean createRolePermCheckResult = conn.queryForObject(gkUserCreateRoleCheck, Boolean.class);
            List<String> roleCheckResult = conn.queryForList(gkRoleCheck, String.class);

            if(!createRolePermCheckResult){
                issues.add("gatekeeper user missing usesuper");
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
        }
        return issues;
    }

    public List<DbUser> getUsers(RdsQuery rdsQuery) throws SQLException {
        String getUsers = "select usename from pg_user";
        List<DbUser> users = new ArrayList<>();
        try {
            JdbcTemplate template = connect(rdsQuery.getAddress(), gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
            users = template.query(getUsers, new RedshiftDbUserMapper());
        }catch(SQLException e){
            logger.error("Error retrieving users from DB", e);
        }

        return users;
    }

    public List<String> checkIfUsersHasTables(RdsCheckUsersTableQuery rdsQuery){
        return Collections.emptyList();
    }

    public List<String> getAvailableRoles(RdsQuery rdsQuery) throws SQLException{
        String address = rdsQuery.getAddress();

        JdbcTemplate conn = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
        List<String> results;
        logger.info("Getting available roles for " + address);
        try {
            results = conn.queryForList("select groname from pg_catalog.pg_group where groname like 'gk_%'", String.class);
        } catch (Exception ex) {
            logger.error("Could not retrieve list of roles for database " + address, ex);
            throw ex;
        }
        return results;
    }

    public Map<RoleType, List<String>> getAvailableTables(RdsQuery rdsQuery) throws SQLException{
        String address = rdsQuery.getAddress();
        Map<RoleType, List<String>> results = new HashMap<>();
        JdbcTemplate conn = connect(address, gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
        String schemaQuery = "SELECT distinct table_schema||'.'||table_name FROM information_schema.tables where table_schema not in ('pg_catalog', 'pg_internal', 'information_schema');";
        logger.info("Getting available schema information for " + address);
        for(RoleType roleType : RoleType.values()) {
            try {
                List<String> schemas = conn.queryForList(schemaQuery, String.class);
                results.put(roleType, !schemas.isEmpty() ? schemas : Collections.singletonList("No Schemas are available for role " + roleType.getDbRole() + " at this time."));
                logger.info("Retrieved available schema information for database " + address + " for role " + roleType.getDbRole());
            } catch (Exception ex) {
                logger.error("Could not retrieve available role information for database " + address + " for role " +  roleType.getDbRole(), ex);
                results.put(roleType, Collections.singletonList("Unable to get available schemas for role " + roleType.getDbRole()));
            }
        }
        return results;
    }

    private JdbcTemplate connect(String url, String gkUserPassword) throws SQLException {
        String dbUrl = "jdbc:redshift://" + url;

        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("com.amazon.redshift.jdbc42.Driver");
        dataSource.setUrl(dbUrl+"?"+ssl);
        dataSource.setUsername(gkUserName);
        dataSource.setPassword(gkUserPassword);
        dataSource.setMinIdle(0);
        dataSource.setMaxIdle(0);

        return new JdbcTemplate(dataSource);
    }

    private void updateUser(JdbcTemplate conn, String address, String user, String password, RoleType role, String expirationTime ) throws SQLException{
        logger.info("Rotating the password for " + user + " on " + address + " with role " + role.getDbRole());
        conn.execute("ALTER USER " + user + " PASSWORD '" + password + "' VALID UNTIL " + " '" + expirationTime + "'", new CallableStatementExecutor());
        logger.info("Done Updating user " + user + " on " + address + " with role " + role.getDbRole());
    }

    private void createUser(JdbcTemplate conn, String address, String user, String password, RoleType role, String expirationTime ) throws SQLException{
        logger.info("Creating user " + user + " on " + address + " with role " + role.getDbRole());
        conn.execute("CREATE USER " + user + " PASSWORD '" + password + "' VALID UNTIL " + " '" + expirationTime + "'", new CallableStatementExecutor());
        conn.execute("ALTER GROUP " + role.getDbRole() + " ADD USER " + user, new CallableStatementExecutor());
        logger.info("Done Creating user " + user + " on " + address + " with role " + role.getDbRole());
    }

    private boolean userExists(JdbcTemplate conn, String user) throws SQLException {
        logger.info("Checking to see if user " + user + " exists");
        return conn.queryForObject("SELECT count(*) FROM pg_user WHERE usename='" + user+"'", Integer.class) > 0;
    }
    private boolean revokeUser(JdbcTemplate conn, String user) throws SQLException {
        conn.execute("DROP USER IF EXISTS " + user);
        return !userExists(conn, user);
    }

    private class CallableStatementExecutor implements CallableStatementCallback<Boolean> {
        public Boolean doInCallableStatement(CallableStatement callableStatement) throws SQLException, DataAccessException {
            return callableStatement.execute();
        }
    }
    
    private class RedshiftDbUserMapper implements RowMapper<DbUser> {
        public DbUser mapRow(ResultSet rs, int rowNum) throws SQLException{
            return new DbUser()
                    .setUsername(rs.getString(1));
        }
    }
}
