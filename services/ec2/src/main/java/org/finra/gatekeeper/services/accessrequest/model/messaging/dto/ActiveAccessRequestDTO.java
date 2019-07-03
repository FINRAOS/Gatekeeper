package org.finra.gatekeeper.services.accessrequest.model.messaging.dto;

import java.util.Objects;

public class ActiveAccessRequestDTO {
    private String requestId;
    private String name;
    private String ip;


    public ActiveAccessRequestDTO() {
    }

    public ActiveAccessRequestDTO(String requestId, String name, String ip) {
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


    @Override
    public String toString() {
        return "{ " +
                "requestId: '" + requestId + '\'' +
                ", name: '" + name + '\'' +
                ", ip: '" + ip + '\'' +
                " }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActiveAccessRequestDTO that = (ActiveAccessRequestDTO) o;
        return Objects.equals(requestId, that.requestId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, name, ip);
    }
}
