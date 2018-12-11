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

package org.finra.gatekeeper.services.db.connections;

import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.interfaces.DBConnection;
import org.finra.gatekeeper.rds.model.DbUser;
import org.finra.gatekeeper.rds.model.RoleType;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final String EXPIRATION_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
    private final String getSchemas = "SELECT distinct table_schema||'.'||table_name FROM information_schema.role_table_grants "
    + "where grantee = ? order by table_schema||'.'||table_name";
    private final String getUsers = "select rolname from pg_roles where rolcanlogin = true";

    private final String gkUserName;
    private final String gkUserPassword;
    private final Boolean ssl;
    private final String sslMode;
    private final String sslCert;
    private final Integer connectTimeout;

    @Autowired
    public PostgresDBConnection(GatekeeperProperties gatekeeperProperties){
        GatekeeperProperties.GatekeeperDbProperties db = gatekeeperProperties.getDb();
        GatekeeperProperties.GatekeeperDbProperties.PostgresDbProperties postgres = db.getPostgres();
        this.gkUserName = db.getGkUser();
        this.gkUserPassword = db.getGkPass();
        this.ssl = postgres.getSsl();
        this.sslMode = postgres.getSslMode();
        this.sslCert = postgres.getSslCert();
        this.connectTimeout = postgres.getConnectTimeout();
    }

    private PGPoolingDataSource connect(String url) throws SQLException{
        logger.info("Getting connection for " + url);
        logger.info("Creating Datasource connection for " + url);
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        String dbUrl = "jdbc:postgresql://" + url;

        dataSource.setDataSourceName(url);
        dataSource.setUrl(dbUrl);
        dataSource.setUser(gkUserName);
        dataSource.setPassword(gkUserPassword);
        dataSource.setConnectTimeout(connectTimeout);
        dataSource.setSsl(ssl);
        dataSource.setSslMode(sslMode);
        dataSource.setSslRootCert(sslCert);
        //Do not want to keep the connection after execution

        logger.info("Using the following properties with the connection: " + ssl );
        return dataSource;
    }

    public boolean grantAccess(String user, String password, RoleType role, String address, Integer length) throws SQLException {
        PGPoolingDataSource dataSource = null;

        try {
            dataSource = connect(address);
            JdbcTemplate conn = new JdbcTemplate(dataSource);

            String expirationTime = LocalDateTime.now().plusDays(length).format(DateTimeFormatter.ofPattern(EXPIRATION_TIMESTAMP));
            String userWithSuffix = user + "_" + role.getShortSuffix();
            //Try to revoke the user
            logger.info("Removing " + userWithSuffix + " from " + address + " if they exist.");
            revokeUser(conn, userWithSuffix);
            //Update the gk roles on the DB
            createUser(conn, address, userWithSuffix, password, role, expirationTime);
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
    private void createUser(JdbcTemplate conn, String address, String user, String password, RoleType role, String expirationTime ) throws SQLException{
        logger.info("Creating user " + user + " on " + address + " with role " + role.getDbRole());
        conn.execute("CREATE USER " + user + " PASSWORD '" + password + "' VALID UNTIL " + " '" + expirationTime + "'", new PostgresCallableStatementExecutor());
        conn.execute("GRANT " + role.getDbRole() + " TO " + user, new PostgresCallableStatementExecutor());
        logger.info("Done Creating user " + user + " on " + address + " with role " + role.getDbRole());
     }

    public boolean revokeAccess(String user, RoleType roles, String address) throws SQLException{
        PGPoolingDataSource dataSource = null;
        try {
            dataSource = connect(address);
            JdbcTemplate conn = new JdbcTemplate(dataSource);
            logger.info("Removing " + user + " from " + address + " if they exist.");
            if(roles != null) {
                //if roles is provided revoke the user with the suffix (from activiti)
                revokeUser(conn, user + "_" + roles.getShortSuffix());
            }else{
                //if roles is not provided just revoke the user (forced removal)
                revokeUser(conn, user);
            }
            return true;

        }catch(Exception ex){
            String username = roles == null ? user : user + "_" + roles.getShortSuffix();
            logger.error("An exception was thrown while trying to revoke user " + username + " from address " + address, ex);
            return false;
        } finally {
            if(dataSource != null) {
                dataSource.close();
            }
        }
    }

    public Map<RoleType, List<String>> getAvailableTables(String address) throws SQLException{
        Map<RoleType, List<String>> results = new HashMap<>();
        PGPoolingDataSource dataSource = connect(address);
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

    public List<String> checkDb(String address) throws GKUnsupportedDBException {
        String gkUserCreateRoleCheck = "select rolcreaterole from pg_roles where rolname = 'gatekeeper'";
        String gkRoleCheck = "select rolname from pg_roles where rolname in ('gk_datafix','gk_dba','gk_readonly')";

        List<String> issues = new ArrayList<>();
        List<String> gkRoles = new ArrayList<>();
        gkRoles.addAll(Arrays.asList("gk_datafix", "gk_readonly", "gk_dba"));
        PGPoolingDataSource dataSource = null;

        try{
            logger.info("Checking the gatekeeper setup for " + address);
            dataSource = connect(address);
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
        }
        catch(CannotGetJdbcConnectionException ex){
            logger.error("Failed to connect to DB", ex);
            if(ex.getMessage().contains("password")) {
                issues.add("Password authentication failed for gatekeeper user");
            }else{
                issues.add("Unable to connect to DB (Check network configuration)");
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
     * @param address - the url for the db
     * @param users - the list of users to look for
     * @return boolean - true if the user still owns tables, false otherwise (they don't exist)
     *
     * @throws SQLException - if there's an issue executing the query on the database
     */
    public List<String> checkIfUsersHasTables(String address, List<String> users) throws SQLException{
        PGPoolingDataSource dataSource = null;
        try {
            dataSource = connect(address);
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

    public List<DbUser> getUsers(String address) throws SQLException{
        PGPoolingDataSource dataSource = connect(address);
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

    public List<String> getAvailableRoles(String address) throws SQLException{
        PGPoolingDataSource dataSource = connect(address);
        JdbcTemplate conn = new JdbcTemplate(dataSource);
        List<String> results;
        logger.info("Getting available roles for " + address);
        try {
            results = conn.queryForList("select rolname from pg_catalog.pg_roles where rolname like 'gk_%' and rolcanlogin = false", String.class);
        } catch (Exception ex) {
            logger.error("Could not retrieve list of roles for database " + address, ex);
            throw ex;
        }
        dataSource.close();
        return results;
    }

    private boolean revokeUser(JdbcTemplate conn, String user){
        return conn.execute("DROP USER IF EXISTS " + user, new PostgresCallableStatementExecutor());

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
