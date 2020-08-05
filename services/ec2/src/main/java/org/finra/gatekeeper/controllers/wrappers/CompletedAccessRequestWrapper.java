
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

package org.finra.gatekeeper.controllers.wrappers;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.RequestStatus;

import java.util.Date;

/**
 * A wrapper class that makes Front End requests cleaner to work with.
 */
public class CompletedAccessRequestWrapper extends ActiveAccessRequestWrapper{

    public Integer getAttempts() {
        return attempts;
    }

    public CompletedAccessRequestWrapper setAttempts(Integer attempts) {
        this.attempts = attempts;
        return this;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public CompletedAccessRequestWrapper setStatus(RequestStatus status) {
        this.status = status;
        return this;
    }

    public Date getUpdated() {
        return updated;
    }

    public CompletedAccessRequestWrapper setUpdated(Date updated) {
        this.updated = updated;
        return this;
    }

    public String getActionedByUserId() {
        return actionedByUserId;
    }

    public CompletedAccessRequestWrapper setActionedByUserId(String actionedByUserId) {
        this.actionedByUserId = actionedByUserId;
        return this;
    }

    public String getActionedByUserName() {
        return actionedByUserName;
    }

    public CompletedAccessRequestWrapper setActionedByUserName(String actionedByUserName) {
        this.actionedByUserName = actionedByUserName;
        return this;
    }

    private Date updated;
    private Integer attempts;
    private RequestStatus status;
    @JsonAlias("actioned_by_user_id")
    private String actionedByUserId;
    @JsonAlias("actioned_by_user_name")
    private String actionedByUserName;

    public CompletedAccessRequestWrapper(AccessRequest accessRequest){
        super(accessRequest);
        this.setInstanceCount(accessRequest.getInstances().size())
                .setUserCount(accessRequest.getUsers().size());
    }

    public CompletedAccessRequestWrapper() {
        super();
    }
}
