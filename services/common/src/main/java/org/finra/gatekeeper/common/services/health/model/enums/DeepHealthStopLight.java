package org.finra.gatekeeper.common.services.health.model.enums;

public enum DeepHealthStopLight {
    GREEN("GREEN"), RED("RED"), YELLOW("YELLOW");

    private final String name;

    private DeepHealthStopLight(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
