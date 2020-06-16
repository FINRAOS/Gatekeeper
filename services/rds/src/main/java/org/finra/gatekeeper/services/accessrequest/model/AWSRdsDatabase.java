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
import org.finra.gatekeeper.services.aws.model.DatabaseType;

import javax.persistence.*;
import java.util.Objects;

/**
 * Domain Representation for an AWS RDS Instance that is tied to a gatekeeper request
 */
@Entity
@Table(name = "request_database")
public class AWSRdsDatabase {

    private Long id;
    private String name;
    private String application;
    private String instanceId;
    private String dbName;
    private String engine;
    private String endpoint;
    private String arn;
    private String status;
    private DatabaseType databaseType;

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

    AWSRdsDatabase setId(Long id){
        this.id = id;
        return this;
    }

    /**
     * Setters and Getters for name
     */

    public String getName(){
        return name;
    }

    public AWSRdsDatabase setName(String name){
        this.name = name;
        return this;
    }

    /**
     * Setters and Getters for name
     */

    public String getDbName(){
        return dbName;
    }

    public AWSRdsDatabase setDbName(String dbName){
        this.dbName = dbName;
        return this;
    }

    /**
     * Setters and Getters for account
     */

    public String getApplication(){
        return application;
    }

    public AWSRdsDatabase setApplication(String application){
        this.application = application;
        return this;
    }

    /**
     * Setters and Getters for instanceId
     */

    public String getInstanceId(){
        return instanceId;
    }

    public AWSRdsDatabase setInstanceId(String instanceId){
        this.instanceId = instanceId;
        return this;
    }

    /**
     * Setters and getters for Engine
     */

    public String getEngine(){
        return engine;
    }

    public AWSRdsDatabase setEngine(String engine){
        this.engine = engine;
        return this;
    }


    /**
     * Setters and getters for IP
     */

    public String getEndpoint(){
        return endpoint;
    }

    public AWSRdsDatabase setEndpoint(String endpoint){
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Setters and getters for IP
     */

    public String getArn(){
        return arn;
    }

    public AWSRdsDatabase setArn(String arn){
        this.arn = arn;
        return this;
    }

    /**
     * Setters and getters for State
     */

    public String getStatus(){
        return status;
    }

    public AWSRdsDatabase setStatus(String status){
        this.status = status;
        return this;
    }

    @Enumerated(EnumType.STRING)
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public AWSRdsDatabase setDatabaseType(DatabaseType global) {
        this.databaseType = global;
        return this;
    }

    /**
     * Constructors
     */
    public AWSRdsDatabase() {}

    public AWSRdsDatabase(String name, String dbName, String application, String instanceId, String engine, String endpoint, String arn, String status, DatabaseType databaseType) {
        this.name = name;
        this.dbName = dbName;
        this.application = application;
        this.instanceId = instanceId;
        this.engine = engine;
        this.endpoint = endpoint;
        this.arn = arn;
        this.status = status;
        this.databaseType = databaseType;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        AWSRdsDatabase that = (AWSRdsDatabase) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(dbName, that.dbName)
                && Objects.equals(application, that.application)
                && Objects.equals(engine, that.engine)
                && Objects.equals(instanceId, that.instanceId)
                && Objects.equals(endpoint, that.endpoint)
                && Objects.equals(status, that.status)
                && Objects.equals(endpoint, that.endpoint)
                && Objects.equals(databaseType, that.databaseType);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, application, name, dbName, instanceId, engine, endpoint, status, databaseType);
    }

    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("ID", id)
                .add("RDS Database Application", application)
                .add("RDS Database Instance Name", name)
                .add("RDS Database Name", dbName)
                .add("RDS Database ID", instanceId)
                .add("RDS Database Engine", engine)
                .add("RDS Database Endpoint", endpoint)
                .add("RDS Database ARN", arn)
                .add("Status", status)
                .add("Database Type", databaseType)
                .toString();
    }
}
