package org.finra.gatekeeper.services.auth.model;

import java.util.HashMap;
import java.util.Map;

public class GetRoleResponseDTO {
    private String userId;
    private String name;
    private Boolean isApprover;
    private Map<String, RoleMembership> roleMemberships;
    private String email;
    private Map<String, Map<String, Map<String, Integer>>> approvalThreshold;
    private Integer maxDays;
    private Map<String, Map<String, Map<String, Integer>>> overridePolicy;

    public GetRoleResponseDTO() {
        roleMemberships = new HashMap<>();
        approvalThreshold = new HashMap<>();
        overridePolicy = new HashMap<>();
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isApprover() {
        return isApprover;
    }

    public void setApprover(Boolean approver) {
        isApprover = approver;
    }

    public Map<String, RoleMembership> getRoleMemberships() {
        return roleMemberships;
    }

    public void setRoleMemberships(Map<String, RoleMembership> roleMemberships) {
        this.roleMemberships = roleMemberships;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Map<String, Map<String, Integer>>> getApprovalThreshold() {
        return approvalThreshold;
    }

    public void setApprovalThreshold(Map<String, Map<String, Map<String, Integer>>> approvalThreshold) {
        this.approvalThreshold = approvalThreshold;
    }

    public Integer getMaxDays() {
        return maxDays;
    }

    public void setMaxDays(Integer maxDays) {
        this.maxDays = maxDays;
    }

    public Map<String, Map<String, Map<String, Integer>>> getOverridePolicy() {
        return overridePolicy;
    }

    public void setOverridePolicy(Map<String, Map<String, Map<String, Integer>>> overridePolicy) {
        this.overridePolicy = overridePolicy;
    }


    @Override
    public String toString() {
        return "GetRoleResponseDTO{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", isApprover=" + isApprover +
                ", roleMemberships=" + roleMemberships +
                ", email='" + email + '\'' +
                ", approvalThreshold=" + approvalThreshold +
                ", maxDays=" + maxDays +
                ", overridePolicy=" + overridePolicy +
                '}';
    }
}
