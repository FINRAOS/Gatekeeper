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

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o){
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        AccessRequest that = (AccessRequest) o;
        return Objects.equals(id, that.id)
                && Objects.equals(requestorId, that.requestorId)
                && Objects.equals(requestorName, that.requestorName)
                && Objects.equals(requestorEmail, that.requestorEmail)
                && Objects.equals(account, that.account)
                && Objects.equals(region, that.region)
                && Objects.equals(accountSdlc, that.accountSdlc)
                && Objects.equals(days, that.days)
                && Objects.equals(users, that.users)
                && Objects.equals(awsRdsDatabases, that.awsRdsDatabases)
                && Objects.equals(requestReason, that.requestReason)
                && Objects.equals(approverComments, that.approverComments);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, requestorId, requestorName, requestorEmail, account, region, accountSdlc, days, users, roles, awsRdsDatabases, requestReason, approverComments);
    }

    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("ID", id)
                .add("Requestor ID:", requestorId)
                .add("Requestor Name", requestorName)
                .add("Requestor Email", requestorEmail)
                .add("Account", account)
                .add("Region", region)
                .add("Account SDLC", accountSdlc)
                .add("Days", days)
                .add("Users", users)
                .add("Roles", roles)
                .add("RDS Databases", awsRdsDatabases)
                .add("Request Reason", requestReason)
                .add("Approver Comments", approverComments)
                .toString();
    }

    /**
     * Constructors
     */

    public AccessRequest() {}

    public AccessRequest(Integer days, String account, String region, String accountSdlc, String requestorId, String requestorName, String requestorEmail, List<UserRole> roles, List<User> users, List<AWSRdsDatabase> awsRdsDatabases, String requestReason, String approverComments) {
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
    }



}
