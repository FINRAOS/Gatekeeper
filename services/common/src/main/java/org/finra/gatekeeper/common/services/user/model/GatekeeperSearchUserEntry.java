/*
 *
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
 */

package org.finra.gatekeeper.common.services.user.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class GatekeeperSearchUserEntry {
    private String userId;
    private String email;
    private String name;

    public GatekeeperSearchUserEntry(String accountId, String email, String name) {
        this.userId = accountId;
        this.email = email;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatekeeperSearchUserEntry that = (GatekeeperSearchUserEntry) o;
        return Objects.equal(userId, that.userId) &&
                Objects.equal(email, that.email) &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, email, name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("email", email)
                .add("name", name)
                .toString();
    }
}
