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

package org.finra.gatekeeper.services.accessrequest.model.response;

/**
 * Some Generic wrapper response class for the angular frontend to eat up.
 */
public class AccessRequestCreationResponse {
    private AccessRequestCreationOutcome outcome;
    private Object response;

    public AccessRequestCreationOutcome getOutcome() {
        return outcome;
    }

    public AccessRequestCreationResponse setOutcome(AccessRequestCreationOutcome outcome) {
        this.outcome = outcome;
        return this;
    }

    public Object getResponse() {
        return response;
    }

    public AccessRequestCreationResponse setResponse(Object response) {
        this.response = response;
        return this;
    }

    public AccessRequestCreationResponse(AccessRequestCreationOutcome outcome, Object response){
        this.outcome = outcome;
        this.response = response;
    }
}
