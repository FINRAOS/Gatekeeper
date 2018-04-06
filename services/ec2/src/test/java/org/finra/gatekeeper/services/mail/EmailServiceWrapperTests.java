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
import org.finra.gatekeeper.services.email.wrappers.EmailServiceWrapper;
import org.finra.gatekeeper.services.keypairs.KeypairService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
public class EmailServiceWrapperTests {

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


    @Before
    public void initMocks() throws Exception {
        //Mocking out the method calls
        when(emailService.sendEmailWithAttachment(anyString(),anyString(),anyString(),anyString(),anyString(),anyMap(),anyString(),anyString(),anyMap(),anyString())).thenReturn(null);
        when(emailService.sendEmail(anyString(),anyString(),anyString(),anyString(),anyString(),anyMap())).thenReturn(null);

        //A mock user
        when(testUser.getEmail()).thenReturn("TestUser@finra.org");
        when(testUser.getUserId()).thenReturn("Test User");

        //The mock request
        List<User> users = new ArrayList<User>();
        users.add(testUser);
        when(request.getUsers()).thenReturn(users);
        when(request.getId()).thenReturn(125L);
        when(request.getAccount()).thenReturn("TEP");
        when(request.getRequestorEmail()).thenReturn("Request@finra.org");
        List<AWSInstance> instances = new ArrayList<AWSInstance>();

        when(instance.getIp()).thenReturn("127.0.0.1");
        instances.add(instance);
        when(request.getInstances()).thenReturn(instances);

        //Setting up the spring values
        when(emailProperties.getApproverEmails()).thenReturn("DL-ProductivityEng@finra.org");
        when(emailProperties.getFrom()).thenReturn("UNIT_FROM@finra.org");
        when(emailProperties.getTeam()).thenReturn("DL-UNIT_TEAM@finra.org");
        when(emailProperties.getOpsEmails()).thenReturn("DL-ProductivityOps@finra.org");
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
        verify(emailService, times(1)).sendEmailWithAttachment("DL-UNIT_TEAM@finra.org", "UNIT_FROM@finra.org", null, "Failure executing process", "failure", contentMap, "Exception.txt", "exception", param, "text/plain");



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

        mailServiceWrapper.notifyOfCredentials(request, testUser, privateKeyString,createStatus);
        verify(emailService, times(1)).sendEmailWithAttachment(testUser.getEmail(), "UNIT_FROM@finra.org", null, "Access Request " + request.getId() + " - Your temporary credential", "credentials", contentMap, "credential.pem", "privatekey", param, "application/x-pem-file");
        verify(emailService, times(1)).sendEmail("TestUser@finra.org", "UNIT_FROM@finra.org", null, "Access Request " + request.getId() + " - Your temporary username", "username", contentMap);

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
        mailServiceWrapper.notifyAdmins(request);
        verify(emailService, times(1)).sendEmail("DL-ProductivityEng@finra.org", "UNIT_FROM@finra.org", null, "GATEKEEPER: Access Requested", "accessRequested", contentMap);
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
        mailServiceWrapper.notifyExpired(request);
        verify(emailService, times(1)).sendEmail(testUser.getEmail(), "UNIT_FROM@finra.org", null, "Your Access has expired", "accessExpired", contentMap);
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
        mailServiceWrapper.notifyOps(request);
        verify(emailService, times(1)).sendEmail("DL-ProductivityOps@finra.org", "UNIT_FROM@finra.org",  "DL-UNIT_TEAM@finra.org", "GATEKEEPER: Manual revoke access for expired request", "manualRemoval", contentMap);
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
        mailServiceWrapper.notifyApproved(request);
        verify(emailService, times(1)).sendEmail("Request@finra.org", "UNIT_FROM@finra.org", null, "Access Request 125 was granted", "accessGranted", contentMap);
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
        mailServiceWrapper.notifyCanceled(request);
        verify(emailService, times(1)).sendEmail("DL-ProductivityEng@finra.org", "UNIT_FROM@finra.org", "Request@finra.org", "Access Request 125 was canceled", "requestCanceled", contentMap);
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
        mailServiceWrapper.notifyRejected(request);
        verify(emailService, times(1)).sendEmail("Request@finra.org", "UNIT_FROM@finra.org", null, "Access Request 125 was denied", "accessDenied", contentMap);
    }

    private String constructStackTrace(Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
