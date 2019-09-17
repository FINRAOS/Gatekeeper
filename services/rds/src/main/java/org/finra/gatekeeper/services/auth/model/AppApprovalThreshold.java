package org.finra.gatekeeper.services.auth.model;

import org.finra.gatekeeper.rds.model.RoleType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AppApprovalThreshold {
    private Map<RoleType, Map<String, Integer>> appApprovalThresholds;

    public AppApprovalThreshold() {
        this.appApprovalThresholds = new HashMap<>();
    }

    public AppApprovalThreshold(Map<RoleType, Map<String, Integer>> appApprovalThresholds) {
        this.appApprovalThresholds = appApprovalThresholds;
    }

    public Map<RoleType, Map<String, Integer>> getAppApprovalThresholds() {
        return appApprovalThresholds;
    }

    public AppApprovalThreshold setAppApprovalThresholds(Map<RoleType, Map<String, Integer>> appApprovalThresholds) {
        this.appApprovalThresholds = appApprovalThresholds;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppApprovalThreshold that = (AppApprovalThreshold) o;
        return Objects.equals(appApprovalThresholds, that.appApprovalThresholds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appApprovalThresholds);
    }

    @Override
    public String toString() {
        return "AppApprovalThreshold{" +
                "appApprovalThresholds=" + appApprovalThresholds +
                '}';
    }
}
