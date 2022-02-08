/*
 * Copyright 2022. Gatekeeper Contributors
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

package org.finra.gatekeeper.services.accessrequest.model;


/**
 * An Enum representing the Status for a Gatekeeper Request
 */
public enum RequestStatus {

    CANCELED("CANCELED", "The request was canceled by the requestor"),
    GRANTED("GRANTED", "No Approval Necessary, request has been successfully fulfilled"),
    ERROR("ERROR", "No Approval Necessary, but an error occurred trying to fulfill this request"),
    APPROVAL_REJECTED("APPROVAL_REJECTED", "Approval Necessary, Approver denied request"),
    APPROVAL_GRANTED("APPROVAL_GRANTED", "Approval Necessary, Admin granted request"),
    APPROVAL_PENDING("APPROVAL_PENDING", "Approval Necessary, Admin hasn't actioned request yet"),
    APPROVAL_ERROR("APPROVAL_ERROR", "Approval Necessary, Admin granted request but there was an error trying to fulfill the request");

    private final String value;
    private final String description;


    RequestStatus(String value, String description){
        this.value = value;
        this.description = description;
    }

    public String getValue(){
        return value;
    }
    public String getDescription(){
        return description;
    }

}
