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

package org.finra.gatekeeper.common.properties;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.finra.gatekeeper.common.services.email.JavaEmailService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@ConfigurationProperties(prefix="gatekeeper.email")
public class GatekeeperEmailProperties {
    @Bean
    public Configuration freemarkerConfig() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_29);
        configuration.setClassForTemplateLoading(JavaEmailService.class, "/emails");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return configuration;
    }

    /**
     * The Email addresses (or Distribution List) for the approvers
     */
    private String approverEmails;

    /**
     * The Email addresses (or Distribution List) for ops team members
     */
    private String opsEmails;

    /**
     * The address that the email is sent from
     */
    private String from;

    /**
     * The team managing Gatekeeper
     */
    private String team;


    /**
     * Whether or not to send Access Requested emails
     */
    private boolean sendAccessRequestedEmail;


    /**
     * Whether or not use use AWS Simple Email Service
     */
    private boolean useSES;

    public GatekeeperEmailProperties() {
    }


    /**
    * The Disclaimer for making a change to a request displayed at the bottom of the email.
    */
    private String changeDisclaimer;



    public String getApproverEmails() {
        return approverEmails;
    }

    public GatekeeperEmailProperties setApproverEmails(String approverEmails) {
        this.approverEmails = approverEmails;
        return this;
    }

    public String getOpsEmails() {
        return opsEmails;
    }

    public GatekeeperEmailProperties setOpsEmails(String opsEmails) {
        this.opsEmails = opsEmails;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public GatekeeperEmailProperties setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTeam() {
        return team;
    }

    public GatekeeperEmailProperties setTeam(String team) {
        this.team = team;
        return this;
    }

    public boolean isSendAccessRequestedEmail() {
        return sendAccessRequestedEmail;
    }

    public GatekeeperEmailProperties setSendAccessRequestedEmail(boolean sendAccessRequestedEmail) {
        this.sendAccessRequestedEmail = sendAccessRequestedEmail;
        return this;
    }
    public boolean isUseSES() {
        return useSES;
    }

    public GatekeeperEmailProperties setUseSES(boolean useSES) {
        this.useSES = useSES;
        return this;
    }

    public String getChangeDisclaimer() {
        return changeDisclaimer;
    }

    public GatekeeperEmailProperties setChangeDisclaimer(String changeDisclaimer) {
        this.changeDisclaimer = changeDisclaimer;
        return this;
    }
}
