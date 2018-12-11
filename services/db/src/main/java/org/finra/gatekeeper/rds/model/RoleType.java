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

package org.finra.gatekeeper.rds.model;

/**
 * Domain Representation of the 3 Gatekeeper RDS Roles that get requested
 */
public enum RoleType {
    READONLY("readonly", "ro", "gk_readonly", "Read Only"),
    DATAFIX("datafix", "df", "gk_datafix", "Datafix"),
    DBA("dba", "dba", "gk_dba", "DBA"),
    READONLY_CONFIDENTIAL("readonly_confidential", "roc", "gk_readonly_confidential", "Read Only Confidential" ),
    DBA_CONFIDENTIAL("dba_confidential", "dbac", "gk_dba_confidential", "DBA Confidential" );

    private String userSuffix;
    private String shortSuffix;
    private String dbRole;
    private String roleDescription;

    public String getUserSuffix() {
        return userSuffix;
    }

    public String getShortSuffix() { return shortSuffix; }

    public String getDbRole() {
        return dbRole;
    }

    public String getRoleDescription() {return roleDescription;}

    RoleType(String userSuffix, String shortSuffix, String dbRole, String roleDescription){
        this.userSuffix = userSuffix;
        this.shortSuffix = shortSuffix;
        this.dbRole = dbRole;
        this.roleDescription = roleDescription;
    }
}
