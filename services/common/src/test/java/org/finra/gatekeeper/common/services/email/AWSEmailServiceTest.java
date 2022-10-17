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

package org.finra.gatekeeper.common.services.email;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import freemarker.template.Template;
import org.finra.gatekeeper.common.services.aws.AwsSesSessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

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
public class AWSEmailServiceTest {

    private AWSEmailService awsEmailService;

    @Mock
    private AwsSesSessionService awsSesSessionService;

    @Mock
    private AmazonSimpleEmailService ses;

    @Mock
    private freemarker.template.Configuration freemarkerConfig;

    @Captor
    ArgumentCaptor<SendRawEmailRequest> sendRawEmailRequestCapture;

    @Before
    public void initMocks() throws Exception {
        //Mocking out the method calls
        when(ses.sendRawEmail(any(SendRawEmailRequest.class))).thenReturn(null);
        Template template = Template.getPlainTextTemplate("test", "<html>A test.</html>", new freemarker.template.Configuration());
        when(freemarkerConfig.getTemplate(anyString())).thenReturn(template);
        when(awsSesSessionService.getSES()).thenReturn(ses);
        awsEmailService = new AWSEmailService(freemarkerConfig, awsSesSessionService);
    }

    @Test
    public void testSendEmailWithNoAttachmentsWithCC() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", "requestContent");
        awsEmailService.sendEmail("toUser", "fromEmail", "cc", "subject", "testTemplate", contentMap);
        verify(ses, times(1)).sendRawEmail(sendRawEmailRequestCapture.capture());
        SendRawEmailRequest request = sendRawEmailRequestCapture.getValue();
        String stringRequest = Charset.defaultCharset().decode(request.getRawMessage().getData()).toString();
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
        awsEmailService.sendEmail("toUser", "fromEmail", null, "subject", "testTemplate", contentMap);
        verify(ses, times(1)).sendRawEmail(sendRawEmailRequestCapture.capture());
        SendRawEmailRequest request = sendRawEmailRequestCapture.getValue();
        String stringRequest = Charset.defaultCharset().decode(request.getRawMessage().getData()).toString();
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
        awsEmailService.sendEmailWithAttachment("toUser", "fromEmail", "cc", "subject", "testTemplate", contentMap, "test.txt", "testTemplate", attachmentMap, "text/plain");
        verify(ses, times(1)).sendRawEmail(sendRawEmailRequestCapture.capture());
        SendRawEmailRequest request = sendRawEmailRequestCapture.getValue();
        String stringRequest = Charset.defaultCharset().decode(request.getRawMessage().getData()).toString();
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
        awsEmailService.sendEmailWithAttachment("toUser", "fromEmail", null, "subject", "testTemplate", contentMap, "test.txt", "testTemplate", attachmentMap, "text/plain");
        verify(ses, times(1)).sendRawEmail(sendRawEmailRequestCapture.capture());
        SendRawEmailRequest request = sendRawEmailRequestCapture.getValue();
        String stringRequest = Charset.defaultCharset().decode(request.getRawMessage().getData()).toString();
        assertThat(stringRequest).contains("From: fromEmail");
        assertThat(stringRequest).contains("To: toUser");
        assertThat(stringRequest).doesNotContain("Cc: cc");
        assertThat(stringRequest).contains("Subject: subject");
        assertThat(stringRequest).contains("<html>A test.</html>");
        assertThat(stringRequest).contains("filename=test.txt");
        assertThat(stringRequest).contains("Content-Type: text/plain");
    }
}
