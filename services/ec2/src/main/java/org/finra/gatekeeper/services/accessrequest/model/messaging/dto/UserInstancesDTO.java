package org.finra.gatekeeper.services.accessrequest.model.messaging.dto;

import java.util.ArrayList;
import java.util.List;

public class UserInstancesDTO {

    private List<ActiveAccessRequestDTO> linux;
    private List<ActiveAccessRequestDTO> windows;

    public UserInstancesDTO() {
        this.linux = new ArrayList<>();
        this.windows = new ArrayList<>();
    }

    public UserInstancesDTO(List<ActiveAccessRequestDTO> linux, List<ActiveAccessRequestDTO> windows) {
        this.linux = linux;
        this.windows = windows;
    }

    public List<ActiveAccessRequestDTO> getLinux() {
        return linux;
    }

    public UserInstancesDTO setLinux(List<ActiveAccessRequestDTO> linux) {
        this.linux = linux;
        return this;
    }

    public List<ActiveAccessRequestDTO> getWindows() {
        return windows;
    }

    public UserInstancesDTO setWindows(List<ActiveAccessRequestDTO> windows) {
        this.windows = windows;
        return this;
    }

    @Override
    public String toString() {
        return "{ " +
                "linux: " + linux + ", " +
                "windows: " + windows +
                " }";
    }
}
