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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="gatekeeper.email")
public class GatekeeperEmailProperties {
    /**
     * The Email addresses (or Distribution List) for the approvers
     */
    private String approverEmails;

    /**
     * The Email addresses (or Distribution List) for ops team members
     */
    private String opsEmails;

    /**
     * The address that the email is sent from
     */
    private String from;

    /**
     * The team managing Gatekeeper
     */
    private String team;

    public String getApproverEmails() {
        return approverEmails;
    }

    public GatekeeperEmailProperties setApproverEmails(String approverEmails) {
        this.approverEmails = approverEmails;
        return this;
    }

    public String getOpsEmails() {
        return opsEmails;
    }

    public GatekeeperEmailProperties setOpsEmails(String opsEmails) {
        this.opsEmails = opsEmails;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public GatekeeperEmailProperties setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTeam() {
        return team;
    }

    public GatekeeperEmailProperties setTeam(String team) {
        this.team = team;
        return this;
    }
}
