/*
 * Copyright 2019. Gatekeeper Contributors
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

import java.util.List;

public class RdsCheckUsersTableQuery extends RdsQuery {
    /**
     * The list of users to check
     */
    private List<String> users;

    public List<String> getUsers() {
        return users;
    }

    public RdsCheckUsersTableQuery withUsers(List<String> users) {
        this.users = users;
        return this;
    }

    public RdsCheckUsersTableQuery(String account, String accountId, String region, String sdlc, String address, String dbName) {
        super(account, accountId, region, sdlc, address, dbName);
    }

    public RdsCheckUsersTableQuery(String account, String accountId, String region, String sdlc, String address, String dbName, List<String> users) {
        super(account, accountId, region, sdlc, address, dbName);
        this.users = users;
    }

}
