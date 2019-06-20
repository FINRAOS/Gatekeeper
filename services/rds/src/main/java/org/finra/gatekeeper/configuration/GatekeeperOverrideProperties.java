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

import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix="gatekeeper.overridePolicy")
public class GatekeeperOverrideProperties {
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

    public Map<String, Map<String, Map<String, Integer>>> getOverridePolicy(Map<String, RoleMembership> roleMemberships, boolean isApprover) {
        Map<String, Map<String, Map<String, Integer>>> overridePolicy = new HashMap<>();
        for(String application : roleMemberships.keySet()) {
            overridePolicy.put(application, getOverridePolicyByApplication(roleMemberships.get(application), isApprover));
        }
        return overridePolicy;
    }

    private Map<String, Map<String, Integer>> getOverridePolicyByApplication(RoleMembership roleMembership, boolean isApprover){

        if(isApprover)
            return this.overrides.get(GatekeeperRdsRole.APPROVER.toString().toLowerCase());
        if(roleMembership.getRoles().containsKey(GatekeeperRdsRole.DBA))
            return this.overrides.get(GatekeeperRdsRole.DBA.toString().toLowerCase());
        if(roleMembership.getRoles().containsKey(GatekeeperRdsRole.DEV))
            return this.overrides.get(GatekeeperRdsRole.DEV.toString().toLowerCase());
        if(roleMembership.getRoles().containsKey(GatekeeperRdsRole.OPS))
            return this.overrides.get(GatekeeperRdsRole.OPS.toString().toLowerCase());

        return this.overrides.get(GatekeeperRdsRole.UNAUTHORIZED.toString().toLowerCase());
    }

    public Integer getMaxDaysForRequest(RoleMembership roleMembership, List<UserRole> roleList, String sdlc){
        Integer currMax = maxDays;

        //For each role let's check if there was some override value set.
        for(UserRole role : roleList){
            Map<String, Map<String, Integer>> overridePolicy = getOverridePolicyByApplication(roleMembership, false);

            //if there's a policy then lets keep going
            if(overridePolicy != null
                    && overridePolicy.containsKey(role.getRole())
                    && overridePolicy.containsValue(overridePolicy.get(role.getRole()))){

                Map<String, Integer> env = overridePolicy.get(role.getRole());
                Integer max = env.get(sdlc) != null ? env.get(sdlc) : maxDays;

                currMax = max < currMax ? max : currMax;

            }

        }
        return currMax;
    }
}
