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

package org.finra.gatekeeper.services.aws.model;

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * Simple POJO used to pull what we need off AWS EC2 Instances.
 */
public class GatekeeperAWSInstance {
    private String instanceId;
    private String application;
    private String name;
    private String ip;
    private String ssmStatus;
    private String platform;

    public GatekeeperAWSInstance(String instanceId, String application, String name, String ip, String platform){
        this.instanceId = instanceId;
        this.application = application;
        this.name = name;
        this.ip = ip;
        this.ssmStatus = "Unknown";
        this.platform = platform;
    }

    public String getInstanceId(){
        return instanceId;
    }

    public String getApplication(){
        return application;
    }

    public String getName(){
        return name;
    }

    public String getIp() { return ip; }

    public String getSsmStatus() {return ssmStatus;}

    public String getPlatform(){ return  platform;}

    public GatekeeperAWSInstance setSsmStatus(String ssmStatus) {
        this.ssmStatus = ssmStatus;
        return this;
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        GatekeeperAWSInstance that = (GatekeeperAWSInstance)o;
        return Objects.equals(this.instanceId, that.getInstanceId())
                && Objects.equals(this.application, that.getApplication())
                && Objects.equals(this.name, that.getName())
                && Objects.equals(this.ip, that.getIp())
                && Objects.equals(this.ssmStatus, that.getSsmStatus())
                && Objects.equals(this.platform, that.getPlatform());
    }


    @Override
    public int hashCode() {
        return Objects.hash(instanceId, application, name, ip, ssmStatus, platform);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Instance ID", instanceId)
                .add("Application", application)
                .add("Name", name)
                .add("IP Address", ip)
                .add("SSM Status", ssmStatus)
                .add("Platform", platform)
                .toString();
    }
}
