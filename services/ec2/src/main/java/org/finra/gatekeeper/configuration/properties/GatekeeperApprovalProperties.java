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

package org.finra.gatekeeper.configuration.properties;

import org.finra.gatekeeper.services.auth.GatekeeperRole;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix="gatekeeper.approval-threshold")
public class GatekeeperApprovalProperties {
    /**
     * Thresholds for DEV Role
     */
    private Map<String, Integer> dev;

    /**
     * Thresholds for Support Role
     */
    private Map<String, Integer> support;

    public Map<String, Integer> getDev() {
        return dev;
    }

    public GatekeeperApprovalProperties setDev(Map<String, Integer> dev) {
        this.dev = dev;
        return this;
    }

    public Map<String, Integer> getSupport() {
        return support;
    }

    public GatekeeperApprovalProperties setSupport(Map<String, Integer> support) {
        this.support = support;
        return this;
    }


    public Map<String, Integer> getApprovalPolicy(GatekeeperRole role) {
        switch(role){
            case DEV:
                return dev;
            case SUPPORT:
                return support;
            default:
                return new HashMap<>();
        }
    }
}

