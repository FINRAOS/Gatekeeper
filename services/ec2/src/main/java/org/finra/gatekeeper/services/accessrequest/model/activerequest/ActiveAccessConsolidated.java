package org.finra.gatekeeper.services.accessrequest.model.activerequest;

import java.util.List;

public class ActiveAccessConsolidated {

    private List<ActiveAccessRequest> linux;
    private List<ActiveAccessRequest> windows;

    public ActiveAccessConsolidated() {
    }

    public ActiveAccessConsolidated(List<ActiveAccessRequest> linux, List<ActiveAccessRequest> windows) {
        this.linux = linux;
        this.windows = windows;
    }

    public List<ActiveAccessRequest> getLinux() {
        return linux;
    }

    public ActiveAccessConsolidated setLinux(List<ActiveAccessRequest> linux) {
        this.linux = linux;
        return this;
    }

    public List<ActiveAccessRequest> getWindows() {
        return windows;
    }

    public ActiveAccessConsolidated setWindows(List<ActiveAccessRequest> windows) {
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
