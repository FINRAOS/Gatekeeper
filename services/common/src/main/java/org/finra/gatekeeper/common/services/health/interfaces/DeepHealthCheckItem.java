package org.finra.gatekeeper.common.services.health.interfaces;

import org.finra.gatekeeper.common.services.health.model.DeepHealthCheckTargetDTO;

public interface DeepHealthCheckItem {
    public DeepHealthCheckTargetDTO doHealthCheck();
}
