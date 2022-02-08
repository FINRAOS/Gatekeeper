/*
 * Copyright 2022. Gatekeeper Contributors
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

package org.finra.gatekeeper.services.accessrequest.model;

import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.util.Objects;

/**
 * Domain Representation for a User Role
 */
@Entity
@Table(name = "request_role")
public class UserRole {

    private Long id;
    private String role;

    @ManyToOne
    private AccessRequest accessRequest;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public UserRole setId(Long id) {
        this.id = id;
        return this;
    }

    public String getRole() {
        return role;
    }

    public UserRole setRole(String role) {
        this.role = role;
        return this;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        UserRole that = (UserRole) o;
        return Objects.equals(id, that.id)
                && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, role);
    }

    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("ID", id)
                .add("Role", role)
                .toString();
    }

    /**
     * Constructors
     */

    public UserRole(){}

    public UserRole(String role){
        this.role = role;
    }
}
