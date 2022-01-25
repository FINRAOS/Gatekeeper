/*
 * Copyright 2022. Gatekeeper Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.finra.gatekeeper.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties relating to the sns service for gatekeeper
 */
@Component
@ConfigurationProperties(prefix="gatekeeper")
public class GatekeeperSnsProperties {

    private SnsProperties sns;

    public SnsProperties getSns() {
        return sns;
    }

    public void setSns(SnsProperties sns) {
        this.sns = sns;
    }

    public static class SnsProperties {
        private int retryCount = -1;
        private int retryIntervalMillis = -1;
        private int retryIntervalMultiplier = -1;
        private String approvalTopicARN;

        public String getApprovalTopicARN() { return approvalTopicARN; }

        public SnsProperties setApprovalTopicARN(String approvalTopicARN) {
            this.approvalTopicARN = approvalTopicARN;
            return this;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public int getRetryIntervalMillis() {
            return retryIntervalMillis;
        }

        public SnsProperties setRetryIntervalMillis(int retryIntervalMillis) {
            this.retryIntervalMillis = retryIntervalMillis;
            return this;
        }

        public int getRetryIntervalMultiplier() {
            return retryIntervalMultiplier;
        }

        public SnsProperties setRetryIntervalMultiplier(int retryIntervalMultiplier) {
            this.retryIntervalMultiplier = retryIntervalMultiplier;
            return this;
        }

        public SnsProperties setRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }
    }
}
