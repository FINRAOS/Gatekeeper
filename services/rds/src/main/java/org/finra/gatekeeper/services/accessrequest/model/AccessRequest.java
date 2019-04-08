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

package org.finra.gatekeeper.services.accessrequest.model;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.List;

/**
 * Domain Object for a Gatekeeper Access Request
 */
@Entity
@Table(name = "access_request")
public class AccessRequest {

    private Long id;
    private String requestorId;
    private String requestorName;
    private String requestorEmail;
    private String account;
    private String region;
    private String accountSdlc;
    private String approverComments;
    private String actionedByUserId;
    private String actionedByUserName;
    private String ticketId;
    private String requestReason;
    private Integer days;
    private List<User> users;
    private List<UserRole> roles;
    private List<AWSRdsDatabase> awsRdsDatabases;

    /**
     * Getters / Setters for ID
     */

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public AccessRequest setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Getters / Setters for requestorId
     */
    public String getRequestorId(){
        return requestorId;
    }

    public AccessRequest setRequestorId(String requestorId){
        this.requestorId = requestorId;
        return this;
    }

    /**
     * Getters / Setters for requestorName
     */
    public String getRequestorName(){
        return requestorName;
    }

    public AccessRequest setRequestorName(String requestorName){
        this.requestorName = requestorName;
        return this;
    }

    /**
     * Getters / Setters for requestorEmail
     */
    public String getRequestorEmail(){
        return requestorEmail;
    }

    public AccessRequest setRequestorEmail(String requestorEmail){
        this.requestorEmail = requestorEmail;
        return this;
    }

    /**
     * Getters / Setters for Account
     */
    public String getAccount(){
        return account;
    }

    public AccessRequest setAccount(String account){
        this.account = account;
        return this;
    }

    /**
     * Getters / Setters for Region
     */
    public String getRegion(){
        return region;
    }

    public AccessRequest setRegion(String region){
        this.region = region;
        return this;
    }

    /**
     * Getters / Setters for Account SDLC
     * @return
     */
    public String getAccountSdlc() {
        return accountSdlc;
    }

    public AccessRequest setAccountSdlc(String accountSdlc) {
        this.accountSdlc = accountSdlc;
        return this;
    }


    /**
     * Getters / Setters for Approver Comments
     */
    @Column(length = 5000)
    public String getApproverComments(){
        return approverComments;
    }

    public AccessRequest setApproverComments(String approverComments){
        this.approverComments = approverComments;
        return this;
    }

    /**
     * Getters / Setters for Days
     */

    public Integer getDays(){
        return days;
    }

    public AccessRequest setDays(Integer days){
        this.days = days;
        return this;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<UserRole> getRoles() {
        return roles;
    }

    public AccessRequest setRoles(List<UserRole> roles) {
        this.roles = roles;
        return this;
    }

    /**
     * Getters / Setters for Users
     */

    @OneToMany(cascade = CascadeType.ALL)
    public List<User> getUsers(){
        return users;
    }

    public AccessRequest setUsers(List<User> users){
        this.users = users;
        return this;
    }

    /**
     * Getters / Setters for Instances
     */

    @OneToMany(cascade = CascadeType.ALL)
    public List<AWSRdsDatabase> getAwsRdsInstances(){
        return awsRdsDatabases;
    }

    public AccessRequest setAwsRdsInstances(List<AWSRdsDatabase> awsRdsDatabases){
        this.awsRdsDatabases = awsRdsDatabases;
        return this;
    }

    @Column(length = 100)
    public String getTicketId() {
        return ticketId;
    }

    public AccessRequest setTicketId(String ticketId) {
        this.ticketId = ticketId;
        return this;
    }

    /**
     * Getters / Setters for requestReason
     */
    @Column(length = 5000)
     public String getRequestReason(){
        return requestReason;
    }

    public AccessRequest setRequestReason(String requestReason){
        this.requestReason = requestReason;
        return this;
    }

    public String getActionedByUserId() {
        return actionedByUserId;
    }

    public AccessRequest setActionedByUserId(String actionedByUserId) {
        this.actionedByUserId = actionedByUserId;
        return this;
    }

    public String getActionedByUserName() {
        return actionedByUserName;
    }

    public AccessRequest setActionedByUserName(String actionedByUserName) {
        this.actionedByUserName = actionedByUserName;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessRequest that = (AccessRequest) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(requestorId, that.requestorId) &&
                Objects.equal(requestorName, that.requestorName) &&
                Objects.equal(requestorEmail, that.requestorEmail) &&
                Objects.equal(account, that.account) &&
                Objects.equal(region, that.region) &&
                Objects.equal(accountSdlc, that.accountSdlc) &&
                Objects.equal(approverComments, that.approverComments) &&
                Objects.equal(actionedByUserId, that.actionedByUserId) &&
                Objects.equal(actionedByUserName, that.actionedByUserName) &&
                Objects.equal(ticketId, that.ticketId) &&
                Objects.equal(requestReason, that.requestReason) &&
                Objects.equal(days, that.days) &&
                Objects.equal(users, that.users) &&
                Objects.equal(roles, that.roles) &&
                Objects.equal(awsRdsDatabases, that.awsRdsDatabases);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, requestorId, requestorName, requestorEmail, account, region, accountSdlc, approverComments, actionedByUserId, actionedByUserName, ticketId, requestReason, days, users, roles, awsRdsDatabases);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("requestorId", requestorId)
                .add("requestorName", requestorName)
                .add("requestorEmail", requestorEmail)
                .add("account", account)
                .add("region", region)
                .add("accountSdlc", accountSdlc)
                .add("approverComments", approverComments)
                .add("actionedByUserId", actionedByUserId)
                .add("actionedByUserName", actionedByUserName)
                .add("requestReason", requestReason)
                .add("ticketId", ticketId)
                .add("days", days)
                .add("users", users)
                .add("roles", roles)
                .add("awsRdsDatabases", awsRdsDatabases)
                .toString();
    }

    /**
     * Constructors
     */

    public AccessRequest() {}

    public AccessRequest(Integer days,
                         String account,
                         String region,
                         String accountSdlc,
                         String requestorId,
                         String requestorName,
                         String requestorEmail,
                         List<UserRole> roles,
                         List<User> users,
                         List<AWSRdsDatabase> awsRdsDatabases,
                         String requestReason,
                         String approverComments,
                         String actionedByUserId,
                         String actionedByUserName) {
        this.days = days;
        this.region = region;
        this.account = account;
        this.accountSdlc = accountSdlc;
        this.requestorId = requestorId;
        this.requestorName = requestorName;
        this.requestorEmail = requestorEmail;
        this.users = users;
        this.roles = roles;
        this.awsRdsDatabases= awsRdsDatabases;
        this.requestReason = requestReason;
        this.approverComments = approverComments;
        this.actionedByUserId = actionedByUserId;
        this.actionedByUserName = actionedByUserName;
    }



}
