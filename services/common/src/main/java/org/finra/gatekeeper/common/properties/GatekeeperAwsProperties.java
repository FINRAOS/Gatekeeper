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

package org.finra.gatekeeper.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="gatekeeper.aws")
public class GatekeeperAwsProperties {
    /**
     * The host for the proxy EX: myproxy.company.com
     */
    private String proxyHost;
    /**
     * The port for the proxy to go through EX: 8080
     */
    private String proxyPort;
    /**
     * The timeout for each AWS Session created by the app (in milliseconds)
     */
    private Integer sessionTimeout;
    /**
     * The pad to add to prevent a session from expiring mid-use
     */
    private Integer sessionTimeoutPad;
    /**
     * The Role to Assume
     */
    private String roleToAssume;

    public String getProxyHost() {
        return proxyHost;
    }

    public GatekeeperAwsProperties setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public GatekeeperAwsProperties setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public GatekeeperAwsProperties setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public Integer getSessionTimeoutPad() {
        return sessionTimeoutPad;
    }

    public GatekeeperAwsProperties setSessionTimeoutPad(Integer sessionTimeoutPad) {
        this.sessionTimeoutPad = sessionTimeoutPad;
        return this;
    }

    public String getRoleToAssume() {
        return roleToAssume;
    }

    public GatekeeperAwsProperties setRoleToAssume(String roleToAssume) {
        this.roleToAssume = roleToAssume;
        return this;
    }
}
