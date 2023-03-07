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

package org.finra.gatekeeper.common.authfilter.parser;

import java.util.Objects;
import java.util.Set;

public class GatekeeperUserProfile implements IGatekeeperUserProfile {

    private String userId;
    private String name;
    private String email;
    private String source;
    private Set<String> memberships;

    public GatekeeperUserProfile(String userId, String source) {
        if(userId==null||source==null){
            throw new IllegalArgumentException("User userId and Source is required");
        }
        this.userId = userId.toUpperCase();
        this.source = source;
    }

    public GatekeeperUserProfile(String userId, String name, String email, Set<String> memberships,  String source) {
        if(userId==null||source==null){
            throw new IllegalArgumentException("User userId and Source is required");
        }
        this.userId = userId.toUpperCase();
        this.name = name;
        this.email = email;
        this.memberships = memberships;
        this.source = source;
    }

    @Override
    public String getUserId() {
        return this.userId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Set<String> getMemberships() { return memberships;}

    @Override
    public String getSource() {
        return this.source;
    }


    public GatekeeperUserProfile setMemberships(Set<String> memberships) {
        this.memberships = memberships;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GatekeeperUserProfile that = (GatekeeperUserProfile) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, source);
    }

    @Override
    public String toString() {
        return "userId: " + this.userId + ", Source: " + this.source;
    }
}
