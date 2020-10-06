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

package org.finra.gatekeeper.services.mail;

import org.finra.gatekeeper.configuration.properties.GatekeeperEmailProperties;
import org.finra.gatekeeper.services.accessrequest.model.AWSInstance;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.email.EmailService;
import org.finra.gatekeeper.services.email.model.GatekeeperLinuxNotification;
import org.finra.gatekeeper.services.email.wrappers.EmailServiceWrapper;
import org.finra.gatekeeper.services.keypairs.KeypairService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.*;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Configuration
@ActiveProfiles("unit-test")
@RunWith(MockitoJUnitRunner.class)
public class EmailServiceWrapperTest {

    @InjectMocks
    private EmailServiceWrapper mailServiceWrapper;

    @Mock
    private EmailService emailService;

    @Mock
    private GatekeeperEmailProperties emailProperties;


    @Mock
    private User testUser;
    @Mock
    private AccessRequest request;

    @Mock
    private AWSInstance instance;

    private String testUserEmail = "TestUser@company.com";
    private String testUserId = "testuser";
    private String approverEmail = "GKApprover@company.com";
    private String fromEmail = "UNIT_FROM@company.com";
    private String teamEmail = "DL-UNIT_TEAM@company.com";
    private String opsEmail = "GKOps@company.com";
    private String requestEmail = "Request@company.com";

    @Before
    public void initMocks() throws Exception {
        //Mocking out the method calls
        when(emailService.sendEmail(anyString(),anyString(),anyString(),anyString(),anyString(),anyMap())).thenReturn(null);

        //A mock user
        when(testUser.getEmail()).thenReturn(testUserEmail);

        //The mock request
        List<User> users = new ArrayList<User>();
        users.add(testUser);
        when(request.getUsers()).thenReturn(users);
        when(request.getId()).thenReturn(125L);
        when(request.getRequestorEmail()).thenReturn(requestEmail);
        List<AWSInstance> instances = new ArrayList<AWSInstance>();

        instances.add(instance);
        when(request.getInstances()).thenReturn(instances);

        //Setting up the spring values
        when(emailProperties.getApproverEmails()).thenReturn(approverEmail);
        when(emailProperties.getFrom()).thenReturn(fromEmail);
        when(emailProperties.getTeam()).thenReturn(teamEmail);
        when(emailProperties.getOpsEmails()).thenReturn(opsEmail);
    }


    /**
     * Test for the notifyAdminsOfFailure method
     * @throws Exception
     */
    @Test
    public void testNotifyAdminsOfFailure() throws Exception {
        Throwable exception = new Throwable();
        StackTraceElement[] stackTraceElements = new StackTraceElement[]{new StackTraceElement("test.class", "testMethod",
                "testFileName", 22)};
        exception.setStackTrace(stackTraceElements);
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", request);
        Map<String, Object> param = new HashMap<>();
        param.put("stacktrace", constructStackTrace(exception));

        mailServiceWrapper.notifyAdminsOfFailure(request, exception);
        verify(emailService, times(1)).sendEmailWithAttachment(teamEmail, fromEmail, null, "Failure executing process", "failure", contentMap, "Exception.txt", "exception", param, "text/plain");



    }


    /**
     * Test for the notifyOfCredentials method
     * @throws Exception
     */

    @Test
    public void testNotifyOfCredentials() throws Exception {
        Map<String,String> createStatus = new HashMap<>();
        createStatus.put("instance1", "String1");
        createStatus.put("instance2", "String2");

        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", request);
        contentMap.put("user", testUser);
        contentMap.put("instanceStatus", createStatus);

        KeypairService keypairService = new KeypairService();
        KeyPair keypair = keypairService.createKeypair();
        PrivateKey privateKey = keypair.getPrivate();
        Map<String, Object> param = new HashMap<String, Object>();
        String privateKeyString = keypairService.getPEM(privateKey);
        param.put("privatekey", privateKeyString);

        mailServiceWrapper.notifyOfCredentials(request, new GatekeeperLinuxNotification()
                .setUser(testUser)
                .setKey(privateKeyString)
                .setCreateStatus(createStatus));
        verify(emailService, times(1)).sendEmailWithAttachment(testUser.getEmail(), fromEmail, null, "Access Request " + request.getId() + " - Your temporary credential", "credentials", contentMap, "credential.pem", "privatekey", param, "application/x-pem-file");
        contentMap.put("approverDL", approverEmail);
        verify(emailService, times(1)).sendEmail(testUserEmail, fromEmail, null, "Access Request " + request.getId() + " - Your temporary username", "username", contentMap);

    }


    /**
     * Test for the notifyAdmins method
     * @throws Exception
     */
    @Test
    public void testNotifyAdmins() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", request);
        contentMap.put("user", null);
        contentMap.put("approverDL", approverEmail);
        mailServiceWrapper.notifyAdmins(request);
        verify(emailService, times(1)).sendEmail(approverEmail, fromEmail, null, "GATEKEEPER: Access Requested ("+request.getId()+")", "accessRequested", contentMap);
    }

    /**
     * Test for the notifyExpired method
     * @throws Exception
     */
    @Test
    public void testNotifyExpired() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", request);
        contentMap.put("user", null);
        contentMap.put("approverDL", approverEmail);
        mailServiceWrapper.notifyExpired(request);
        verify(emailService, times(1)).sendEmail(testUser.getEmail(), fromEmail, null, "Your Access has expired", "accessExpired", contentMap);
    }

    /**
     * Test for the notifyOps method
     * @throws Exception
     */
    @Test
    public void testNotifyOps() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", request);
        contentMap.put("offlineInstances", Arrays.asList(new AWSInstance[]{instance}));
        contentMap.put("user", null);
        contentMap.put("approverDL", approverEmail);
        mailServiceWrapper.notifyOps(request);
        verify(emailService, times(1)).sendEmail(opsEmail, fromEmail,  teamEmail, "GATEKEEPER: Manual revoke access for expired request", "manualRemoval", contentMap);
    }

    /**
     * Test for the notifyApproved method
     * @throws Exception
     */
    @Test
    public void testNotifyApproved() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", request);
        contentMap.put("user", null);
        contentMap.put("approverDL", approverEmail);
        mailServiceWrapper.notifyApproved(request);
        verify(emailService, times(1)).sendEmail(requestEmail, fromEmail, null, "Access Request 125 was granted", "accessGranted", contentMap);
    }

    /**
     * Test for the notifyCanceled method
     * @throws Exception
     */
    @Test
    public void testNotifyCanceled() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", request);
        contentMap.put("user", null);
        contentMap.put("approverDL", approverEmail);
        mailServiceWrapper.notifyCanceled(request);
        verify(emailService, times(1)).sendEmail(approverEmail, fromEmail, requestEmail, "Access Request 125 was canceled", "requestCanceled", contentMap);
    }


    /**
     * Test for the notifyRejected method
     * @throws Exception
     */
    @Test
    public void testNotifyRejected() throws Exception {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("request", request);
        contentMap.put("user", null);
        contentMap.put("approverDL", approverEmail);
        mailServiceWrapper.notifyRejected(request);
        verify(emailService, times(1)).sendEmail(requestEmail, fromEmail, null, "Access Request 125 was denied", "accessDenied", contentMap);
    }

    private String constructStackTrace(Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
