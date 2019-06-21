package org.finra.gatekeeper.configuration.model;

import org.finra.gatekeeper.rds.model.RoleType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AppSpecificApprovalThreshold {

    private Map<RoleType, Map<String, Integer>> appSpecificApprovalThresholds;

    public AppSpecificApprovalThreshold() {
        this.appSpecificApprovalThresholds = new HashMap<>();
    }

    public AppSpecificApprovalThreshold(Map<RoleType, Map<String, Integer>> approvalThresholds) {
        this.appSpecificApprovalThresholds = approvalThresholds;
    }

    public Map<RoleType, Map<String, Integer>> getAppSpecificApprovalThresholds() {
        return appSpecificApprovalThresholds;
    }

    public void setAppSpecificApprovalThresholds(Map<RoleType, Map<String, Integer>> appSpecificApprovalThresholds) {
        this.appSpecificApprovalThresholds = appSpecificApprovalThresholds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppSpecificApprovalThreshold that = (AppSpecificApprovalThreshold) o;
        return Objects.equals(appSpecificApprovalThresholds, that.appSpecificApprovalThresholds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appSpecificApprovalThresholds);
    }

    @Override
    public String toString() {
        return "AppSpecificApprovalThreshold{" +
                "appSpecificApprovalThresholds=" + appSpecificApprovalThresholds +
                '}';
    }
}
