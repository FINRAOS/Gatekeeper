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

package org.finra.gatekeeper.common.services.properties;


import org.finra.gatekeeper.common.properties.GatekeeperSharedProperties;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GatekeeperPropertiesService {

    private final Logger logger = LoggerFactory.getLogger(AccountInformationService.class);

    private final GatekeeperSharedProperties gatekeeperSharedProperties;

    @Autowired
    public GatekeeperPropertiesService(GatekeeperSharedProperties gatekeeperSharedProperties) {
        this.gatekeeperSharedProperties = gatekeeperSharedProperties;
    }

    public Map<String, Object> getJustificationConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("explanationFieldRequired", gatekeeperSharedProperties.isExplanationFieldRequired());
        result.put("ticketIdFieldRequired", gatekeeperSharedProperties.isTicketIdFieldRequired());
        result.put("ticketIdFieldMessage", gatekeeperSharedProperties.getTicketIdFieldMessage());

        return result;
    }
}
