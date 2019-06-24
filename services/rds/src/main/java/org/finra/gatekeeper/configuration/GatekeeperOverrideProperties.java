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

import org.finra.gatekeeper.configuration.model.AppSpecificOverridePolicy;
import org.finra.gatekeeper.rds.model.RoleType;
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

    public Map<String, AppSpecificOverridePolicy> getOverridePolicy(Map<String, RoleMembership> roleMemberships, boolean isApprover) {
        Map<String, AppSpecificOverridePolicy> overridePolicy = new HashMap<>();
        roleMemberships.forEach((application, roleMembership) -> {
            overridePolicy.put(application, getOverridePolicyByApplication(roleMemberships.get(application), isApprover));
        })
        return overridePolicy;
    }

    public Integer getMaxDaysForRequest(RoleMembership roleMembership, List<UserRole> roleList, String sdlc){
        Integer currMax = maxDays;

        //For each role let's check if there was some override value set.
        for(UserRole role : roleList){
            AppSpecificOverridePolicy appSpecificOverridePolicy = getOverridePolicyByApplication(roleMembership, false);

            //if there's a policy then lets keep going
            if(appSpecificOverridePolicy != null
                    && appSpecificOverridePolicy.getAppSpecificOverridePolicy().containsKey(RoleType.valueOf(role.getRole().toUpperCase()))
                    && appSpecificOverridePolicy.getAppSpecificOverridePolicy().containsValue(appSpecificOverridePolicy.getAppSpecificOverridePolicy().get(RoleType.valueOf(role.getRole().toUpperCase())))){

                Map<String, Integer> env = appSpecificOverridePolicy.getAppSpecificOverridePolicy().get(RoleType.valueOf(role.getRole().toUpperCase()));
                Integer max = env.get(sdlc) != null ? env.get(sdlc) : maxDays;

                currMax = max < currMax ? max : currMax;

            }

        }
        return currMax;
    }

    private AppSpecificOverridePolicy getOverridePolicyByApplication(RoleMembership roleMembership, boolean isApprover){
        AppSpecificOverridePolicy appSpecificOverridePolicy = new AppSpecificOverridePolicy();
        Map<String, Map<String, Integer>> overridePolicyStringFormat;
        Map<RoleType, Map<String, Integer>> overridePolicyEnumFormat = new HashMap<>();

        if(isApprover)
            overridePolicyStringFormat = this.overrides.get(GatekeeperRdsRole.APPROVER.toString().toLowerCase());
        else if(roleMembership.getRoles().containsKey(GatekeeperRdsRole.DBA))
            overridePolicyStringFormat = this.overrides.get(GatekeeperRdsRole.DBA.toString().toLowerCase());
        else if(roleMembership.getRoles().containsKey(GatekeeperRdsRole.DEV))
            overridePolicyStringFormat = this.overrides.get(GatekeeperRdsRole.DEV.toString().toLowerCase());
        else if(roleMembership.getRoles().containsKey(GatekeeperRdsRole.OPS))
            overridePolicyStringFormat = this.overrides.get(GatekeeperRdsRole.OPS.toString().toLowerCase());
        else
            overridePolicyStringFormat = this.overrides.get(GatekeeperRdsRole.UNAUTHORIZED.toString().toLowerCase());

        overridePolicyStringFormat.forEach((roleType, policy) -> {
            overridePolicyEnumFormat.put(RoleType.valueOf(roleType.toUpperCase()), policy);
        });

        appSpecificOverridePolicy.setAppSpecificOverridePolicy(overridePolicyEnumFormat);
        return appSpecificOverridePolicy;
    }
}
