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

import org.apache.commons.dbcp.BasicDataSource;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.accessrequest.model.RoleType;
import org.finra.gatekeeper.services.db.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.services.db.interfaces.DBConnection;
import org.finra.gatekeeper.services.db.model.DbUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

/**
 * Interface for dealing with MySQL RDS Instances.
 */
@Component
public class MySQLDBConnection implements DBConnection {
    private final Logger logger = LoggerFactory.getLogger(MySQLDBConnection.class);

    private final String gkUserName;
    private final String gkUserPassword;
    private final String ssl;

    @Autowired
    public MySQLDBConnection(GatekeeperProperties gatekeeperProperties){
        this.gkUserName = gatekeeperProperties.getDb().getGkUser();
        this.gkUserPassword = gatekeeperProperties.getDb().getGkPass();
        this.ssl = gatekeeperProperties.getDb().getMysql().getSsl();
    }

    private JdbcTemplate connect(String url) throws SQLException {
        String dbUrl = "jdbc:mysql://" + url;

        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
        dataSource.setUrl(dbUrl+"?"+ssl);
        dataSource.setUsername(gkUserName);
        dataSource.setPassword(gkUserPassword);
        dataSource.setMinIdle(0);
        dataSource.setMaxIdle(0);

        return new JdbcTemplate(dataSource);
    }
    public boolean grantAccess(String user, String password, RoleType role, String address, Integer time) throws Exception{
        try {
            createUser(address, user, password, role, String.valueOf(time));
            return true;
        }catch(Exception ex){
            logger.error("An exception was thrown trying to create user " + getGkUserName(user, role) + " at address " + address, ex);
            return false;
        }
    }

    private void createUser(String address, String user, String password, RoleType role, String expirationTime) throws Exception{
        JdbcTemplate conn = connect(address);

        String userRole = getGkUserName(user, role); //16 is the maximum length for a user in MySQL, if there's a user hitting this limit, a shorter suffix shall be used
        //revoke the user if they exist
        revokeAccess(user, role, address);

        logger.info("Creating User " + userRole + " if they dont already exist");
        boolean wasUserCreated = conn.execute(new MySqlStatement("CREATE USER " + userRole + " IDENTIFIED BY '" + password + "'"));
        logger.info(wasUserCreated ?
                "User " + userRole + " successfully created on database" + address :
                "Failed to create " + userRole + " on database " + address
        );

        List<String> schemasToGrant = getSchemasForDb(conn);


        logger.info("User " + userRole + " has role "+ role + " granting him those privs");

        String privs;
        switch(role){
            case READONLY:
                privs = "SELECT";
                break;
            case DATAFIX:
                privs = "SELECT, INSERT, DELETE, UPDATE";
                break;
            case DBA:
                privs = "SELECT, CREATE, ALTER, DROP ";
                break;
            default:
                throw new GatekeeperException("Unknown Role provided: " + role);
        }

        logger.info("Granting roles to all of the non mysql schemas (" +schemasToGrant + ")");
        schemasToGrant.forEach(schema -> {
            logger.info("Granting " + privs + " for " + userRole + " on " + schema);
            conn.execute(generateQuery(privs, userRole, schema)) ;
            logger.info("Done!");
        });

        logger.info("Successfully Created " + userRole + " with "+ role + " for the following schemas " + schemasToGrant);
    }

    /**
     * MySQL has a limit of 16 characters in the user name, if the user that gatekeeper wants to generate that is > 16 characters then
     * gatekeeper will attempt to use a shorter role, if it's STILL too long then just cut off from the username (hope this is rare case.)
     *
     * @param user
     * @param role
     * @return
     */
    private String getGkUserName(String user, RoleType role){
        return role != null ? user + "_" + role.getShortSuffix() : user;
    }

    //pulls all the non system schemas for granting
    private List<String> getSchemasForDb(JdbcTemplate conn){
        String getSchemas = "select distinct table_schema from information_schema.tables where table_schema not in ('information_schema', 'mysql', 'sys', 'performance_schema')";
        return conn.queryForList(getSchemas, String.class);
    }

    public Map<RoleType, List<String>> getAvailableTables(String address) throws SQLException{
        Map<RoleType, List<String>> results = new HashMap<>();
        JdbcTemplate template = connect(address);
        String schemaQuery = "select concat(table_schema,'.',table_name) from information_schema.tables where table_schema not in ('information_schema', 'mysql', 'sys', 'performance_schema')";
        for(RoleType roleType : RoleType.values()) {
            try {
                results.put(roleType, template.queryForList(schemaQuery, String.class));
            } catch (Exception ex) {
                logger.error("An exception was thrown while trying to fetch tables over MySQL at address " + address, ex);
                results.put(roleType, Collections.singletonList("Unable to get available schemas"));
            }
        }
        return results;
    }

    private String generateQuery(String roles, String user, String schema){
        return "GRANT "+roles+" ON " + schema + ".* TO " + user + " REQUIRE SSL";
    }

    public boolean revokeAccess(String user, RoleType role, String address) throws Exception{
        String userRole = getGkUserName(user, role);

        try{
            JdbcTemplate conn = connect(address);
            logger.info("Deleting User " + user + " if they already exist on DB " + address);
            conn.execute("GRANT USAGE ON *.* to " + userRole);
            conn.execute("DROP USER '" + userRole + "'");

            logger.info("Deleted existing " + userRole + " on database " + address);
            return true;
        }catch (Exception ex){
            logger.error("An exception was thrown Trying to revoke access to " + userRole + " from " + address, ex);
            return false;
        }
    }

    public List<String> checkDb(String address) throws GKUnsupportedDBException{
        String checkGrants = "SHOW GRANTS FOR CURRENT_USER";

        List<String> issues = new ArrayList<>();

        try{
            logger.info("Checking the gatekeeper setup for " + address);
            JdbcTemplate template = connect(address);
            String roleCheckResult = template.queryForObject(checkGrants, String.class);
            if(!roleCheckResult.contains("CREATE USER")){
                issues.add("gatekeeper is missing CREATE USER");
            }
        }catch(SQLException e){
            logger.error("Error running check query", e);
        }
        catch(CannotGetJdbcConnectionException ex){
            logger.error("Could not connect", ex);
            if(ex.getMessage().contains("password")) {
                issues.add("Password authentication failed for gatekeeper user");
            } else{
                issues.add("Unable to connect to DB (Check network configuration)");
            }
        }

        return issues;
    }

    public List<DbUser> getUsers(String address) throws SQLException {
        String getUsers = "select user from mysql.user";
        List<DbUser> users = new ArrayList<>();
        try {
            JdbcTemplate template = connect(address);
            users = template.query(getUsers, new MySqlDbUserMapper());
        }catch(SQLException e){
            logger.error("Error retrieving users from DB", e);
        }

        return users;
    }

    public List<String> checkIfUsersHasTables(String address, List<String> users){
        return Collections.emptyList();
    }

    private class MySqlStatement implements StatementCallback<Boolean>{
        private String sql;

        public MySqlStatement(String sql){
            this.sql = sql;
        }

        public Boolean doInStatement(Statement statement) throws SQLException, DataAccessException {
            return statement.execute(sql);
        }
    }

    private class MySqlDbUserMapper implements RowMapper<DbUser> {
        public DbUser mapRow(ResultSet rs, int rowNum) throws SQLException{
            return new DbUser()
                    .setUsername(rs.getString(1));
        }
    }

}
