package org.finra.gatekeeper.services.accessrequest.model.messaging.dto;

public class ActiveRequestUserDTO {

    private String userId;
    private String gkUserId;
    private String email;
    private UserInstancesDTO activeAccess;
    private UserInstancesDTO expiredAccess;

    public ActiveRequestUserDTO(){
        this.activeAccess = new UserInstancesDTO();
        this.expiredAccess = new UserInstancesDTO();
    }

    public String getUserId() {
        return userId;
    }

    public ActiveRequestUserDTO setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getGkUserId() {
        return gkUserId;
    }

    public ActiveRequestUserDTO setGkUserId(String gkUserId) {
        this.gkUserId = gkUserId;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ActiveRequestUserDTO setEmail(String email) {
        this.email = email;
        return this;
    }


    public UserInstancesDTO getActiveAccess() {
        return activeAccess;
    }

    public ActiveRequestUserDTO setActiveAccess(UserInstancesDTO activeAccess) {
        this.activeAccess = activeAccess;
        return this;
    }

    public UserInstancesDTO getExpiredAccess() {
        return expiredAccess;
    }

    public ActiveRequestUserDTO setExpiredAccess(UserInstancesDTO expiredAccess) {
        this.expiredAccess = expiredAccess;
        return this;
    }


    @Override
    public String toString() {
        return "{ " +
                "userId: '" + userId + '\'' +
                ", gkUserId: '" + gkUserId + '\'' +
                ", email: '" + email + '\'' +
                ", activeAccess: " + activeAccess +
                ", expiredAccess: " + expiredAccess +
                " }";
    }
}
