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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.List;

public class DbUser {
    private String username;
    private List<String> roles;

    public String getUsername() {
        return username;
    }

    public DbUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public DbUser setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbUser dbUser = (DbUser) o;
        return Objects.equal(username, dbUser.username) &&
                Objects.equal(roles, dbUser.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username, roles);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .add("roles", roles)
                .toString();
    }

    public DbUser(String username, List<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public DbUser() {
    }
}
