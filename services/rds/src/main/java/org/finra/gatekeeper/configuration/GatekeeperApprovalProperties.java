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

import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix="gatekeeper.approvalThreshold")
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

    public Map<String, Map<String, Integer>> getApprovalPolicy(GatekeeperRdsRole role) {
        switch (role) {
            case DEV:
                return dev;
            case OPS:
                return ops;
            case DBA:
                return dba;
            case APPROVER:
                return new HashMap<>();
            default:
                return null;
        }
    }
}
