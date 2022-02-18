package org.finra.gatekeeper.common.services.health.model;

import org.finra.gatekeeper.common.services.health.model.enums.DeepHealthStopLight;

public class DeepHealthStatusDTO {
    private DeepHealthStopLight status;

    public DeepHealthStopLight getStatus() {
        return status;
    }

    public DeepHealthStatusDTO setStatus(DeepHealthStopLight status) {
        this.status = status;
        return this;
    }
}
