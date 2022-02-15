package org.finra.gatekeeper.common.services.health.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.finra.gatekeeper.common.services.health.model.enums.DeepHealthCheckTargetStatus;
import org.finra.gatekeeper.common.services.health.model.enums.DependencyCriticality;

import java.util.HashMap;
import java.util.Map;

public class DeepHealthCheckTargetDTO {

    private String application;
    private String component;
    private String description;
    private String category;
    private String uri;
    private DeepHealthCheckTargetStatus status;
    private DependencyCriticality dependencyType;
    private Map<String, Object> data = new HashMap<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String exceptionMessage;
    private String startTimestamp;
    private String endTimestamp;

    public String getApplication() {
        return application;
    }

    public DeepHealthCheckTargetDTO setApplication(String application) {
        this.application = application;
        return this;
    }

    public String getComponent() {
        return component;
    }

    public DeepHealthCheckTargetDTO setComponent(String component) {
        this.component = component;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DeepHealthCheckTargetDTO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public DeepHealthCheckTargetDTO setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public DeepHealthCheckTargetDTO setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public DeepHealthCheckTargetStatus getStatus() {
        return status;
    }

    public DeepHealthCheckTargetDTO setStatus(DeepHealthCheckTargetStatus status) {
        this.status = status;
        return this;
    }

    public DependencyCriticality getDependencyType() {
        return dependencyType;
    }

    public DeepHealthCheckTargetDTO setDependencyType(DependencyCriticality dependencyType) {
        this.dependencyType = dependencyType;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public DeepHealthCheckTargetDTO setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public DeepHealthCheckTargetDTO setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public DeepHealthCheckTargetDTO setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
        return this;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

    public DeepHealthCheckTargetDTO setEndTimestamp(String endTimestamp) {
        this.endTimestamp = endTimestamp;
        return this;
    }
}
