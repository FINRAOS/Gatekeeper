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

import org.finra.gatekeeper.services.accessrequest.model.AWSInstance;
import org.finra.gatekeeper.services.accessrequest.model.User;

import javax.persistence.Access;
import java.util.List;

/**
 * A wrapper class that makes Front End requests cleaner to work with.
 */
public class AccessRequestWrapper {

    private Long id;
    private Integer hours;
    private String requestorId;
    private String requestorName;
    private String requestorEmail;
    private String account;
    private String region;
    private String requestReason;
    private String ticketId;
    private String approverComments;
    private String platform;
    private List<User> users;
    private List<AWSInstance> instances;

    public Long getId() {
        return id;
    }

    public AccessRequestWrapper setId(Long id) {
        this.id = id;
        return this;
    }

    public Integer getHours(){
        return hours;
    }

    public AccessRequestWrapper setHours(Integer hours){
        this.hours = hours;
        return this;
    }

    public String getAccount(){
        return account;
    }

    public AccessRequestWrapper setAccount(String account){
        this.account = account;
        return this;
    }

    public String getRegion(){
        return region;
    }

    public AccessRequestWrapper setRegion(String region){
        this.region = region;
        return this;
    }

    public List<User> getUsers(){
        return users;
    }

    public AccessRequestWrapper setUsers(List<User> users){
        this.users = users;
        return this;
    }

    public List<AWSInstance> getInstances(){
        return instances;
    }

    public AccessRequestWrapper setInstances(List<AWSInstance> instances){
        this.instances = instances;
        return this;
    }


    public String getRequestorEmail() {
        return requestorEmail;
    }

    public AccessRequestWrapper setRequestorEmail(String requestorEmail) {
        this.requestorEmail = requestorEmail;
        return this;
    }

    public String getRequestorName() {
        return requestorName;
    }

    public AccessRequestWrapper setRequestorName(String requestorName) {
        this.requestorName = requestorName;
        return this;
    }

    public String getRequestorId() {
        return requestorId;
    }

    public AccessRequestWrapper setRequestorId(String requestorId) {
        this.requestorId = requestorId;
        return this;
    }

    public String getApproverComments() { return approverComments; }

    public AccessRequestWrapper setApproverComments(String approverComments) {
        this.approverComments = approverComments;
        return this;
    }


    public String getRequestReason() {
        return requestReason;
    }

    public AccessRequestWrapper setRequestReason(String requestReason) {
        this.requestReason = requestReason;
        return this;
    }

    public String getTicketId() {
        return ticketId;
    }

    public AccessRequestWrapper setTicketId(String ticketId) {
        this.ticketId = ticketId;
        return this;
    }

    public String getPlatform(){return platform;}

    public AccessRequestWrapper setPlatform(String platform){
        this.platform=platform;
        return this;
    }


}
