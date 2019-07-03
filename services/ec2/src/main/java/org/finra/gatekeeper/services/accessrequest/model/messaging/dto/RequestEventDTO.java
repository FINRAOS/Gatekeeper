package org.finra.gatekeeper.services.accessrequest.model.messaging.dto;

import java.util.List;

public class RequestEventDTO {
    private Long requestId;
    private String eventType;
    private List<ActiveRequestUserDTO> users;

    public Long getRequestId() {
        return requestId;
    }

    public RequestEventDTO setRequestId(Long requestId) {
        this.requestId = requestId;
        return this;
    }

    public String getEventType() {
        return eventType;
    }

    public RequestEventDTO setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public List<ActiveRequestUserDTO> getUsers() {
        return users;
    }

    public RequestEventDTO setUsers(List<ActiveRequestUserDTO> users) {
        this.users = users;
        return this;
    }
}
