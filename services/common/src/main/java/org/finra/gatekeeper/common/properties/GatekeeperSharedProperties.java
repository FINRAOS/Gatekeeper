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

import java.util.Map;

@Component
@ConfigurationProperties(prefix="gatekeeper")
public class GatekeeperSharedProperties {

    private boolean explanationFieldRequired;

    private boolean ticketIdFieldRequired;

    private String ticketIdFieldMessage;


    public boolean isExplanationFieldRequired() {
        return explanationFieldRequired;
    }

    public GatekeeperSharedProperties setExplanationFieldRequired(boolean explanationFieldRequired) {
        this.explanationFieldRequired = explanationFieldRequired;
        return this;
    }

    public boolean isTicketIdFieldRequired() {
        return ticketIdFieldRequired;
    }

    public GatekeeperSharedProperties setTicketIdFieldRequired(boolean ticketIdFieldRequired) {
        this.ticketIdFieldRequired = ticketIdFieldRequired;
        return this;
    }

    public String getTicketIdFieldMessage() {
        return ticketIdFieldMessage;
    }

    public GatekeeperSharedProperties setTicketIdFieldMessage(String ticketIdFieldMessage) {
        this.ticketIdFieldMessage = ticketIdFieldMessage;
        return this;
    }
}
