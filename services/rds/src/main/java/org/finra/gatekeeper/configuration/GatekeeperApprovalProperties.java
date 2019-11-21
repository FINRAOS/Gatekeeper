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

import org.finra.gatekeeper.rds.model.RoleType;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.services.auth.model.AppApprovalThreshold;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConfigurationProperties(prefix="gatekeeper.approval-threshold")
public class GatekeeperApprovalProperties {
    /**
     * Thresholds for DEV Role
     */
    private Map<String, Map<String, Integer>> dev;

    /**
     * Thresholds for Support Role
     */
    private Map<String, Map<String, Integer>> ops;

    /**
     * Thresholds for DBA Role
     */
    private Map<String, Map<String, Integer>> dba;

    private List<RoleType> gatekeeperRoles;
    private Set<String> sdlcs;

    public Map<String, Map<String, Integer>> getDev() {
        return dev;
    }

    public GatekeeperApprovalProperties setDev(Map<String, Map<String, Integer>> dev) {
        this.dev = dev;
        return this;
    }

    public Map<String, Map<String, Integer>> getOps() {
        return ops;
    }

    public GatekeeperApprovalProperties setOps(Map<String, Map<String, Integer>> support) {
        this.ops = support;
        return this;
    }

    public Map<String, Map<String, Integer>> getDba() {
        return dba;
    }

    public GatekeeperApprovalProperties setDba(Map<String, Map<String, Integer>> dba) {
        this.dba = dba;
        return this;
    }

    private Map<RoleType, Map<String, Integer>> getApprovalPolicy(GatekeeperRdsRole role) {
        switch (role) {
            case DEV:
                return addApprovalPolicyToMap(dev);
            case OPS:
                return addApprovalPolicyToMap(ops);
            case DBA:
                return addApprovalPolicyToMap(dba);
            default:
                return null;
        }
    }

    public Map<String, AppApprovalThreshold> getApprovalPolicy(Map<String, RoleMembership> roleMemberships) {
        Map<String, AppApprovalThreshold> approvalPolicy = new HashMap<>();
        roleMemberships.forEach((application, roleMembership) -> {
            approvalPolicy.put(application, getApprovalPolicyByApplication(roleMemberships.get(application)));
        });
        return approvalPolicy;
    }

    public Set<String> getAllSdlcs() {
        return dev.get(RoleType.values()[0].toString().toLowerCase()).keySet();
    }

    private AppApprovalThreshold getApprovalPolicyByApplication(RoleMembership roleMembership) {
        gatekeeperRoles = new ArrayList<>(Arrays.asList(RoleType.values()));
        sdlcs = getAllSdlcs();
        AppApprovalThreshold applicationApprovalPolicy = initializeApprovalPolicy();

        for(GatekeeperRdsRole role : roleMembership.getRoles().keySet()) {
            AppApprovalThreshold roleApprovalPolicyToCompareTo = new AppApprovalThreshold(getApprovalPolicy(role));
            applicationApprovalPolicy = mergeApprovalPolicies(applicationApprovalPolicy, roleApprovalPolicyToCompareTo);
        }

        return applicationApprovalPolicy;
    }

    private AppApprovalThreshold initializeApprovalPolicy() {
        AppApprovalThreshold initialApprovalPolicy = new AppApprovalThreshold();
        Map<RoleType, Map<String, Integer>> thresholds = new HashMap<>();
        sdlcs = getAllSdlcs();

        gatekeeperRoles.forEach(gatekeeperRole -> {
            thresholds.put(gatekeeperRole, new HashMap<>());
            sdlcs.forEach(sdlc -> {
                thresholds.get(gatekeeperRole).put(sdlc, -1);
            });
        });

        initialApprovalPolicy.setAppApprovalThresholds(thresholds);
        return initialApprovalPolicy;
    }

    private AppApprovalThreshold mergeApprovalPolicies(AppApprovalThreshold applicationApprovalPolicy, AppApprovalThreshold roleApprovalPolicy) {
        for(RoleType gatekeeperRole : applicationApprovalPolicy.getAppApprovalThresholds().keySet()) {
            for(String sdlc : applicationApprovalPolicy.getAppApprovalThresholds().get(gatekeeperRole).keySet()) {
                int applicationApprovalThreshold = applicationApprovalPolicy.getAppApprovalThresholds().get(gatekeeperRole).get(sdlc);
                int roleApprovalThreshold = roleApprovalPolicy.getAppApprovalThresholds().get(gatekeeperRole).get(sdlc);
                if(roleApprovalThreshold > applicationApprovalThreshold) {
                    applicationApprovalPolicy.getAppApprovalThresholds().get(gatekeeperRole).put(sdlc, roleApprovalThreshold);
                }
            }
        }
        return applicationApprovalPolicy;
    }

    private Map<RoleType, Map<String, Integer>> addApprovalPolicyToMap(Map<String, Map<String, Integer>> approvalThresholdsFromConfig) {
        Map<RoleType, Map<String, Integer>> approvalPolicy = new HashMap<>();
        approvalThresholdsFromConfig.forEach((role, policy) -> {
            approvalPolicy.put(RoleType.valueOf(role.toUpperCase()), policy);
        });
        return approvalPolicy;
    }
}
