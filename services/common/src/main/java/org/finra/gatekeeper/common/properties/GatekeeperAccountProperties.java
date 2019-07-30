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

package org.finra.gatekeeper.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix="gatekeeper.account")
public class GatekeeperAccountProperties {
    /**
     * The URL for the service serving your accounts
     */
    private String serviceURL;
    /**
     * The URI for the service serving your accounts
     */
    private String serviceURI;

    /**
     * The overrides for SDLC
     */

    private Map<String, String> sdlcOverrides = new HashMap<>();

    /**
     * This is controls the grouping order for SDLC as they appear in the app.
     */
    private Map<String, Integer> sdlcGrouping = new HashMap<>();

    public String getServiceURL() {
        return serviceURL;
    }

    public GatekeeperAccountProperties setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
        return this;
    }

    public String getServiceURI() {
        return serviceURI;
    }

    public GatekeeperAccountProperties setServiceURI(String serviceURI) {
        this.serviceURI = serviceURI;
        return this;
    }

    /**
     * This takes the sdlcOverrides map and reverses it so that it is account -> sdlc
     * @return
     */
    public Map<String, String> getAccountSdlcOverrides() {
        Map<String, String> individualOverrideMapping = new HashMap<>();
        if(!this.sdlcOverrides.isEmpty()) {
            this.sdlcOverrides.entrySet().forEach(entry -> {
                final String sdlc = entry.getKey();
                final List<String> overrideAccountIds = Arrays.asList(entry.getValue().split(","));
                overrideAccountIds.forEach(account -> {
                    individualOverrideMapping.put(account.trim(), sdlc);
                });
            });
        }
        return individualOverrideMapping;
    }

    public Map<String, String> getSdlcOverrides() {
        return sdlcOverrides;
    }

    public GatekeeperAccountProperties setSdlcOverrides(Map<String, String> sdlcOverrides) {
        this.sdlcOverrides = sdlcOverrides;
        return this;
    }

    public Map<String, Integer> getSdlcGrouping() {
        return sdlcGrouping;
    }

    public GatekeeperAccountProperties setSdlcGrouping(Map<String, Integer> sdlcGrouping) {
        this.sdlcGrouping = sdlcGrouping;
        return this;
    }
}
