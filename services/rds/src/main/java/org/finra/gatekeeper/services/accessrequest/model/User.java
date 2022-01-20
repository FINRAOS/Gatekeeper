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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.util.Objects;

/**
 * Domain representation for a User in a Gatekeeper Request
 */
@Entity
@Table(name = "request_user")
public class User {

    private Long id;
    private String name;
    private String email;
    @JsonAlias("user_id")
    private String userId;

    @ManyToOne
    private AccessRequest accessRequest;

    /**
     * Setters and Getters for ID
     */

    @Id
    @GeneratedValue
    public Long getId(){
        return id;
    }

    public User setId(Long id){
        this.id = id;
        return this;
    }

    /**
     * Setters and Getters for name
     */

    public String getName(){
        return name;
    }

    public User setName(String name){
        this.name = name;
        return this;
    }

    /**
     * Setters and Getters for email
     */
    public String getEmail(){
        return email;
    }

    public User setEmail(String email){
        this.email = email;
        return this;
    }

    /**
     * Setters and getters for User Id
     */
    public String getUserId() {
        return userId;
    }

    public User setUserId(String userId) {
        this.userId = userId;
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

        User that = (User) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(email, that.email)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, name, email, userId);
    }

    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("ID", id)
                .add("User ID", userId)
                .add("Name", name)
                .add("Email", email)
                .toString();
    }

    /**
     * Constructors
     */

    public User(){}

    public User(String userId, String name, String email){
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

}
