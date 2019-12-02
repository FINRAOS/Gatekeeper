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
package org.finra.gatekeeper.controllers.wrappers;


import org.finra.gatekeeper.rds.model.DbUser;

import java.util.List;

public class RemoveUsersWrapper {

    private String account;
    private String region;
    private String instanceId;
    private String instanceName;
    private List<DbUser> users;

    public String getAccount() {
        return account;
    }

    public RemoveUsersWrapper setAccount(String account) {
        this.account = account;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public RemoveUsersWrapper setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public RemoveUsersWrapper setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public RemoveUsersWrapper setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }


    public List<DbUser> getUsers() {
        return users;
    }

    public RemoveUsersWrapper setUsers(List<DbUser> users) {
        this.users = users;
        return this;
    }

    public RemoveUsersWrapper() {}

    public RemoveUsersWrapper(String instanceName, List<DbUser> users) {
        this.instanceName = instanceName;
        this.users = users;
    }
}
