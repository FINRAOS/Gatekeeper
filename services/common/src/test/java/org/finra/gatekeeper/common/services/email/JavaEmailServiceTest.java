/*
 * Copyright 2022. Gatekeeper Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenjavaMailSender/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.finra.gatekeeper.common.services.email;

import freemarker.template.Template;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


@Configuration
@ActiveProfiles("unit-test")
@RunWith(MockitoJUnitRunner.class)
public class JavaEmailServiceTest {

    @InjectMocks
    private JavaEmailService javaEmailService;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private freemarker.template.Configuration freemarkerConfig;

    @Captor
    ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    @Before
    public void initMocks() throws Exception {
        //Mocking out the method calls
        when(javaMailSender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
        doNothing().when(javaMailSender).send((MimeMessage) any());
        Template template = Template.getPlainTextTemplate("test", "<html>A test.</html>", new freemarker.template.Configuration());
        when(freemarkerConfig.getTemplate(anyString())).thenReturn(template);
    }

    @Test
    public void testSendEmailWithNoAttachmentsWithCC() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", "requestContent");
        javaEmailService.sendEmail("toUser", "fromEmail", "cc", "subject", "testTemplate", contentMap);
        verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());
        String stringRequest = mimeMessageToString(mimeMessageCaptor.getValue());
        assertThat(stringRequest).contains("From: fromEmail");
        assertThat(stringRequest).contains("To: toUser");
        assertThat(stringRequest).contains("Cc: cc");
        assertThat(stringRequest).contains("Subject: subject");
        assertThat(stringRequest).contains("<html>A test.</html>");
    }

    @Test
    public void testSendEmailWithNoAttachmentsWithNoCC() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", "requestContent");
        javaEmailService.sendEmail("toUser", "fromEmail", null, "subject", "testTemplate", contentMap);
        verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());
        String stringRequest = mimeMessageToString(mimeMessageCaptor.getValue());
        assertThat(stringRequest).contains("From: fromEmail");
        assertThat(stringRequest).contains("To: toUser");
        assertThat(stringRequest).doesNotContain("Cc: cc");
        assertThat(stringRequest).contains("Subject: subject");
        assertThat(stringRequest).contains("<html>A test.</html>");
    }

    @Test
    public void testSendEmailWithAttachmentsWithCC() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", "requestContent");
        Map<String,Object> attachmentMap = new HashMap<>();
        attachmentMap.put("param", "test");
        javaEmailService.sendEmailWithAttachment("toUser", "fromEmail", "cc", "subject", "testTemplate", contentMap, "test.txt", "testTemplate", attachmentMap, "text/plain");
        verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());
        String stringRequest = mimeMessageToString(mimeMessageCaptor.getValue());
        assertThat(stringRequest).contains("From: fromEmail");
        assertThat(stringRequest).contains("To: toUser");
        assertThat(stringRequest).contains("Cc: cc");
        assertThat(stringRequest).contains("Subject: subject");
        assertThat(stringRequest).contains("<html>A test.</html>");
        assertThat(stringRequest).contains("filename=test.txt");
        assertThat(stringRequest).contains("Content-Type: text/plain");
    }

    @Test
    public void testSendEmailWithAttachmentsWithNoCC() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", "requestContent");
        Map<String,Object> attachmentMap = new HashMap<>();
        attachmentMap.put("param", "test");
        javaEmailService.sendEmailWithAttachment("toUser", "fromEmail", null, "subject", "testTemplate", contentMap, "test.txt", "testTemplate", attachmentMap, "text/plain");
        verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());
        String stringRequest = mimeMessageToString(mimeMessageCaptor.getValue());
        assertThat(stringRequest).contains("From: fromEmail");
        assertThat(stringRequest).contains("To: toUser");
        assertThat(stringRequest).doesNotContain("Cc: cc");
        assertThat(stringRequest).contains("Subject: subject");
        assertThat(stringRequest).contains("<html>A test.</html>");
        assertThat(stringRequest).contains("filename=test.txt");
        assertThat(stringRequest).contains("Content-Type: text/plain");
    }

    private String mimeMessageToString(MimeMessage message) throws IOException, MessagingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);
        return Charset.defaultCharset().decode(ByteBuffer.wrap(outputStream.toByteArray())).toString();
    }
}
