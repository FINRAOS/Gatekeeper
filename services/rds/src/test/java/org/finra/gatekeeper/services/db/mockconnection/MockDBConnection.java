/*
 *
 * Copyright 2020. Gatekeeper Contributors
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
 */

package org.finra.gatekeeper.services.db.mockconnection;

import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.interfaces.DBConnection;
import org.finra.gatekeeper.rds.model.*;

import java.sql.SQLException;
import java.util.*;

/**
 * This class is used to test the DatabaseConnectionService Class
 */
public class MockDBConnection implements DBConnection {
    @Override
    public boolean grantAccess(RdsGrantAccessQuery rdsGrantAccessQuery) throws Exception {
        return rdsGrantAccessQuery.getUser().contains("happy");
    }

    @Override
    public boolean revokeAccess(RdsRevokeAccessQuery rdsRevokeAccessQuery) throws Exception {
        return rdsRevokeAccessQuery.getUser().contains("happy");
    }

    @Override
    public Map<RoleType, List<String>> getAvailableTables(RdsQuery rdsQuery) throws Exception {
        Map<RoleType, List<String>> roleTypeListMap = new HashMap<>();
        roleTypeListMap.put(RoleType.READONLY, Arrays.asList("ro_table1", "ro_table2"));
        roleTypeListMap.put(RoleType.READONLY_CONFIDENTIAL, Arrays.asList("roc_table1"));
        roleTypeListMap.put(RoleType.DBA, Arrays.asList("dba_table1", "dba_table2"));
        roleTypeListMap.put(RoleType.DBA_CONFIDENTIAL, Arrays.asList("dbac_table1"));
        roleTypeListMap.put(RoleType.DATAFIX, Arrays.asList("df_table1"));
        return roleTypeListMap;
    }

    @Override
    public List<String> checkDb(RdsQuery rdsQuery) throws GKUnsupportedDBException {
        if(rdsQuery.getDbInstanceName().contains("fail")){
            return Collections.singletonList("Failed");
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> checkIfUsersHasTables(RdsCheckUsersTableQuery rdsCheckUsersTableQuery) throws SQLException {
        List<String> response = new ArrayList<>();
        for(String user: rdsCheckUsersTableQuery.getUsers()) {
            if(!user.contains("happy")){
                throw new SQLException("An Error Occurred");
            }
        }
        return response;
    }

    @Override
    public List<DbUser> getUsers(RdsQuery rdsQuery) throws SQLException {
        return Arrays.asList(
                new DbUser().setUsername("gk_test_ro").setRoles(null),
                new DbUser().setUsername("admin").setRoles(null)
        );
    }

    @Override
    public List<String> getAvailableRoles(RdsQuery rdsQuery) throws SQLException {
        return Arrays.asList("DBA", "READONLY", "DATAFIX");
    }
}
