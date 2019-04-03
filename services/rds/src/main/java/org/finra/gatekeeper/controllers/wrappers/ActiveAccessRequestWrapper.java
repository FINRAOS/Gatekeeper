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

package org.finra.gatekeeper.controllers.wrappers;

import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;

import java.util.Date;

/**
 * A wrapper class that makes Front End requests cleaner to work with.
 */
public class ActiveAccessRequestWrapper extends AccessRequestWrapper {

    private Integer userCount;
    private Integer instanceCount;
    private String taskId;
    private Date created;

    public Integer getUserCount() {
        return userCount;
    }

    public ActiveAccessRequestWrapper setUserCount(Integer userCount) {
        this.userCount = userCount;
        return this;
    }

    public Integer getInstanceCount() {
        return instanceCount;
    }

    public ActiveAccessRequestWrapper setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public ActiveAccessRequestWrapper setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }



    public Date getCreated() {
        return created;
    }

    public ActiveAccessRequestWrapper setCreated(Date created) {
        this.created = created;
        return this;
    }



    public ActiveAccessRequestWrapper(){

    }

    public ActiveAccessRequestWrapper(AccessRequest accessRequest){
        super();
        this.setRegion(accessRequest.getRegion())
                .setId(accessRequest.getId())
                .setAccount(accessRequest.getAccount())
                .setAccountSdlc(accessRequest.getAccountSdlc())
                .setDays(accessRequest.getDays())
                .setInstances(accessRequest.getAwsRdsInstances())
                .setRoles(accessRequest.getRoles())
                .setUsers(accessRequest.getUsers())
                .setRequestorId(accessRequest.getRequestorId())
                .setRequestorName(accessRequest.getRequestorName())
                .setRequestorEmail(accessRequest.getRequestorEmail())
                .setApproverComments(accessRequest.getApproverComments())
                .setTicketId(accessRequest.getTicketId())
                .setRequestReason(accessRequest.getRequestReason());
    }



}
