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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class RdsGrantAccessQuery extends RdsQuery {
    /**
     * The user to create
     */
    private String user;
    /**
     * The password to give the user
     */
    private String password;

    /**
     * The role to give the user
     */
    private RoleType role;

    /**
     * The amount of time for the request
     */
    private Integer time;
    
    public String getUser() {
        return user;
    }

    public RdsGrantAccessQuery withUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RdsGrantAccessQuery withPassword(String password) {
        this.password = password;
        return this;
    }

    public RoleType getRole() {
        return role;
    }

    public RdsGrantAccessQuery withRole(RoleType role) {
        this.role = role;
        return this;
    }

    public Integer getTime() {
        return time;
    }

    public RdsGrantAccessQuery withTime(Integer time) {
        this.time = time;
        return this;
    }

    public RdsGrantAccessQuery(String account, String accountId, String region, String sdlc, String address, String dbName, String dbEngine, boolean rdsIAMAuth) {
        super(account, accountId, region, sdlc, address, dbName, dbEngine, rdsIAMAuth);
    }

    public RdsGrantAccessQuery(String account, String accountId, String region, String sdlc, String address, String dbInstanceName, String dbEngine, boolean rdsIAMAuth, String user, String password, RoleType role, Integer time) {
        super(account, accountId, region, sdlc, address, dbInstanceName, dbEngine, rdsIAMAuth);
        this.user = user;
        this.password = password;
        this.role = role;
        this.time = time;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("user", user)
                .add("password", password)
                .add("role", role)
                .add("time", time)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RdsGrantAccessQuery)) return false;
        if (!super.equals(o)) return false;
        RdsGrantAccessQuery that = (RdsGrantAccessQuery) o;
        return Objects.equal(user, that.user) &&
                Objects.equal(password, that.password) &&
                role == that.role &&
                Objects.equal(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), user, password, role, time);
    }
}
