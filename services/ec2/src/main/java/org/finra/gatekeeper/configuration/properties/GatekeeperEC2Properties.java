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
@ConfigurationProperties(prefix="gatekeeper")
public class GatekeeperEC2Properties {
    /**
     * The tag that identifies the application that owns the instance
     */
    private String appIdentityTag;

    private boolean explanationFieldRequired;

    private boolean ticketIdFieldRequired;

    private String ticketIdFieldMessage;

    public String getAppIdentityTag() {
        return appIdentityTag;
    }

    public GatekeeperEC2Properties setAppIdentityTag(String appIdentityTag) {
        this.appIdentityTag = appIdentityTag;
        return this;
    }

    public boolean isExplanationFieldRequired() {
        return explanationFieldRequired;
    }

    public GatekeeperEC2Properties setExplanationFieldRequired(boolean explanationFieldRequired) {
        this.explanationFieldRequired = explanationFieldRequired;
        return this;
    }

    public boolean isTicketIdFieldRequired() {
        return ticketIdFieldRequired;
    }

    public GatekeeperEC2Properties setTicketIdFieldRequired(boolean ticketIdFieldRequired) {
        this.ticketIdFieldRequired = ticketIdFieldRequired;
        return this;
    }

    public String getTicketIdFieldMessage() {
        return ticketIdFieldMessage;
    }

    public GatekeeperEC2Properties setTicketIdFieldMessage(String ticketIdFieldMessage) {
        this.ticketIdFieldMessage = ticketIdFieldMessage;
        return this;
    }
}
