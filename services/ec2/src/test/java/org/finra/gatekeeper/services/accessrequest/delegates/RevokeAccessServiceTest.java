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

package org.finra.gatekeeper.services.accessrequest.delegates;

import org.activiti.engine.ManagementService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.JobQuery;
import org.finra.gatekeeper.services.aws.Ec2LookupService;
import org.finra.gatekeeper.services.aws.SsmService;
import org.finra.gatekeeper.services.accessrequest.model.AWSInstance;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.email.wrappers.EmailServiceWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;


/**
 * Tests for Gatekeeper Revoke Access Task
 */
@Configuration
@ActiveProfiles("unit-test")
@RunWith(MockitoJUnitRunner.class)
public class RevokeAccessServiceTest {

    @Mock
    private EmailServiceWrapper emailServiceWrapper;
    @Mock
    private SsmService ssmService;
    @Mock
    private ManagementService managementService;
    @Mock
    private Ec2LookupService ec2LookupService;
    @Mock
    private DelegateExecution execution;
    @Mock
    private JobQuery mockjobQuery = new JobQueryImpl();
    @Mock
    private JobEntity mockJobEntity;

    @InjectMocks
    private RevokeAccessServiceTask revokeAccessServiceTask;

    private AccessRequest mockRequest;
    @Before
    public void init(){
        Mockito.when(managementService.createJobQuery()).thenReturn(mockjobQuery);
        Mockito.when(mockjobQuery.processInstanceId(Mockito.anyString())).thenReturn(mockjobQuery);
        Mockito.when(mockjobQuery.singleResult()).thenReturn(mockJobEntity);
        Mockito.when(mockJobEntity.getRetries()).thenReturn(2);
        Mockito.when(execution.getProcessInstanceId()).thenReturn("1");

        mockRequest = new AccessRequest()
                .setId(1L)
                .setAccount("test")
                .setRegion("us-east-1")
                .setApproverComments("This is test")
                .setHours(1)
                .setRequestReason("To test this code")
                .setInstances(Arrays.asList(
                        createInstance("i-12345", "numbersInst", "linux", "123.23.3.2", "TST", "Online"),
                        createInstance("i-abcde", "alphasInst", "linux", "123.45.6.7", "TST", "Online"),
                        createInstance("i-123abc", "numbersAlphaInst", "linux", "101.50.4.2", "TST", "Online"),
                        createInstance("i-abc123", "alphaNumbersInst", "linux", "222.34.5,4", "TST", "Online")
                ))
                .setPlatform("linux")
                .setRequestorEmail("Test@email.com")
                .setRequestorId("reqtest")
                .setRequestorName("Test Requestor")
                .setUsers(Arrays.asList(
                        createUser(1L, "Test Requestor", "reqtest", "Test@email.com")
                ));

        Mockito.when(execution.getVariable(Mockito.any())).thenReturn(mockRequest);

    }

    @Test
    public void testRevokeDeleteNoNotifyAllOnline() throws Exception{
        Map<String, String> ssmMap = new HashMap<>();
        ssmMap.put("i-12345", "Online");
        ssmMap.put("i-abcde", "Online");
        ssmMap.put("i-123abc", "Online");
        ssmMap.put("i-abc123", "Online");

        Mockito.when(ssmService.checkInstancesWithSsm(Mockito.any(), Mockito.any())).thenReturn(ssmMap);

        revokeAccessServiceTask.execute(execution);

        Mockito.verify(emailServiceWrapper, times(0)).notifyOps(Mockito.any(), Mockito.any());
        Mockito.verify(ssmService, times(1)).deleteUserAccount(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    }

    @Test
    public void testRevokeDeleteNoNotifySomeTerminated() throws Exception{
        Map<String, String> ssmMap = new HashMap<>();
        ssmMap.put("i-12345", "Online");
        ssmMap.put("i-abcde", "Online");
        ssmMap.put("i-123abc", "Online");

        Mockito.when(ssmService.checkInstancesWithSsm(Mockito.any(), Mockito.any())).thenReturn(ssmMap);

        Map<String, Boolean> instancesStillExistMap = new HashMap<>();
        instancesStillExistMap.put("i-abc123", true);

        Mockito.when(ec2LookupService.checkIfInstancesExistOrTerminated(Mockito.any(), Mockito.any())).thenReturn(instancesStillExistMap);

        revokeAccessServiceTask.execute(execution);

        Mockito.verify(emailServiceWrapper, times(0)).notifyOps(Mockito.any(), Mockito.any());
        Mockito.verify(ssmService, times(1)).deleteUserAccount(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    }

    @Test
    public void testRevokeDeleteAndNotify() throws Exception{
        Map<String, String> ssmMap = new HashMap<>();
        ssmMap.put("i-12345", "Online");
        ssmMap.put("i-abcde", "Online");
        ssmMap.put("i-123abc", "Not good man");

        Mockito.when(ssmService.checkInstancesWithSsm(Mockito.any(), Mockito.any())).thenReturn(ssmMap);

        Map<String, Boolean> instancesStillExistMap = new HashMap<>();
        instancesStillExistMap.put("i-123abc", true);
        instancesStillExistMap.put("i-abc123", false);

        Mockito.when(ec2LookupService.checkIfInstancesExistOrTerminated(Mockito.any(), Mockito.any())).thenReturn(instancesStillExistMap);
        revokeAccessServiceTask.execute(execution);

        Mockito.verify(emailServiceWrapper, times(1)).notifyOps(Mockito.any(), Mockito.any());
        Mockito.verify(ssmService, times(1)).deleteUserAccount(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    private AWSInstance createInstance(String instanceId, String name, String platform, String ip, String application, String status){
        return new AWSInstance()
                .setApplication(application)
                .setInstanceId(instanceId)
                .setName(name)
                .setPlatform(platform)
                .setIp(ip)
                .setStatus(status);
    }

    private User createUser(Long id, String name, String userId, String email){
        return new User()
                .setId(id)
                .setName(name)
                .setUserId(userId)
                .setEmail(email);
    }
}
