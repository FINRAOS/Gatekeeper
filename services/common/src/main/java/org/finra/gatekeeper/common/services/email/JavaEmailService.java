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
 *
 */

package org.finra.gatekeeper.common.services.email;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Service for email message related activity
 */
@Component
public class JavaEmailService {

    private final Logger logger = LoggerFactory.getLogger(org.finra.gatekeeper.common.services.email.JavaEmailService.class);
    private final String charset = "US-ASCII";


    private final JavaMailSender javaMailSender;
    private final Configuration freemarkerConfig;

    @Autowired
    public JavaEmailService(JavaMailSender javaMailSender,
                            Configuration freemarkerConfig) {
        this.javaMailSender = javaMailSender;
        this.freemarkerConfig = freemarkerConfig;
    }

    private String generateEmailMessage(String template, Map params) throws Exception {
        Template templateObj = freemarkerConfig.getTemplate(template + ".ftl");

        Writer stringWriter = new StringWriter();
        String result;

        try {
            templateObj.process(params, stringWriter);
            result = stringWriter.toString();
        } finally {
            stringWriter.close();
        }
        return result;
    }

    private MimeMessage generateMimeMessage(String to, String from, String cc, String subject) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, charset);
        InternetAddress fAddress = new InternetAddress(from);
        message.setTo(InternetAddress.parse(to, true));
        if (cc != null) {
            message.setCc(InternetAddress.parse(cc, true));
        }
        message.setFrom(fAddress);
        message.setSubject(subject);
        return mimeMessage;
    }

    /**
     * Generates and sends an email
     *
     * @param to           - Comma separated list of users to send email
     * @param cc           - Comma separated list of users to cc
     * @param emailSubject - String containing subject of email
     * @param template     - Template name for email
     * @param contentMap   - Key-value pairing used by template
     * @return - the mimeMessage object
     * @throws Exception
     */
    public MimeMessage sendEmail(String to, String from, String cc, String emailSubject, String template, Map<String, Object> contentMap) throws Exception {
        return sendEmailWithAttachment(to, from, cc, emailSubject, template, contentMap, null, null, null, null);
    }

    /**
     * Generates and sends an email
     *
     * @param to                 - Comma separated list of users to send email
     * @param cc                 - Comma separated list of users to cc
     * @param emailSubject       - String containing subject of email
     * @param template           - Template name for email
     * @param contentMap         - Key-value pairing used by template
     * @param attachmentName     - Name of attachment
     * @param attachmentTemplate - Template name for email attachment
     * @param attachmentMap      - Key-value pairing used by attachment template
     * @return - the mimeMessage object
     * @throws Exception
     */
    public MimeMessage sendEmailWithAttachment(String to, String from, String cc, String emailSubject, String template, Map<String, Object> contentMap, String attachmentName, String attachmentTemplate, Map<String, Object> attachmentMap, String mimeType) throws Exception {
        logger.info("Sending email to " + to + " with subject " + emailSubject + " and message: " + template);
        MimeMessage mimeMessage = generateMimeMessage(to, from, cc, emailSubject);
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, charset);
        message.setText(generateEmailMessage(template, contentMap), true);
        if (attachmentName != null) {
            DataSource data = new ByteArrayDataSource(generateEmailMessage("attachments/"+attachmentTemplate, attachmentMap), mimeType);
            message.addAttachment(attachmentName, data);
        }
        javaMailSender.send(mimeMessage);
        return mimeMessage;
    }
}
