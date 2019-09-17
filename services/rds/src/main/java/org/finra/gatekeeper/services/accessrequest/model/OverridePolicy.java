package org.finra.gatekeeper.services.accessrequest.model;

import java.util.HashMap;
import java.util.Map;

public class OverridePolicy {

    private Map<String, Map<String, Integer>> overridePolicy;


    public OverridePolicy() {
        this.overridePolicy = new HashMap<>();
    }

    public OverridePolicy(Map<String, Map<String, Integer>> policy) {
        this.overridePolicy = policy;
    }


    public Map<String, Map<String, Integer>> getOverridePolicy() {
        return overridePolicy;
    }

    public OverridePolicy setOverridePolicy(Map<String, Map<String, Integer>> overridePolicy) {
        this.overridePolicy = overridePolicy;
        return this;
    }

    @Override
    public String toString() {
        return "OverridePolicy : { " +
                "overridePolicy : " + overridePolicy +
                " }";
    }
}
