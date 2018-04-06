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

package org.finra.gatekeeper.common.services.user.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Gatekeeper LDAP User Entry
 * <p>
 */
public class GatekeeperUserEntry extends GatekeeperSearchUserEntry {

    private String dn;

    public GatekeeperUserEntry(String accountId, String dn, String email, String name) {
        super(accountId, email, name);
        this.dn = dn;
    }

    public String getDn() {
        return dn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GatekeeperUserEntry that = (GatekeeperUserEntry) o;
        return Objects.equal(dn, that.dn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), dn);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("entry", super.toString())
                .add("dn", dn)
                .toString();
    }
}
