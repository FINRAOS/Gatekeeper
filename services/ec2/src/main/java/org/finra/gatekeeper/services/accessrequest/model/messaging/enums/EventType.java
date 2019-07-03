package org.finra.gatekeeper.services.accessrequest.model.messaging.enums;

public enum EventType {
    APPROVAL("approval"),
    EXPIRATION("expiration");

    private String value;

    EventType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
