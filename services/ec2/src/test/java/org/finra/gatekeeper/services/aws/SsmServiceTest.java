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

package org.finra.gatekeeper.services.aws;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClient;
import com.amazonaws.services.simplesystemsmanagement.model.*;
import org.finra.gatekeeper.configuration.properties.GatekeeperEmailProperties;
import org.finra.gatekeeper.configuration.properties.GatekeeperSsmProperties;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.accessrequest.model.AWSInstance;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests for AWS SSM Service
 */
@Configuration
@ActiveProfiles("unit-test")
@RunWith(MockitoJUnitRunner.class)
public class SsmServiceTest {
    @Mock
    private AWSSimpleSystemsManagementClient awsSimpleSystemsManagementClient;

    @Mock
    private AwsSessionService awsSessionService;

    @Mock
    private DescribeInstanceInformationResult firstDescribeInstanceInformationResult;

    @Mock
    private DescribeInstanceInformationResult secondDescribeInstanceInformationResult;

    @Mock
    private InstanceInformation firstInstanceInformation;

    @Mock
    private InstanceInformation secondInstanceInformation;

    @Mock
    private GatekeeperSsmProperties ssmDocuments;

    @Mock
    private GatekeeperEmailProperties email;

    @Mock
    private User user;

    @Mock
    private AccessRequest accessRequest;

    @InjectMocks
    private SsmService ssmService;

    private AWSEnvironment awsEnvironment = new AWSEnvironment("Dev", "us-west-2");



    @Before
    public void before() {
        String fakeCommandId = "fake";
        SendCommandResult fakeResult = new SendCommandResult();
        Command fakeCommand = new Command();
        fakeCommand.setCommandId(fakeCommandId);
        fakeResult.setCommand(fakeCommand);

        ListCommandInvocationsResult fakeCommandList = new ListCommandInvocationsResult();
        CommandInvocation fakeCommand1 = new CommandInvocation();
        fakeCommand1.setCommandId("fake");
        fakeCommand1.setStatus(CommandInvocationStatus.Success);
        CommandInvocation fakeCommand2 = new CommandInvocation();
        fakeCommand2.setCommandId("fake");
        fakeCommand2.setStatus(CommandInvocationStatus.Success);
        fakeCommandList.setCommandInvocations(Arrays.asList(fakeCommand1, fakeCommand2));

        when(user.getEmail()).thenReturn("testUserEmail");
        when(user.getUserId()).thenReturn("testUserId");
        when(user.getName()).thenReturn("testUserName");

        when(accessRequest.getAccount()).thenReturn("DEV");
        when(accessRequest.getHours()).thenReturn(5);
        when(accessRequest.getId()).thenReturn(10L);


        when(firstInstanceInformation.getInstanceId()).thenReturn("testIdOne");
        when(secondInstanceInformation.getInstanceId()).thenReturn("testIdTwo");

        when(firstInstanceInformation.getPingStatus()).thenReturn("Online");
        when(secondInstanceInformation.getPingStatus()).thenReturn("Offline");

        List<InstanceInformation> instanceInformation = new  ArrayList<>();
        instanceInformation.add(firstInstanceInformation);

        List<InstanceInformation> secondInstanceInformationList = new  ArrayList<>();
        secondInstanceInformationList.add(secondInstanceInformation);


        when(firstDescribeInstanceInformationResult.getNextToken()).thenReturn(null);
        when(firstDescribeInstanceInformationResult.getInstanceInformationList()).thenReturn(instanceInformation);


        when(secondDescribeInstanceInformationResult.getNextToken()).thenReturn(null);
        when(secondDescribeInstanceInformationResult.getInstanceInformationList()).thenReturn(secondInstanceInformationList);

        when(awsSessionService.getSsmSession(Mockito.any())).thenReturn(awsSimpleSystemsManagementClient);
        when(awsSimpleSystemsManagementClient.sendCommand(Mockito.any())).thenReturn(fakeResult);
        when(awsSimpleSystemsManagementClient.listCommandInvocations(Mockito.any())).thenReturn(fakeCommandList);

        when(awsSimpleSystemsManagementClient.describeInstanceInformation(Mockito.any())).thenReturn(firstDescribeInstanceInformationResult);
        Map<String, GatekeeperSsmProperties.SsmDocument> linuxDocumentMap = new HashMap<>();


        Map<String, GatekeeperSsmProperties.SsmDocument> windowsDocumentMap = new HashMap<>();


        GatekeeperSsmProperties.SsmDocument windowsCreateDocument = new GatekeeperSsmProperties.SsmDocument();
        GatekeeperSsmProperties.SsmDocument windowsDeleteDocument = new GatekeeperSsmProperties.SsmDocument();
        GatekeeperSsmProperties.SsmDocument linuxCreateDocument = new GatekeeperSsmProperties.SsmDocument();
        GatekeeperSsmProperties.SsmDocument linuxDeleteDocument = new GatekeeperSsmProperties.SsmDocument();
        windowsCreateDocument.setDocumentName("test-windows-create");
        windowsCreateDocument.setTimeout(5);
        windowsCreateDocument.setWaitInterval(1);
        windowsDeleteDocument.setDocumentName("test-windows-delete");
        windowsDeleteDocument.setTimeout(10);
        windowsDeleteDocument.setWaitInterval(1);

        linuxCreateDocument.setDocumentName("test-linux-create");
        linuxCreateDocument.setTimeout(15);
        linuxCreateDocument.setWaitInterval(1);
        linuxDeleteDocument.setDocumentName("test-linux-delete");
        linuxDeleteDocument.setTimeout(20);
        linuxDeleteDocument.setWaitInterval(1);

        linuxDocumentMap.put("create", linuxCreateDocument);
        linuxDocumentMap.put("delete", linuxDeleteDocument);

        windowsDocumentMap.put("create", windowsCreateDocument);
        windowsDocumentMap.put("delete", windowsDeleteDocument);


        when(ssmDocuments.getPlatformDocuments("Linux")).thenReturn(linuxDocumentMap);
        when(ssmDocuments.getPlatformDocuments("Windows")).thenReturn(windowsDocumentMap);

        when(email.getFrom()).thenReturn("testMailFrom");
        when(email.getTeam()).thenReturn("testTeamMail");
        when(email.getOpsEmails()).thenReturn("testOpsMail");

    }

    @Test
    public void testCreateUserAccount(){
        Map<String, String> results = ssmService.createUserAccount(awsEnvironment, Arrays.asList("i-1","i-2"), "theUser", "HelloKey", "Linux", "23");
        Assert.assertTrue("Verify that the ssm succeeds for Linux", results.containsValue("Success"));
        SendCommandRequest scr = new SendCommandRequest()
                .withInstanceIds( Arrays.asList("i-1","i-2"))
                .withDocumentName("test-linux-create")
                .addParametersEntry("userName",Arrays.asList("theUser"))
                .addParametersEntry("publicKey",Arrays.asList("HelloKey"))
                .addParametersEntry("hours", Arrays.asList("23"))
                .addParametersEntry("executionTimeout",Arrays.asList("15"));
        verify(awsSimpleSystemsManagementClient, times(1)).sendCommand(scr);
    }

    @Test
    public void testCreateUserAccountWindows(){
        Map<String, String> results = ssmService.createUserAccount(awsEnvironment, Arrays.asList("i-3","i-4"), user, accessRequest, "Windows");
        Assert.assertTrue("Verify that the ssm succeeds for Linux", results.containsValue("Success"));
        SendCommandRequest scr = new SendCommandRequest()
                .withInstanceIds( Arrays.asList("i-3","i-4"))
                .withDocumentName("test-windows-create")
                .addParametersEntry("userName",Arrays.asList("testUserName"))
                .addParametersEntry("userEmail",Arrays.asList("testUserEmail"))
                .addParametersEntry("userId",Arrays.asList("testUserId"))
                .addParametersEntry("hours",Arrays.asList("5"))
                .addParametersEntry("account",Arrays.asList("DEV"))
                .addParametersEntry("accessRequest",Arrays.asList(Long.toString(10L)))
                .addParametersEntry("mailFrom",Arrays.asList("testMailFrom"))
                .addParametersEntry("teamEmail",Arrays.asList("testTeamMail"))
                .addParametersEntry("opsEmail",Arrays.asList("testOpsMail"))
                .addParametersEntry("executionTimeout",Arrays.asList("5"));
        verify(awsSimpleSystemsManagementClient, times(1)).sendCommand(scr);
    }
    @Test
    public void testCreateUserAccountFails(){
        ListCommandInvocationsResult fakeCommandList = new ListCommandInvocationsResult();
        CommandInvocation fakeCommand1 = new CommandInvocation();
        fakeCommand1.setCommandId("fake");
        fakeCommand1.setStatus(CommandInvocationStatus.Failed);
        CommandInvocation fakeCommand2 = new CommandInvocation();
        fakeCommand2.setCommandId("fake");
        fakeCommand2.setStatus(CommandInvocationStatus.Success);
        fakeCommandList.setCommandInvocations(Arrays.asList(fakeCommand1, fakeCommand2));
        when(awsSimpleSystemsManagementClient.listCommandInvocations(Mockito.any())).thenReturn(fakeCommandList);
        Map<String, String> results = ssmService.createUserAccount(awsEnvironment, Arrays.asList("i-1","i-2"), "theUser", "HelloKey", "Linux", "23");
        Assert.assertFalse("Verify that the ssm reports false and times out", !results.containsValue("Success"));
    }

    @Test
    public void testCreateUserAccountTimesOut(){
        ListCommandInvocationsResult timeout = new ListCommandInvocationsResult();
        timeout.setCommandInvocations(new ArrayList<>());
        when(awsSimpleSystemsManagementClient.listCommandInvocations(Mockito.any())).thenReturn(timeout);
        Map<String, String> results = ssmService.createUserAccount(awsEnvironment, Arrays.asList("i-1","i-2"), "theUser", "HelloKey", "Linux", "23");
        Assert.assertFalse("Verify that the ssm reports false and times out",results.containsValue("Success"));
    }

    @Test
    public void testDeleteUserAccount(){
        Map<String, String> results = ssmService.deleteUserAccount(awsEnvironment, Arrays.asList("i-1","i-2"), "theUser", "Linux");
        Assert.assertTrue("Verify that the ssm succeeds for Linux", results.containsValue("Success"));

        SendCommandRequest scr = new SendCommandRequest()
                .withInstanceIds( Arrays.asList("i-1","i-2"))
                .withDocumentName("test-linux-delete")
                .addParametersEntry("userName",Arrays.asList("theUser"));
        verify(awsSimpleSystemsManagementClient, times(1)).sendCommand(scr);

        results = ssmService.deleteUserAccount(awsEnvironment, Arrays.asList("i-3","i-4"), "theWindowsUser", "Windows");
        Assert.assertTrue("Verify that the ssm succeeds for Windows", results.containsValue("Success"));

        scr = new SendCommandRequest()
                .withInstanceIds( Arrays.asList("i-3","i-4"))
                .withDocumentName("test-windows-delete")
                .addParametersEntry("userName",Arrays.asList("theWindowsUser"));
        verify(awsSimpleSystemsManagementClient, times(1)).sendCommand(scr);

    }

    @Test
    public void testCheckInstancesWithSsm(){
        List<String>instanceIds=new ArrayList<>();
        instanceIds.add(firstInstanceInformation.getInstanceId());
        Map<String,String> statusMap = ssmService.checkInstancesWithSsm(awsEnvironment,instanceIds);
        Assert.assertTrue("Verify only one instance is returned", statusMap.size()==1);
        Assert.assertTrue("Verify the correct instance is returned", statusMap.keySet().contains(firstInstanceInformation.getInstanceId()));
        Assert.assertTrue("Verify the correct status is returned", statusMap.get(firstInstanceInformation.getInstanceId()).equals(firstInstanceInformation.getPingStatus()));

        when(firstDescribeInstanceInformationResult.getNextToken()).thenReturn("ABC");
        when(awsSimpleSystemsManagementClient.describeInstanceInformation(Mockito.any())).thenReturn(firstDescribeInstanceInformationResult).thenReturn(secondDescribeInstanceInformationResult);

        statusMap = ssmService.checkInstancesWithSsm(awsEnvironment,instanceIds);

        Assert.assertTrue("Verify three instances are now returned", statusMap.size()==2);
        Assert.assertTrue("Verify the correct instance is returned given first instance ID", statusMap.keySet().contains(firstInstanceInformation.getInstanceId()));
        Assert.assertTrue("Verify the correct instance is returned given second instance ID", statusMap.keySet().contains(secondInstanceInformation.getInstanceId()));
        Assert.assertTrue("Verify the correct status is returned given first instance ID", statusMap.get(firstInstanceInformation.getInstanceId()).equals(firstInstanceInformation.getPingStatus()));
        Assert.assertTrue("Verify the correct status is returned given second instance ID", statusMap.get(secondInstanceInformation.getInstanceId()).equals(secondInstanceInformation.getPingStatus()));


    }
}
