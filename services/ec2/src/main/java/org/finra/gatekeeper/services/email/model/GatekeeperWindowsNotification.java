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
 */

package org.finra.gatekeeper.services.email.model;

import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.User;

import java.util.List;
import java.util.Map;

public class GatekeeperWindowsNotification {
    private AccessRequest accessRequest;
    private User user;
    private List<String> cancelledInstances;

    public AccessRequest getAccessRequest() {
        return accessRequest;
    }

    public GatekeeperWindowsNotification setAccessRequest(AccessRequest accessRequest) {
        this.accessRequest = accessRequest;
        return this;
    }

    public User getUser() {
        return user;
    }

    public GatekeeperWindowsNotification setUser(User user) {
        this.user = user;
        return this;
    }

    public List<String> getCancelledInstances() {
        return cancelledInstances;
    }

    public GatekeeperWindowsNotification setCancelledInstances(List<String> cancelledInstances) {
        this.cancelledInstances = cancelledInstances;
        return this;
    }
}
