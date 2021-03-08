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

package org.finra.gatekeeper.services.aws.model;

import com.google.common.base.MoreObjects;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Simple POJO used to pull what we need off AWS EC2 Instances.
 */
public class GatekeeperRDSInstance {
    private String instanceId;
    private String name;
    private String dbName;
    private String engine;
    private String status;
    private String arn;
    private String endpoint;
    private String application;
    private List<String> availableRoles;
    private DatabaseType databaseType;



    private Set<GatekeeperADGroupEntry> applicationRoles;
    private boolean enabled;

    public GatekeeperRDSInstance(String instanceId, String name, String dbName, String engine, String status, String arn, String endpoint,
                                 String application, List<String> availableRoles, Boolean enabled, DatabaseType databaseType, Set<GatekeeperADGroupEntry> applicationRoles){
        this.instanceId = instanceId;
        this.name = name;
        this.dbName = dbName;
        this.engine = engine;
        this.status = status;
        this.arn = arn;
        this.endpoint = endpoint;
        this.application = application;
        this.availableRoles = availableRoles;
        this.enabled = enabled;
        this.databaseType = databaseType;
        this.applicationRoles = applicationRoles;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public GatekeeperRDSInstance setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getName() {
        return name;
    }

    public GatekeeperRDSInstance setName(String name) {
        this.name = name;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public GatekeeperRDSInstance setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public String getEngine() {
        return engine;
    }

    public GatekeeperRDSInstance setEngine(String engine) {
        this.engine = engine;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public GatekeeperRDSInstance setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getArn() {
        return arn;
    }

    public GatekeeperRDSInstance setArn(String arn) {
        this.arn = arn;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public GatekeeperRDSInstance setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getApplication() {
        return application;
    }

    public GatekeeperRDSInstance setApplication(String application) {
        this.application = application;
        return this;
    }

    public List<String> getAvailableRoles() {
        return availableRoles;
    }

    public GatekeeperRDSInstance setAvailableRoles(List<String> availableRoles) {
        this.availableRoles = availableRoles;
        return this;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public GatekeeperRDSInstance setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public GatekeeperRDSInstance setDatabaseType(DatabaseType globalCluster) {
        this.databaseType = globalCluster;
        return this;
    }
    public Set<GatekeeperADGroupEntry> getApplicationRoles() {
        return applicationRoles;
    }

    public void setApplicationRoles(Set<GatekeeperADGroupEntry> applicationRoles) {
        this.applicationRoles = applicationRoles;
    }
    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        GatekeeperRDSInstance that = (GatekeeperRDSInstance)o;
        return Objects.equals(this.instanceId, that.getInstanceId())
                && Objects.equals(this.name, that.getName())
                && Objects.equals(this.dbName, that.getDbName())
                && Objects.equals(this.engine, that.getEngine())
                && Objects.equals(this.status, that.getStatus())
                && Objects.equals(this.arn, that.getArn())
                && Objects.equals(this.endpoint, that.getEndpoint())
                && Objects.equals(this.application, that.getApplication())
                && Objects.equals(this.availableRoles, that.getAvailableRoles())
                && Objects.equals(this.enabled, that.getEnabled())
                && Objects.equals(this.databaseType, that.getDatabaseType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId, name, dbName, engine, status, arn, endpoint, application, availableRoles, enabled, databaseType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Instance ID", instanceId)
                .add("Name", name)
                .add("Database Name", dbName)
                .add("Engine", engine)
                .add("Status", status)
                .add("ARN", arn)
                .add("Endpoint", endpoint)
                .add("Application", application)
                .add("Available Roles", availableRoles)
                .add("Enabled?", enabled)
                .add("Database Type", databaseType)
                .add("Application Groups", applicationRoles)
                .toString();
    }
}
