package org.finra.gatekeeper.services.accessrequest.model.activerequest;

import org.finra.gatekeeper.services.accessrequest.model.User;

import java.util.Objects;

public class UserNoId {

    private String name;
    private String email;
    private String userId;

    public UserNoId() {
    }

    public UserNoId(String name, String email, String userId) {
        this.name = name;
        this.email = email;
        this.userId = userId;
    }

    public UserNoId(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.userId = user.getUserId();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserNoId userNoId = (UserNoId) o;
        return Objects.equals(userId, userNoId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "UserNoId{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
