package org.finra.gatekeeper.configuration.model;

import org.finra.gatekeeper.rds.model.RoleType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AppSpecificOverridePolicy {

    private Map<RoleType, Map<String, Integer>> appSpecificOverridePolicy;

    public Map<RoleType, Map<String, Integer>> getAppSpecificOverridePolicy() {
        return appSpecificOverridePolicy;
    }

    public void setAppSpecificOverridePolicy(Map<RoleType, Map<String, Integer>> appSpecificOverridePolicy) {
        this.appSpecificOverridePolicy = appSpecificOverridePolicy;
    }

    public AppSpecificOverridePolicy() {
        this.appSpecificOverridePolicy = new HashMap<>();
    }

    public AppSpecificOverridePolicy(Map<RoleType, Map<String, Integer>> appSpecificOverridePolicy) {
        this.appSpecificOverridePolicy = appSpecificOverridePolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppSpecificOverridePolicy that = (AppSpecificOverridePolicy) o;
        return Objects.equals(appSpecificOverridePolicy, that.appSpecificOverridePolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appSpecificOverridePolicy);
    }

    @Override
    public String toString() {
        return "AppSpecificOverridePolicy{" +
                "appSpecificOverridePolicy=" + appSpecificOverridePolicy +
                '}';
    }
}
