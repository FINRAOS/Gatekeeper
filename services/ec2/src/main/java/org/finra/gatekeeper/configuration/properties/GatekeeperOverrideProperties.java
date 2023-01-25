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

package org.finra.gatekeeper.configuration.properties;

import org.finra.gatekeeper.services.accessrequest.model.OverridePolicy;
import org.finra.gatekeeper.services.auth.GatekeeperRole;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix="gatekeeper.override-policy")
public class GatekeeperOverrideProperties {
    /**
     * Max Hours allowed on a Request
     */
    private Integer maxHours;

    public Integer getMaxHours() {
        return maxHours;
    }

    public GatekeeperOverrideProperties setMaxHours(Integer maxHours) {
        this.maxHours = maxHours;
        return this;
    }

    public GatekeeperOverrideProperties setOverrides(Map<String, Map<String, Integer>> overrides) {
        this.overrides = overrides;
        return this;
    }

    private Map<String, Map<String, Integer>> overrides = new HashMap<>();

    public Map<String, Map<String, Integer>> getOverrides() {
        return overrides;
    }

    public OverridePolicy getOverrides(GatekeeperRole role) {
        Map<String, Integer> overridePolicy = new HashMap<>();
        if(overrides.containsKey(role.toString().toLowerCase())) {
            overrides.get(role.toString().toLowerCase()).forEach((sdlc, value) -> {
                overridePolicy.put(sdlc, value);
            });
        }

        return new OverridePolicy(overridePolicy);
    }

    public Integer getMaxHoursForRequest(GatekeeperRole gkRole, String sdlc) {
        Integer currMax = maxHours;
        OverridePolicy overridePolicy = getOverrides(gkRole);

        //Check the maximum allowed duration for the requestor. The lowest override value should be used.
        if(overridePolicy.getOverridePolicy().containsKey(sdlc)){
            Integer max = overridePolicy.getOverridePolicy().get(sdlc);
            currMax = max < currMax ? max : currMax;
        }
        return currMax;
    }
}
