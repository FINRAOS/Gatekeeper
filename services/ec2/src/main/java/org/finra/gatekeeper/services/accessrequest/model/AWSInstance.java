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
import java.util.Objects;

/**
 * Object model for an AWS Instance for gatekeeper
 */
@Entity
@Table(name = "request_instance")
public class AWSInstance {

    private Long id;
    private String name;
    private String application;
    private String instanceId;
    private String ip;
    private String status;
    private String platform;

    @ManyToOne
    private AccessRequest accessRequest;

    /**
     * Setters and Getters for ID
     */

    @Id
    @GeneratedValue
    public Long getId(){
        return id;
    }

    AWSInstance setId(Long id){
        this.id = id;
        return this;
    }

    /**
     * Setters and Getters for name
     */

    public String getName(){
        return name;
    }

    public AWSInstance setName(String name){
        this.name = name;
        return this;
    }

    /**
     * Setters and Getters for application
     */

    public String getApplication(){
        return application;
    }

    public AWSInstance setApplication(String application){
        this.application = application;
        return this;
    }

    /**
     * Setters and Getters for instanceId
     */

    public String getInstanceId(){
        return instanceId;
    }

    public AWSInstance setInstanceId(String instanceId){
        this.instanceId = instanceId;
        return this;
    }

    /**
     * Setters and getters for IP
     */

    public String getIp(){
        return ip;
    }

    public AWSInstance setIp(String ip){
        this.ip = ip;
        return this;
    }

    /**
     * Setters and getters for State
     */

    public String getStatus(){
        return status;
    }

    public AWSInstance setStatus(String status){
        this.status = status;
        return this;
    }


    /**
     * Setters and getters for Platform
     */

    public String getPlatform(){
        return platform;
    }

    public AWSInstance setPlatform(String platform){
        this.platform = platform;
        return this;
    }
    /**
     * Constructors
     */
    public AWSInstance() {}

    public AWSInstance(String name, String application, String instanceId, String ip, String status, String platform) {
        this.name = name;
        this.application = application;
        this.instanceId = instanceId;
        this.ip = ip;
        this.status = status;
        this.platform = platform;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        AWSInstance that = (AWSInstance) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(application, that.application)
                && Objects.equals(instanceId, that.instanceId)
                && Objects.equals(ip, that.ip)
                && Objects.equals(status, that.status)
                && Objects.equals(platform, that.platform);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, application, name, instanceId, ip, status,platform);
    }

    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("ID", id)
                .add("Instance Application", application)
                .add("Instance Name", name)
                .add("Instance ID", instanceId)
                .add("Instance IP", ip)
                .add("Status", status)
                .add("Platform", platform)
                .toString();
    }
}
