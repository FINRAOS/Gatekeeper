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

package org.finra.gatekeeper.rds.interfaces;

import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Standard contract for a Database Connection for Gatekeeper
 */
public interface DBConnection {
    /**
     * Grants user access to a database
     *
     * @param rdsGrantAccessQuery - the details of the database to grant access to
     * @return true if success, false if failure
     * @throws Exception - if connection or query fails on any level
     */
    boolean grantAccess(RdsGrantAccessQuery rdsGrantAccessQuery) throws Exception;

    /**
     * Removes user from a database
     *
     * @param rdsRevokeAccessQuery - the details of the database to revoke access to
     * @return true if success, false if failure
     * @throws Exception - if connection or query fails on any level
     */
    boolean revokeAccess(RdsRevokeAccessQuery rdsRevokeAccessQuery) throws Exception;


    /**
     * Gets all of the available tables for the user.
     *
     * @param rdsQuery - the details of the database to make the call
     * @return a Map of RoleType to List of String where the role type corresponds to the tables that they have access to
     * @throws Exception - if connection or query fails on any level
     */
    Map<RoleType, List<String>> getAvailableTables(RdsQuery rdsQuery) throws Exception;

    /**
     * Connects to a DB and checks for any required setups
     *
     * @param rdsQuery - the details of the database to make the call
     * @return String - empty if no issues, otherwise will be the issues
     */
    List<String> checkDb(RdsQuery rdsQuery) throws GKUnsupportedDBException;

    /**
     * Check if the users have any dependent objects
     *
     * @param rdsCheckUsersTableQuery - The details of the query for the list of users to check
     * @return List of the users who have tables
     * @throws SQLException - if connection or query fails on any level
     */
    List<String> checkIfUsersHasTables(RdsCheckUsersTableQuery rdsCheckUsersTableQuery) throws SQLException;

    /**
     * Connects to the DB and gets all of the Users currently on the RDS database
     * @param rdsQuery - the details of the database to make the call
     * @return List of all of the users on the database
     */
    List<DbUser> getUsers(RdsQuery rdsQuery) throws SQLException;

    /**
     * Connects to the DB and gets all of the available gk roles currently on the RDS database
     * @param rdsQuery - the details of the database to make the call
     * @return List of all available roles to that database
     */
    List<String> getAvailableRoles(RdsQuery rdsQuery) throws SQLException;

}
