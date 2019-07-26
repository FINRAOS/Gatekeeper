/*
 * Copyright 2018. Gatekeeper Contributors
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

package org.finra.gatekeeper.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix="gatekeeper")
public class GatekeeperSsmProperties {
    /**
     * The SSM Configuration properties per OS
     */

    private Integer ssmGrantRetryCount = 3;
    private Map<String, Map<String, SsmDocument>> ssm;

    public int getSsmGrantRetryCount() {
        return ssmGrantRetryCount;
    }

    public GatekeeperSsmProperties setSsmGrantRetryCount(int ssmGrantRetryCount) {
        this.ssmGrantRetryCount = ssmGrantRetryCount;
        return this;
    }

    public Map<String, Map<String, SsmDocument>> getSsm() {
        return ssm;
    }

    public GatekeeperSsmProperties setSsm(Map<String, Map<String, SsmDocument>> ssm) {
        this.ssm = ssm;
        return this;
    }

    public Map<String, SsmDocument> getPlatformDocuments(String platform) {
        return this.ssm.get(platform.toLowerCase());
    }

    public static class SsmDocument {
        /**
         * The name of the SSM Document
         */
        private String documentName;
        /**
         * The timeout in milliseconds for the document
         */
        private int timeout;
        /**
         * The interval to poll SSM for completion for this document
         */
        private int waitInterval;

        public String getDocumentName() {
            return documentName;
        }

        public SsmDocument setDocumentName(String documentName) {
            this.documentName = documentName;
            return this;
        }

        public int getTimeout() {
            return timeout;
        }

        public SsmDocument setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public int getWaitInterval() {
            return waitInterval;
        }

        public SsmDocument setWaitInterval(int waitInterval) {
            this.waitInterval = waitInterval;
            return this;
        }

    }
}
