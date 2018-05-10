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

package org.finra.gatekeeper.services.accessrequest.model;


import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * The Domain Model object for an Access Request
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
    private String approverComments;
    private String approverUserId;
    private String approverUserName;
    private String requestReason;
    private String platform;
    private Integer hours;
    private List<User> users;
    private List<AWSInstance> instances;

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
     * Getters / Setters for Platform
     */
    public String getPlatform(){
        return platform;
    }

    public AccessRequest setPlatform(String platform){
        this.platform = platform;
        return this;
    }

    /**
     * Getters / Setters for Hours
     */

    public Integer getHours(){
        return hours;
    }

    public AccessRequest setHours(Integer hours){
        this.hours = hours;
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
    public List<AWSInstance> getInstances(){
        return instances;
    }

    public AccessRequest setInstances(List<AWSInstance> instances){
        this.instances = instances;
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

    public String getApproverUserId() {
        return approverUserId;
    }

    public AccessRequest setApproverUserId(String approverUserId) {
        this.approverUserId = approverUserId;
        return this;
    }

    public String getApproverUserName() {
        return approverUserName;
    }

    public AccessRequest setApproverUserName(String approverUserName) {
        this.approverUserName = approverUserName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessRequest that = (AccessRequest) o;
        return com.google.common.base.Objects.equal(id, that.id) &&
                com.google.common.base.Objects.equal(requestorId, that.requestorId) &&
                com.google.common.base.Objects.equal(requestorName, that.requestorName) &&
                com.google.common.base.Objects.equal(requestorEmail, that.requestorEmail) &&
                com.google.common.base.Objects.equal(account, that.account) &&
                com.google.common.base.Objects.equal(region, that.region) &&
                com.google.common.base.Objects.equal(approverComments, that.approverComments) &&
                com.google.common.base.Objects.equal(approverUserId, that.approverUserId) &&
                com.google.common.base.Objects.equal(approverUserName, that.approverUserName) &&
                com.google.common.base.Objects.equal(requestReason, that.requestReason) &&
                com.google.common.base.Objects.equal(platform, that.platform) &&
                com.google.common.base.Objects.equal(hours, that.hours) &&
                com.google.common.base.Objects.equal(users, that.users) &&
                com.google.common.base.Objects.equal(instances, that.instances);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(id, requestorId, requestorName, requestorEmail, account, region, approverComments, approverUserId, approverUserName, requestReason, platform, hours, users, instances);
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
                .add("Hours", hours)
                .add("Users", users)
                .add("Instances", instances)
                .add("Request Reason", requestReason)
                .add("Approver Comments", approverComments)
                .add("Approver ID", approverUserId)
                .add("Approver Name", approverUserName)
                .add("Platform", platform)
                .toString();
    }

    /**
     * Constructors
     */

    public AccessRequest() {}

    public AccessRequest(Integer hours,
                         String account,
                         String region,
                         String requestorId,
                         String requestorName,
                         String requestorEmail,
                         List<User> users,
                         List<AWSInstance> instances,
                         String requestReason,
                         String approverComments,
                         String approverUserId,
                         String approverUserName,
                         String platform) {
        this.hours = hours;
        this.region = region;
        this.account = account;
        this.requestorId = requestorId;
        this.requestorName = requestorName;
        this.requestorEmail = requestorEmail;
        this.users = users;
        this.instances = instances;
        this.requestReason = requestReason;
        this.approverComments = approverComments;
        this.approverUserId = approverUserId;
        this.approverUserName = approverUserName;
        this.platform = platform;
    }



}
