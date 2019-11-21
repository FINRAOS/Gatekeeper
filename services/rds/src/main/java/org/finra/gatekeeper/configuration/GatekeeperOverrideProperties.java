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

package org.finra.gatekeeper.configuration;

import org.finra.gatekeeper.services.accessrequest.model.OverridePolicy;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix="gatekeeper.override-policy")
public class GatekeeperOverrideProperties {

    @Autowired
    private GatekeeperRoleService gatekeeperRoleService;

    private Integer maxDays;

    public Integer getMaxDays() {
        return maxDays;
    }

    public GatekeeperOverrideProperties setMaxDays(Integer maxDays) {
        this.maxDays = maxDays;
        return this;
    }

    public GatekeeperOverrideProperties setOverrides(Map<String, Map<String, Map<String, Integer>>> overrides) {
        this.overrides = overrides;
        return this;
    }

    private Map<String, Map<String, Map<String, Integer>>> overrides = new HashMap<>();

    public Map<String, Map<String, Map<String, Integer>>> getOverrides() {
        return overrides;
    }

    public OverridePolicy getOverrides(Map<String, RoleMembership> roleMemberships) {
        Set<GatekeeperRdsRole> roles = gatekeeperRoleService.getUserRoles(roleMemberships);
        Map<String, Map<String, Integer>> overridePolicy = new HashMap<>();
        if(!roles.isEmpty()) {
            roles.forEach(role -> {
                if(overrides.containsKey(role.toString().toLowerCase())) {
                    overrides.get(role.toString().toLowerCase()).forEach((gkRole, overrideValuesPerSdlc) -> {
                        if (!overridePolicy.containsKey(gkRole)) {
                            overridePolicy.put(gkRole, overrideValuesPerSdlc);
                        } else {
                            overrideValuesPerSdlc.forEach((sdlc, value) -> {
                                if (!overridePolicy.get(gkRole).containsKey(sdlc) || value > overridePolicy.get(gkRole).get(sdlc)) {
                                    overridePolicy.get(gkRole).put(sdlc, value);
                                }
                            });
                        }
                    });
                }
            });
        }
        return new OverridePolicy(overridePolicy);
    }

    public Integer getMaxDaysForRequest(Map<String, RoleMembership> roleMemberships, List<UserRole> userRoles, String sdlc) {
        Integer currMax = maxDays;
        boolean overrideExists = false;

        OverridePolicy overridePolicy = getOverrides(roleMemberships);

        //Check the maximum allowed duration for each role the requestor has. The lowest override value amongst these roles should be used.
        for(UserRole userRole : userRoles) {
            if(overridePolicy.getOverridePolicy().containsKey(userRole.getRole()) && overridePolicy.getOverridePolicy().get(userRole.getRole()).containsKey(sdlc)){
                if(!overrideExists) {
                    overrideExists = true;
                    currMax = overridePolicy.getOverridePolicy().get(userRole.getRole()).get(sdlc);
                } else {
                    Integer max = overridePolicy.getOverridePolicy().get(userRole.getRole()).get(sdlc);
                    currMax = max < currMax ? max : currMax;
                }
            }
        }

        return currMax;
    }
}
