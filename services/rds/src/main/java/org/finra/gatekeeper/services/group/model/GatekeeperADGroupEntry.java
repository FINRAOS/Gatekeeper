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
package org.finra.gatekeeper.services.group.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class GatekeeperADGroupEntry {
    private String application;
    private String gkRole;
    private String sdlc;
    private String name;

    public GatekeeperADGroupEntry(String application, String gkRole, String sdlc, String name){
        this.application = application;
        this.gkRole = gkRole;
        this.sdlc = sdlc;
        this.name = name;
    }

    public String getApplication() {
        return application;
    }

    public String getGkRole() {
        return gkRole;
    }

    public String getSdlc() {
        return sdlc;
    }

    public String getName() {return name;}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GatekeeperADGroupEntry that = (GatekeeperADGroupEntry) obj;
        return Objects.equal(application, that.application) &&
               Objects.equal(gkRole, that.gkRole) &&
               Objects.equal(sdlc, that.sdlc) &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(application, gkRole, sdlc, name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Application", application)
                .add("GK ROLE", gkRole)
                .add("SDLC", sdlc)
                .add("NAME", name)
                .toString();
    }


}
