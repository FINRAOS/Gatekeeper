package org.finra.gatekeeper.controllers.wrappers;

import org.finra.gatekeeper.services.accessrequest.model.OverridePolicy;
import org.finra.gatekeeper.services.auth.model.AppApprovalThreshold;
import org.finra.gatekeeper.services.auth.model.RoleMembership;

import java.util.HashMap;
import java.util.Map;

public class GetRoleResponseWrapper {
    private String userId;
    private String name;
    private Boolean isApprover;
    private Map<String, RoleMembership> roleMemberships;
    private String email;
    private Integer maxDays;
    private Map<String, AppApprovalThreshold> approvalThreshold;
    private Map<String, Map<String, Integer>> overridePolicy;


    public GetRoleResponseWrapper() {
        roleMemberships = new HashMap<>();
        approvalThreshold = new HashMap<>();
    }

    public String getUserId() {
        return userId;
    }

    public GetRoleResponseWrapper setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public GetRoleResponseWrapper setName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getApprover() {
        return isApprover;
    }

    public GetRoleResponseWrapper setApprover(Boolean approver) {
        isApprover = approver;
        return this;
    }

    public Map<String, RoleMembership> getRoleMemberships() {
        return roleMemberships;
    }

    public GetRoleResponseWrapper setRoleMemberships(Map<String, RoleMembership> roleMemberships) {
        this.roleMemberships = roleMemberships;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public GetRoleResponseWrapper setEmail(String email) {
        this.email = email;
        return this;
    }

    public Integer getMaxDays() {
        return maxDays;
    }

    public GetRoleResponseWrapper setMaxDays(Integer maxDays) {
        this.maxDays = maxDays;
        return this;
    }

    public Map<String, AppApprovalThreshold> getApprovalThreshold() {
        return approvalThreshold;
    }

    public GetRoleResponseWrapper setApprovalThreshold(Map<String, AppApprovalThreshold> approvalThreshold) {
        this.approvalThreshold = approvalThreshold;
        return this;
    }

    public Map<String, Map<String, Integer>> getOverridePolicy() {
        return overridePolicy;
    }

    public GetRoleResponseWrapper setOverridePolicy(Map<String, Map<String, Integer>> overridePolicy) {
        this.overridePolicy = overridePolicy;
        return this;
    }
}
