package org.finra.gatekeeper.services.accessrequest.model.activerequest;

import java.util.List;

public class ActiveRequestData {

    private String requestId;
    private String eventType;
    private List<ActiveRequestUser> users;


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public List<ActiveRequestUser> getUsers() {
        return users;
    }

    public void setUsers(List<ActiveRequestUser> users) {
        this.users = users;
    }
}
