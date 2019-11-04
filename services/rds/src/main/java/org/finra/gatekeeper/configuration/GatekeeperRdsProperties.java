package org.finra.gatekeeper.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties relating to the RDS service for gatekeeper
 */
@Component
@ConfigurationProperties(prefix="gatekeeper.rds")
public class GatekeeperRdsProperties {

    private int retryCount = -1;
    private int retryIntervalMillis = -1;
    private int retryIntervalMultiplier = -1;
    private String snsApprovalTopic;

    public String getSnsApprovalTopic() {
        return snsApprovalTopic;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getRetryIntervalMillis() {
        return retryIntervalMillis;
    }

    public int getRetryIntervalMultiplier() {
        return retryIntervalMultiplier;
    }

    public GatekeeperRdsProperties setRetryIntervalMillis(int retryIntervalMillis) {
        this.retryIntervalMillis = retryIntervalMillis;
        return this;
    }

    public GatekeeperRdsProperties setRetryIntervalMultiplier(int retryIntervalMultiplier) {
        this.retryIntervalMultiplier = retryIntervalMultiplier;
        return this;
    }

    public GatekeeperRdsProperties setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public GatekeeperRdsProperties setSnsApprovalTopic(String snsApprovalTopic) {
        this.snsApprovalTopic = snsApprovalTopic;
        return this;
    }
}
