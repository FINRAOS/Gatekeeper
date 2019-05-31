package org.finra.gatekeeper.services.accessrequest.model.activerequest;

public class ActiveAccessRequest {
    private String requestId;
    private String name;
    private String ip;


    public ActiveAccessRequest() {
    }

    public ActiveAccessRequest(String requestId, String name, String ip) {
        this.requestId = requestId;
        this.name = name;
        this.ip = ip;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
