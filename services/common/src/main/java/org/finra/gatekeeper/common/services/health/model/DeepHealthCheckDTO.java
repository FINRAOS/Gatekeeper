package org.finra.gatekeeper.common.services.health.model;

import java.util.List;

public class DeepHealthCheckDTO {
    private String component;
    private DeepHealthStatusDTO rollUpStatus;
    private List<DeepHealthCheckTargetDTO> dependencies;

    public String getComponent() {
        return component;
    }

    public DeepHealthCheckDTO setComponent(String component) {
        this.component = component;
        return this;
    }

    public DeepHealthStatusDTO getRollUpStatus() {
        return rollUpStatus;
    }

    public DeepHealthCheckDTO setRollUpStatus(DeepHealthStatusDTO rollUpStatus) {
        this.rollUpStatus = rollUpStatus;
        return this;
    }

    public List<DeepHealthCheckTargetDTO> getDependencies() {
        return dependencies;
    }

    public DeepHealthCheckDTO setDependencies(List<DeepHealthCheckTargetDTO> dependencies) {
        this.dependencies = dependencies;
        return this;
    }
}
