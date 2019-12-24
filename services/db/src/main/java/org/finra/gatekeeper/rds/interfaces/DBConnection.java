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
import org.finra.gatekeeper.rds.model.DbUser;
import org.finra.gatekeeper.rds.model.RoleType;

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
     * @param user
     * @param password
     * @param role
     * @param address
     * @param time
     * @return
     * @throws Exception
     */
    boolean grantAccess(String user, String password, RoleType role, String address, String gkUserPassword, Integer time) throws Exception;

    /**
     * Removes user from a database
     *
     * @param user
     * @param role
     * @param address
     * @return
     * @throws Exception
     */
    boolean revokeAccess(String user, RoleType role, String address, String gkUserPassword) throws Exception;


    Map<RoleType, List<String>> getAvailableTables(String address, String gkUserPassword) throws Exception;

    /**
     * Connects to a DB and checks for any required setups
     * @param address
     * @return String - empty if no issues, otherwise will be the issues
     */
    List<String> checkDb(String address, String gkUserPassword) throws GKUnsupportedDBException;

    List<String> checkIfUsersHasTables(String address, List<String> users, String gkUserPassword) throws SQLException;

    /**
     * Connects to the DB and gets all of the Users currently on the RDS instance
     * @param address
     * @return List of all of the users on the instance
     */
    List<DbUser> getUsers(String address, String gkUserPassword) throws SQLException;

    /**
     * Connects to the DB and gets all of the available gk roles currently on the RDS instance
     */
    List<String> getAvailableRoles(String address, String gkUserPassword) throws SQLException;

}
