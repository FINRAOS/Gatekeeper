package org.finra.gatekeeper.services.accessrequest.model.activerequest;

public class ActiveRequestUser {

    private String userId;
    private String gkUserId;
    private String email;
    private ActiveAccessConsolidated activeAccess;
    private ActiveAccessConsolidated expiredAccess;


    public String getUserId() {
        return userId;
    }

    public ActiveRequestUser setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getGkUserId() {
        return gkUserId;
    }

    public ActiveRequestUser setGkUserId(String gkUserId) {
        this.gkUserId = gkUserId;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ActiveRequestUser setEmail(String email) {
        this.email = email;
        return this;
    }


    public ActiveAccessConsolidated getActiveAccess() {
        return activeAccess;
    }

    public ActiveRequestUser setActiveAccess(ActiveAccessConsolidated activeAccess) {
        this.activeAccess = activeAccess;
        return this;
    }

    public ActiveAccessConsolidated getExpiredAccess() {
        return expiredAccess;
    }

    public ActiveRequestUser setExpiredAccess(ActiveAccessConsolidated expiredAccess) {
        this.expiredAccess = expiredAccess;
        return this;
    }
}
