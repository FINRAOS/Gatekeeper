package org.finra.gatekeeper.common.properties;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@EnableAutoConfiguration
@ConditionalOnProperty(value="gatekeeper.health.tagValue")
@ConfigurationProperties(prefix="gatekeeper.health")
public class GatekeeperHealthProperties {

    private String tagLabel;
    private String tagValue;
    private String componentName;
    private Set<String> components;
    private String membershipsTag;
    private String databaseTag;
    private String membershipsComponent;
    private String databaseComponent;

    public String getTagLabel() {
        return tagLabel;
    }

    public GatekeeperHealthProperties setTagLabel(String tagLabel) {
        this.tagLabel = tagLabel;
        return this;
    }

    public String getTagValue() {
        return tagValue;
    }

    public GatekeeperHealthProperties setTagValue(String tagValue) {
        this.tagValue = tagValue;
        return this;
    }

    public String getComponentName() {
        return componentName;
    }

    public GatekeeperHealthProperties setComponentName(String componentName) {
        this.componentName = componentName;
        return this;
    }

    public Set<String> getComponents() {
        return components;
    }

    public GatekeeperHealthProperties setComponents(String components) {
        this.components = new HashSet<>();
        this.components.addAll(Arrays.stream(
                components.split(","))
                .map(String::trim)
                .collect(Collectors.toList()));
        return this;
    }
    public String getMembershipsTag() {
        return membershipsTag;
    }

    public GatekeeperHealthProperties setMembershipsTag(String membershipsTag) {
        this.membershipsTag = membershipsTag;
        return this;
    }

    public String getDatabaseTag() {
        return databaseTag;
    }

    public GatekeeperHealthProperties setDatabaseTag(String databaseTag) {
        this.databaseTag = databaseTag;
        return this;
    }

    public String getMembershipsComponent() {
        return membershipsComponent;
    }

    public GatekeeperHealthProperties setMembershipsComponent(String membershipsComponent) {
        this.membershipsComponent = membershipsComponent;
        return this;
    }

    public String getDatabaseComponent() {
        return databaseComponent;
    }

    public GatekeeperHealthProperties setDatabaseComponent(String databaseComponent) {
        this.databaseComponent = databaseComponent;
        return this;
    }
}
