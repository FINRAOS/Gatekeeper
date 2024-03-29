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

package org.finra.gatekeeper.controllers.wrappers;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class that makes Front End requests cleaner to work with.
 */
public class AccessRequestWrapper {

    private Long id;
    private Integer days;
    @JsonAlias("requestor_id")
    private String requestorId;
    @JsonAlias("requestor_name")
    private String requestorName;
    @JsonAlias("requestor_email")
    private String requestorEmail;
    private String account;
    @JsonAlias("account_sdlc")
    private String accountSdlc;
    private String region;
    @JsonAlias("ticket_id")
    private String ticketId;
    @JsonAlias("request_reason")
    private String requestReason;
    @JsonAlias("approver_comments")
    private String approverComments;
    private List<User> users;
    private List<UserRole> roles;
    private List<AWSRdsDatabase> instances;

    public Long getId() {
        return id;
    }

    public AccessRequestWrapper setId(Long id) {
        this.id = id;
        return this;
    }

    public Integer getDays(){
        return days;
    }

    public AccessRequestWrapper setDays(Integer days){
        this.days = days;
        return this;
    }

    public String getAccount(){
        return account;
    }

    public AccessRequestWrapper setAccount(String account){
        this.account = account;
        return this;
    }

    public String getAccountSdlc() {
        return accountSdlc;
    }

    public AccessRequestWrapper setAccountSdlc(String accountSdlc) {
        this.accountSdlc = accountSdlc;
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
        return users != null? users : (this.users = new ArrayList<>());
    }

    public AccessRequestWrapper setUsers(List<User> users){
        this.users = users;
        return this;
    }

    public List<AWSRdsDatabase> getInstances(){
        return instances != null? instances : (this.instances = new ArrayList<>());
    }

    public AccessRequestWrapper setInstances(List<AWSRdsDatabase> instances){
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

    public String getTicketId() {
        return ticketId;
    }

    public AccessRequestWrapper setTicketId(String ticketId) {
        this.ticketId = ticketId;
        return this;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public AccessRequestWrapper setRequestReason(String requestReason) {
        this.requestReason = requestReason;
        return this;
    }

    public List<UserRole> getRoles() {
        return roles != null? roles : (this.roles = new ArrayList<>());
    }

    public AccessRequestWrapper setRoles(List<UserRole> roles) {
        this.roles = roles;
        return this;
    }
}
