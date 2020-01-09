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

public class RdsRevokeAccessQuery extends RdsQuery {
    /**
     * The user to create
     */
    private String user;

    /**
     * The role to give the user
     */
    private RoleType role;

    public String getUser() {
        return user;
    }

    public RdsRevokeAccessQuery withUser(String user) {
        this.user = user;
        return this;
    }

    public RoleType getRole() {
        return role;
    }

    public RdsRevokeAccessQuery withRole(RoleType role) {
        this.role = role;
        return this;
    }

    public RdsRevokeAccessQuery(String account, String accountId, String region, String sdlc, String address, String dbInstanceName, String user, RoleType role) {
        super(account, accountId, region, sdlc, address, dbInstanceName);
        this.user = user;
        this.role = role;
    }

    public RdsRevokeAccessQuery(String account, String accountId, String region, String sdlc, String address, String dbInstanceName) {
        super(account, accountId, region, sdlc, address, dbInstanceName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("user", user)
                .add("role", role)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RdsRevokeAccessQuery)) return false;
        if (!super.equals(o)) return false;
        RdsRevokeAccessQuery that = (RdsRevokeAccessQuery) o;
        return Objects.equal(user, that.user) &&
                role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), user, role);
    }
}
