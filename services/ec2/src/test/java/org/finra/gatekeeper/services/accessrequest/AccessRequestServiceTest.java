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

package org.finra.gatekeeper.services.accessrequest;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.history.NativeHistoricVariableInstanceQuery;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.finra.gatekeeper.common.services.account.model.Region;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.properties.GatekeeperApprovalProperties;
import org.finra.gatekeeper.services.auth.GatekeeperRole;
import org.finra.gatekeeper.services.aws.SnsService;
import org.finra.gatekeeper.services.aws.SsmService;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.ActiveAccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.CompletedAccessRequestWrapper;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.accessrequest.model.*;
import org.finra.gatekeeper.services.email.wrappers.EmailServiceWrapper;
import org.hibernate.query.internal.NativeQueryImpl;
import org.junit.Assert;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import javax.persistence.EntityManager;

import static org.finra.gatekeeper.services.accessrequest.AccessRequestService.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

/**
 * Unit tests for the Access Request Service
 */

@ActiveProfiles("unit-test")
@RunWith(MockitoJUnitRunner.class)
public class AccessRequestServiceTest {

    @InjectMocks
    private AccessRequestService accessRequestService;

    @Mock
    private TaskService taskService;

    @Mock
    private AccessRequestRepository accessRequestRepository;

    @Mock
    private GatekeeperRoleService gatekeeperLdapService;

    @Mock
    private HistoryService historyService;

    @Mock
    private AccessRequest ownerRequest;

    @Mock
    private AccessRequestWrapper ownerRequestWrapper;

    @Mock
    private AccessRequest nonOwnerRequest;

    @Mock
    private AWSInstance awsInstance;

    @Mock
    private User user;

    @Mock
    private GatekeeperUserEntry gatekeeperUserEntry;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private ProcessInstanceQuery processInstanceQuery;

    @Mock
    private HistoricVariableInstanceQuery historicVariableInstanceQuery;

    @Mock
    private NativeHistoricVariableInstanceQuery nativeHistoricVariableInstanceQuery;

    @Mock
    private HistoricVariableInstanceEntity ownerHistoricVariableInstanceAttempt;

    @Mock
    private HistoricVariableInstanceEntity ownerHistoricVariableInstanceStatus;

    @Mock
    private HistoricVariableInstanceEntity ownerHistoricVariableInstanceAccessRequest;

    @Mock
    private HistoricVariableInstanceEntity nonOwnerHistoricVariableInstanceAttempt;

    @Mock
    private HistoricVariableInstanceEntity nonOwnerHistoricVariableInstanceStatus;

    @Mock
    private HistoricVariableInstanceEntity nonOwnerHistoricVariableInstanceAccessRequest;

    @Mock
    private VariableInstance ownerOneTaskInstance;

    @Mock
    private VariableInstance ownerTwoTaskInstance;

    @Mock
    private GatekeeperApprovalProperties approvalPolicy;

    @Mock
    private Task ownerOneTask;

    @Mock
    private Task ownerTwoTask;

    @Mock
    private TaskQuery taskQuery;

    @Mock
    private AccountInformationService accountInformationService;

    @Mock
    private SsmService ssmService;

    @Mock
    private SnsService snsService;

    @Mock
    private EmailServiceWrapper emailServiceWrapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private NativeQueryImpl query;

    @Mock
    private NativeQueryImpl instanceQuery;

    @Mock
    private NativeQueryImpl userQuery;

    private Date testDate;

    private List<Map<String, String>> requestsMap = new ArrayList<>();
    private List<Map<String, String>> instanceMap = new ArrayList<>();
    private List<Map<String, String>> userMap = new ArrayList<>();

    @Before
    public void initMocks() {
        testDate = new Date();
        //Setting up the spring values
        Map<String, Object> mockValues = new HashMap<>();
        Map<String, Integer> mockDev = new HashMap<>();
        mockDev.put("dev", 48);
        mockDev.put("qa", 48);
        mockDev.put("prod", 2);
        Map<String, Integer> mockOps = new HashMap<>();
        mockOps.put("dev", 48);
        mockOps.put("qa", 48);
        mockOps.put("prod", 2);
        Map<String, Integer> mockSupp = new HashMap<>();
        mockSupp.put("dev", 48);
        mockSupp.put("qa", 48);
        mockSupp.put("prod", 2);
        mockValues.put("dev", mockDev);
        mockValues.put("ops", mockOps);
        mockValues.put("support", mockSupp);

        Region[] regions = new Region[]{ new Region("us-east-1") };
        Account mockAccount = new Account("1234", "Dev Test", "dev", "dev-test", Arrays.asList(regions));

        when(approvalPolicy.getApprovalPolicy(GatekeeperRole.DEV)).thenReturn(mockDev);
        when(approvalPolicy.getApprovalPolicy(GatekeeperRole.OPS)).thenReturn(mockOps);
        when(approvalPolicy.getApprovalPolicy(GatekeeperRole.SUPPORT)).thenReturn(mockSupp);

        List<AWSInstance> instances = new ArrayList<>();
        when(awsInstance.getApplication()).thenReturn("TestApp");
        when(awsInstance.getInstanceId()).thenReturn("testId");
//        when(awsInstance.getName()).thenReturn("testName");
//        when(awsInstance.getIp()).thenReturn("1.2.3.4");
        when(awsInstance.getPlatform()).thenReturn("testPlatform");
        instances.add(awsInstance);

        //Owner mock
        when(ownerRequest.getAccount()).thenReturn("DEV");
        when(ownerRequest.getInstances()).thenReturn(instances);
        when(ownerRequest.getHours()).thenReturn(1);
        when(ownerRequest.getRequestorId()).thenReturn("owner");
        when(ownerRequest.getId()).thenReturn(1L);



        //Non-owner mock
        when(nonOwnerRequest.getAccount()).thenReturn("DEV");
        when(nonOwnerRequest.getInstances()).thenReturn(instances);
        when(nonOwnerRequest.getHours()).thenReturn(1);
        when(nonOwnerRequest.getRequestorId()).thenReturn("non-owner");
        when(nonOwnerRequest.getId()).thenReturn(2L);
        when(nonOwnerRequest.getPlatform()).thenReturn("testPlatform");
//        when(nonOwnerRequest.getUsers()).thenReturn(Arrays.asList(new User().setId(1L).setUserId("user").setEmail("user@email").setName("username")));
//

        Set<String> ownerMemberships = new HashSet<String>();
        ownerMemberships.add("TestApp");

        when(ownerRequestWrapper.getInstances()).thenReturn(instances);
        when(ownerRequestWrapper.getHours()).thenReturn(1);
        when(ownerRequestWrapper.getAccount()).thenReturn("testAccount");
        when(ownerRequestWrapper.getRegion()).thenReturn("testRegion");
        when(ownerRequestWrapper.getPlatform()).thenReturn("testPlatform");
        when(gatekeeperUserEntry.getUserId()).thenReturn("testUserId");
        when(gatekeeperUserEntry.getName()).thenReturn("testName");
        when(gatekeeperUserEntry.getEmail()).thenReturn("testEmail@finra.org");
        when(user.getUserId()).thenReturn("testUserId");
        List<User> users = new ArrayList<>();
        users.add(user);
        when(ownerRequestWrapper.getUsers()).thenReturn(users);
        when(ownerRequest.getUsers()).thenReturn(users);

        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.DEV);
        when(gatekeeperLdapService.getMemberships()).thenReturn(ownerMemberships);
        when(gatekeeperLdapService.getUserProfile()).thenReturn(gatekeeperUserEntry);

        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(runtimeService.createProcessInstanceQuery().count()).thenReturn(2L);


        //Mocks for getActiveRequest()
        when(ownerOneTask.getExecutionId()).thenReturn("ownerOneTask");
        when(ownerOneTask.getCreateTime()).thenReturn(new Date(4500000));
        when(ownerOneTask.getId()).thenReturn("taskOne");

        when(ownerTwoTask.getExecutionId()).thenReturn("ownerTwoTask");
        when(ownerTwoTask.getCreateTime()).thenReturn(testDate);
        when(ownerTwoTask.getId()).thenReturn("taskTwo");

        when(ownerOneTaskInstance.getTextValue2()).thenReturn("1");
        when(ownerTwoTaskInstance.getTextValue2()).thenReturn("2");

        when(accessRequestRepository.getAccessRequestById(1L)).thenReturn(ownerRequest);
        when(accessRequestRepository.getAccessRequestById(2L)).thenReturn(nonOwnerRequest);

        when(runtimeService.getVariableInstance("ownerOneTask", "accessRequest")).thenReturn(ownerOneTaskInstance);
        when(runtimeService.getVariableInstance("ownerTwoTask", "accessRequest")).thenReturn(ownerTwoTaskInstance);



        List<Task> activeTasks = new ArrayList<>();
        activeTasks.add(ownerOneTask);
        activeTasks.add(ownerTwoTask);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskService.createTaskQuery().active()).thenReturn(taskQuery);
        when(taskService.createTaskQuery().active().list()).thenReturn(activeTasks);

        //Mocks for getCompletedRequest()
        List<HistoricVariableInstance> taskVars = new ArrayList<>();
        taskVars.add(nonOwnerHistoricVariableInstanceAttempt);
        taskVars.add(nonOwnerHistoricVariableInstanceStatus);
        taskVars.add(nonOwnerHistoricVariableInstanceAccessRequest);

        Map<String,String> statusMap = new HashMap<>();
        statusMap.put("testId","Unknown");
        when(ssmService.checkInstancesWithSsm(any(),any())).thenReturn(statusMap);
        when(ssmService.checkInstancesAreValidWithSsm(any(), any())).thenReturn("");
        when(accountInformationService.getAccountByAlias(any())).thenReturn(mockAccount);


        Map<String, String> ownerMap = new HashMap<>();
        Map<String, String> nonOwnerMap = new HashMap<>();
        ownerMap.put("taskId", "1");
        ownerMap.put("requestorId", "owner");
        ownerMap.put("instanceCount", "1");
        ownerMap.put("userCount", "1");
        ownerMap.put("created", "1969-12-29T00:00:00");
        ownerMap.put("updated", "1969-12-31T00:00:00");

        nonOwnerMap.put("taskId", "1");
        nonOwnerMap.put("requestorId", "non-owner");
        nonOwnerMap.put("instanceCount", "1");
        nonOwnerMap.put("userCount", "1");
        nonOwnerMap.put("created", "1969-12-29T00:00:00");
        nonOwnerMap.put("updated", "1969-12-31T00:00:00");


        requestsMap.add(ownerMap);
        requestsMap.add(nonOwnerMap);

        Map<String, String> instanceList = new HashMap<>();
        instanceList.put("instances_id", "1");
        instanceMap.add(instanceList);

        Map<String, String> userList = new HashMap<>();
        userList.put("user_id", "1");
        userMap.add(userList);


    }


    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed.
     */
    @Test
    public void testApprovalNeededAdmin() throws Exception {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.APPROVER);
        Assert.assertFalse(accessRequestService.isApprovalNeeded(ownerRequest));
        Assert.assertFalse(accessRequestService.isApprovalNeeded(nonOwnerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has DEV role, is owner of instance, and
     * does exceed threshold
     */
    @Test
    public void testApprovalNeededDevOwnerThreshold() throws Exception {
        when(ownerRequest.getHours()).thenReturn(300);
        Assert.assertTrue(accessRequestService.isApprovalNeeded(ownerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has DEV role, is owner of instance, and
     * does not exceed threshold
     */
    @Test
    public void testApprovalNeededDevOwner() throws Exception {
        Assert.assertFalse(accessRequestService.isApprovalNeeded(ownerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has DEV role, is not owner of instance, and
     * does not exceed threshold
     */
    @Test
    public void testApprovalNeededDevNonOwner() throws Exception {
        Set<String> notOwner = new HashSet<>();
        when(gatekeeperLdapService.getMemberships()).thenReturn(notOwner);
        Assert.assertTrue(accessRequestService.isApprovalNeeded(nonOwnerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has DEV role, is not owner of instance, and
     * does exceed threshold
     */
    @Test
    public void testApprovalNeededDevThreshold() throws Exception {
        when(nonOwnerRequest.getHours()).thenReturn(300);
        Assert.assertTrue(accessRequestService.isApprovalNeeded(nonOwnerRequest));
    }


    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has OPS role, is owner of instance, and
     * does exceed threshold
     */
    @Test
    public void testApprovalNeededOpsOwnerThreshold() throws Exception {
        when(ownerRequest.getHours()).thenReturn(400);
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.OPS);
        Assert.assertTrue(accessRequestService.isApprovalNeeded(ownerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has OPS role, is owner of instance, and
     * does not exceed threshold
     */
    @Test
    public void testApprovalNeededOpsOwner() throws Exception {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.OPS);
        Assert.assertFalse(accessRequestService.isApprovalNeeded(ownerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has OPS role, is not owner of instance, and
     * does not exceed threshold
     */
    @Test
    public void testApprovalNeededOpsNonOwner() throws Exception {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.OPS);
        Set<String> notOwner = new HashSet<>();
        when(gatekeeperLdapService.getMemberships()).thenReturn(notOwner);
        Assert.assertTrue(accessRequestService.isApprovalNeeded(nonOwnerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has OPS role, is not owner of instance, and
     * does exceed threshold
     */
    @Test
    public void testApprovalNeededOpsThreshold() throws Exception {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.OPS);
        when(nonOwnerRequest.getHours()).thenReturn(99);
        Assert.assertTrue(accessRequestService.isApprovalNeeded(nonOwnerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has SUPPORT role, is owner of instance, and
     * does exceed threshold
     */
    @Test
    public void testApprovalNeededSupportOwnerThreshold() throws Exception {
        when(ownerRequest.getHours()).thenReturn(50);
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.SUPPORT);
        Assert.assertTrue(accessRequestService.isApprovalNeeded(ownerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has SUPPORT role, is owner of instance, and
     * does not exceed threshold
     */
    @Test
    public void testApprovalNeededSupportOwner() throws Exception {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.SUPPORT);
        Assert.assertFalse(accessRequestService.isApprovalNeeded(ownerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has SUPPORT role, is not owner of instance, and
     * does not exceed threshold
     */
    @Test
    public void testApprovalNeededSupportNonOwner() throws Exception {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.SUPPORT);
        Assert.assertFalse(accessRequestService.isApprovalNeeded(nonOwnerRequest));
    }

    /**
     * Test the command used within the workflow to determine whether or not
     * approval is needed when the user has SUPPORT role, is not owner of instance, and
     * does exceed threshold
     */
    @Test
    public void testApprovalNeededSupportThreshold() throws Exception {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.SUPPORT);
        when(nonOwnerRequest.getHours()).thenReturn(50);
        Assert.assertTrue(accessRequestService.isApprovalNeeded(nonOwnerRequest));
    }

    /**
     * Test for making sure the storeAccessRequest method works. Makes sure the accessRequestRepository
     * is called and called with the correct object.
     */
    @Test
    public void testStoreAccessRequest() throws GatekeeperException {
        List<User> users = new ArrayList<>();
        users.add(user);
        List<AWSInstance> instances = new ArrayList<>();
        instances.add(awsInstance);

        when(snsService.isEmailTopicSet()).thenReturn(false);

        AccessRequest result = accessRequestService.storeAccessRequest(ownerRequestWrapper);

        Assert.assertEquals(result.getRequestorEmail(), "testEmail@finra.org");
        Assert.assertEquals(result.getRequestorId(), "testUserId");
        Assert.assertEquals(result.getRequestorName(), "testName");
        Assert.assertEquals(result.getRegion(), "testRegion");
        Assert.assertEquals(result.getAccount(), "TESTACCOUNT");
        Assert.assertEquals((long) result.getHours(), 1L);

        Assert.assertEquals(result.getUsers(), users);
        Assert.assertEquals(result.getInstances(), instances);

        verify(accessRequestRepository, times(1)).save(result);


    }


    /**
     * Test for making sure the storeAccessRequest method throws an exception if a prod request for datafix . Makes sure the accessRequestRepository
     * is called and called with the correct object.
     */
    @Test
    public void testStoreAccessRequestDaysBeyondMax() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(user);
        List<AWSInstance> instances = new ArrayList<>();
        instances.add(awsInstance);

        when(snsService.isEmailTopicSet()).thenReturn(false);
        when(ownerRequestWrapper.getHours()).thenReturn(181);
        AccessRequest result = accessRequestService.storeAccessRequest(ownerRequestWrapper);

        Assert.assertEquals(result.getRequestorEmail(), "testEmail@finra.org");
        Assert.assertEquals(result.getRequestorId(), "testUserId");
        Assert.assertEquals(result.getRequestorName(), "testName");
        Assert.assertEquals(result.getRegion(), "testRegion");
        Assert.assertEquals(result.getAccount(), "TESTACCOUNT");
        Assert.assertEquals((long) result.getHours(), 181L);

        Assert.assertEquals(result.getUsers(), users);
        Assert.assertEquals(result.getInstances(), instances);

        verify(accessRequestRepository, times(1)).save(result);
        verify(snsService, times(0)).pushToEmailSNSTopic(any());
    }


    /**
     * Test for making sure the storeAccessRequest method throws an exception if a prod request for datafix . Makes sure the accessRequestRepository
     * is called and called with the correct object.
     */
    @Test
    public void testStoreAccessRequestPushToEmailSnsTopic() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(user);
        List<AWSInstance> instances = new ArrayList<>();
        instances.add(awsInstance);

        when(snsService.isEmailTopicSet()).thenReturn(true);
        when(ownerRequestWrapper.getHours()).thenReturn(181);
        AccessRequest result = accessRequestService.storeAccessRequest(ownerRequestWrapper);

        Assert.assertEquals(result.getRequestorEmail(), "testEmail@finra.org");
        Assert.assertEquals(result.getRequestorId(), "testUserId");
        Assert.assertEquals(result.getRequestorName(), "testName");
        Assert.assertEquals(result.getRegion(), "testRegion");
        Assert.assertEquals(result.getAccount(), "TESTACCOUNT");
        Assert.assertEquals((long) result.getHours(), 181L);

        Assert.assertEquals(result.getUsers(), users);
        Assert.assertEquals(result.getInstances(), instances);

        verify(accessRequestRepository, times(1)).save(result);
        verify(snsService, times(1)).pushToEmailSNSTopic(any());
    }


    /**
     * Test for making sure the storeAccessRequest method throws an exception if a prod request for datafix . Makes sure the accessRequestRepository
     * is called and called with the correct object.
     */
    @Test
    public void testStoreAccessRequestSNSThrowsException() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(user);
        List<AWSInstance> instances = new ArrayList<>();
        instances.add(awsInstance);

        when(snsService.isEmailTopicSet()).thenReturn(true);
        when(snsService.pushToEmailSNSTopic(any())).thenThrow(GatekeeperException.class);
        when(ownerRequestWrapper.getHours()).thenReturn(181);
        AccessRequest result = accessRequestService.storeAccessRequest(ownerRequestWrapper);

        Assert.assertEquals(result.getRequestorEmail(), "testEmail@finra.org");
        Assert.assertEquals(result.getRequestorId(), "testUserId");
        Assert.assertEquals(result.getRequestorName(), "testName");
        Assert.assertEquals(result.getRegion(), "testRegion");
        Assert.assertEquals(result.getAccount(), "TESTACCOUNT");
        Assert.assertEquals((long) result.getHours(), 181L);

        Assert.assertEquals(result.getUsers(), users);
        Assert.assertEquals(result.getInstances(), instances);

        verify(accessRequestRepository, times(1)).save(result);
        verify(snsService, times(1)).pushToEmailSNSTopic(any());
        verify(emailServiceWrapper, times(1)).notifyAdminsOfFailure(any(), any());;
    }


    /**
     * Test for checking that, when the user is APPROVER, they should be able to see
     * any active request. Even ones that they do not own.
     */
    @Test
    public void testGetActiveRequestsAdmin() {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.APPROVER);
        List<ActiveAccessRequestWrapper> activeRequests = accessRequestService.getActiveRequests();
        Assert.assertTrue(activeRequests.size() == 2);

        ActiveAccessRequestWrapper ownerRequest = activeRequests.get(0);
        Assert.assertEquals(ownerRequest.getUserCount(), new Integer(1));
        Assert.assertEquals(ownerRequest.getInstanceCount(), new Integer(1));
        Assert.assertEquals(ownerRequest.getCreated().toString(), new Date(4500000).toString());
        Assert.assertEquals(ownerRequest.getTaskId(), "taskOne");
        ActiveAccessRequestWrapper nonOwnerRequest = activeRequests.get(1);
        Assert.assertEquals(nonOwnerRequest.getUserCount(), new Integer(0));
        Assert.assertEquals(nonOwnerRequest.getInstanceCount(), new Integer(1));
        Assert.assertEquals(nonOwnerRequest.getCreated().toString(), testDate.toString());
        Assert.assertEquals(nonOwnerRequest.getTaskId(), "taskTwo");
    }


    /**
     * Test for checking that, when the user is AUDITOR, they should be able to see
     * any active request. Even ones that they do not own.
     */
    @Test
    public void testGetActiveRequestsAuditor() {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.AUDITOR);
        List<ActiveAccessRequestWrapper> activeRequests = accessRequestService.getActiveRequests();
        Assert.assertTrue(activeRequests.size() == 2);

        ActiveAccessRequestWrapper ownerRequest = activeRequests.get(0);
        Assert.assertEquals(ownerRequest.getUserCount(), new Integer(1));
        Assert.assertEquals(ownerRequest.getInstanceCount(), new Integer(1));
        Assert.assertEquals(ownerRequest.getCreated().toString(), new Date(4500000).toString());
        Assert.assertEquals(ownerRequest.getTaskId(), "taskOne");
        ActiveAccessRequestWrapper nonOwnerRequest = activeRequests.get(1);
        Assert.assertEquals(nonOwnerRequest.getUserCount(), new Integer(0));
        Assert.assertEquals(nonOwnerRequest.getInstanceCount(), new Integer(1));
        Assert.assertEquals(nonOwnerRequest.getCreated().toString(), testDate.toString());
        Assert.assertEquals(nonOwnerRequest.getTaskId(), "taskTwo");
    }

    /**
     * Test for checking that, when the user is DEV, they should be able to see
     * only the requests that are active and were requested by themselves
     */
    @Test
    public void testGetActiveRequests() {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.DEV);
        when(gatekeeperLdapService.getUserProfile().getUserId()).thenReturn("owner");
        List<ActiveAccessRequestWrapper> activeRequests = accessRequestService.getActiveRequests();
        Assert.assertEquals(activeRequests.size(),1);

        ActiveAccessRequestWrapper ownerRequest = activeRequests.get(0);
        Assert.assertEquals(ownerRequest.getUserCount(), new Integer(1));
        Assert.assertEquals(ownerRequest.getInstanceCount(), new Integer(1));
        Assert.assertEquals(ownerRequest.getCreated().toString(), new Date(4500000).toString());
        Assert.assertEquals(ownerRequest.getTaskId(), "taskOne");


        when(gatekeeperLdapService.getUserProfile().getUserId()).thenReturn("non-owner");
        activeRequests = accessRequestService.getActiveRequests();
        Assert.assertEquals(activeRequests.size(),1);

        ActiveAccessRequestWrapper nonOwnerRequest = activeRequests.get(0);
        Assert.assertEquals(nonOwnerRequest.getUserCount(), new Integer(0));
        Assert.assertEquals(nonOwnerRequest.getInstanceCount(), new Integer(1));
        Assert.assertEquals(nonOwnerRequest.getCreated().toString(), testDate.toString());
        Assert.assertEquals(nonOwnerRequest.getTaskId(), "taskTwo");
    }

    /**
     * Test for checking that, when the user is APPROVER, they should be able to see
     * any completed request. Even ones that they do not own.
     */
    @Test
    public void testGetCompletedRequestsAdmin() throws Exception {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.APPROVER);
        when(query.getResultList()).thenReturn(requestsMap);
        doReturn(query).when(entityManager).createNativeQuery(anyString());

        List<CompletedAccessRequestWrapper> completedRequests = accessRequestService.getCompletedRequests();

        Assert.assertEquals(2, completedRequests.size());
        CompletedAccessRequestWrapper ownerRequest = completedRequests.get(0);
        Assert.assertEquals(ownerRequest.getUserCount(), new Integer(1));
        Assert.assertEquals(ownerRequest.getInstanceCount(), new Integer(1));
//        Assert.assertEquals(ownerRequest.getAttempts(), new Integer(1));
        Assert.assertEquals("29 Dec 1969 00:00:00 GMT", ownerRequest.getCreated().toGMTString());
        Assert.assertEquals("31 Dec 1969 00:00:00 GMT", ownerRequest.getUpdated().toGMTString());

        CompletedAccessRequestWrapper nonOwnerRequest = completedRequests.get(1);
        Assert.assertEquals(new Integer(1), nonOwnerRequest.getUserCount());
        Assert.assertEquals(new Integer(1), nonOwnerRequest.getInstanceCount());
//        Assert.assertEquals(new Integer(2), nonOwnerRequest.getAttempts() );
        Assert.assertEquals("29 Dec 1969 00:00:00 GMT", nonOwnerRequest.getCreated().toGMTString());
        Assert.assertEquals("31 Dec 1969 00:00:00 GMT", nonOwnerRequest.getUpdated().toGMTString());


    }

    /**
     * Test for checking that, when the user is AUDITOR, they should be able to see
     * any completed request. Even ones that they do not own.
     */
    @Test
    public void testGetCompletedRequestsAuditor() throws Exception {
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.AUDITOR);
        when(query.getResultList()).thenReturn(requestsMap);
        doReturn(query).when(entityManager).createNativeQuery(anyString());

        List<CompletedAccessRequestWrapper> completedRequests = accessRequestService.getCompletedRequests();

        Assert.assertEquals(2, completedRequests.size());
        CompletedAccessRequestWrapper ownerRequest = completedRequests.get(0);
        Assert.assertEquals(ownerRequest.getUserCount(), new Integer(1));
        Assert.assertEquals(ownerRequest.getInstanceCount(), new Integer(1));
//        Assert.assertEquals(ownerRequest.getAttempts(), new Integer(1));
        Assert.assertEquals("29 Dec 1969 00:00:00 GMT", ownerRequest.getCreated().toGMTString());
        Assert.assertEquals("31 Dec 1969 00:00:00 GMT", ownerRequest.getUpdated().toGMTString());


        CompletedAccessRequestWrapper nonOwnerRequest = completedRequests.get(1);
        Assert.assertEquals(new Integer(1), nonOwnerRequest.getUserCount());
        Assert.assertEquals(new Integer(1), nonOwnerRequest.getInstanceCount());
//        Assert.assertEquals(new Integer(2), nonOwnerRequest.getAttempts() );
        Assert.assertEquals("29 Dec 1969 00:00:00 GMT", nonOwnerRequest.getCreated().toGMTString());
        Assert.assertEquals("31 Dec 1969 00:00:00 GMT", nonOwnerRequest.getUpdated().toGMTString());

    }

    /**
     * Test for checking that, when the user is DEV, they should be able to see
     * only the requests that are active and were requested by themselves
     */
    @Test
    public void testGetCompletedRequests() throws Exception {
        when(gatekeeperLdapService.getUserProfile().getUserId()).thenReturn("owner");
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.DEV);
        when(query.getResultList()).thenReturn(requestsMap);
        doReturn(query).when(entityManager).createNativeQuery(anyString());

        List<CompletedAccessRequestWrapper> completedRequests = accessRequestService.getCompletedRequests();
        Assert.assertEquals(1, completedRequests.size());
        CompletedAccessRequestWrapper ownerRequest = completedRequests.get(0);
        Assert.assertEquals(ownerRequest.getUserCount(), new Integer(1));
        Assert.assertEquals(ownerRequest.getInstanceCount(), new Integer(1));
//        Assert.assertEquals(ownerRequest.getAttempts(), new Integer(1));
        Assert.assertEquals("29 Dec 1969 00:00:00 GMT", ownerRequest.getCreated().toGMTString());
        Assert.assertEquals("31 Dec 1969 00:00:00 GMT", ownerRequest.getUpdated().toGMTString());


        when(gatekeeperLdapService.getUserProfile().getUserId()).thenReturn("non-owner");
        completedRequests = accessRequestService.getCompletedRequests();
        Assert.assertEquals(1,completedRequests.size());
        CompletedAccessRequestWrapper nonOwnerRequest = completedRequests.get(0);
        Assert.assertEquals(new Integer(1), nonOwnerRequest.getUserCount());
        Assert.assertEquals(new Integer(1), nonOwnerRequest.getInstanceCount());
//        Assert.assertEquals(new Integer(2), nonOwnerRequest.getAttempts() );
        Assert.assertEquals("29 Dec 1969 00:00:00 GMT", nonOwnerRequest.getCreated().toGMTString());
        Assert.assertEquals("31 Dec 1969 00:00:00 GMT", nonOwnerRequest.getUpdated().toGMTString());


    }

    /**
     * Test for checking that, a user can retrieve an individual request
     */
    @Test
    public void testGetRequest() throws Exception {
        when(gatekeeperLdapService.getUserProfile().getUserId()).thenReturn("owner");
        when(gatekeeperLdapService.getRole()).thenReturn(GatekeeperRole.DEV);

        when(query.getResultList()).thenReturn(requestsMap);
        doReturn(query).when(entityManager).createNativeQuery(anyString());


        when(userQuery.getResultList()).thenReturn(userMap);
        doReturn(userQuery).when(entityManager).createNativeQuery(USER_QUERY);

        when(instanceQuery.getResultList()).thenReturn(instanceMap);
        doReturn(instanceQuery).when(entityManager).createNativeQuery(INSTANCE_QUERY);

        List<CompletedAccessRequestWrapper> completedRequests = accessRequestService.getRequest(1L);
        Assert.assertEquals(1, completedRequests.size());
        CompletedAccessRequestWrapper ownerRequest = completedRequests.get(0);
        List<AWSInstance> awsInstances = ownerRequest.getInstances();
        List<User> userList = ownerRequest.getUsers();
        Assert.assertEquals(ownerRequest.getUserCount(), new Integer(1));
        Assert.assertEquals(1, awsInstances.size());
        Assert.assertEquals(1, userList.size());
        Assert.assertEquals(ownerRequest.getInstanceCount(), new Integer(1));
//        Assert.assertEquals(ownerRequest.getAttempts(), new Integer(1));
        Assert.assertEquals("29 Dec 1969 00:00:00 GMT", ownerRequest.getCreated().toGMTString());
        Assert.assertEquals("31 Dec 1969 00:00:00 GMT", ownerRequest.getUpdated().toGMTString());

    }

    /**
     * Tests that the status and taskID are passed to the taskService correctly
     * when the request is approved.
     */
    @Test
    public void testApproval(){
        Mockito.when(accessRequestRepository.getAccessRequestById(1L)).thenReturn(ownerRequest);
        accessRequestService.approveRequest("taskOne", 1L, "A reason", 1);
        Map<String,Object> statusMap = new HashMap<>();
        statusMap.put("requestStatus", RequestStatus.APPROVAL_GRANTED);
        verify(accessRequestRepository, times(3)).save(Mockito.any(AccessRequest.class));
        verify(taskService,times(1)).setAssignee("taskOne","testUserId");
        verify(taskService,times(1)).complete("taskOne",statusMap);
    }

    /**
     * Tests that the status and taskID are passed to the taskService correctly
     * when the request is rejected.
     */
    @Test
    public void testRejected(){
        Mockito.when(accessRequestRepository.getAccessRequestById(1L)).thenReturn(nonOwnerRequest);
        accessRequestService.rejectRequest("taskOne", 1L, "Another Reason");
        Map<String,Object> statusMap = new HashMap<>();
        statusMap.put("requestStatus", RequestStatus.APPROVAL_REJECTED);
        verify(accessRequestRepository, times(3)).save(Mockito.any(AccessRequest.class));
        verify(taskService,times(1)).setAssignee("taskOne","testUserId");
        verify(taskService,times(1)).complete("taskOne",statusMap);
    }


    @Test(expected = GatekeeperException.class)
    public void testRequestError() throws GatekeeperException{
        when(ownerRequestWrapper.getPlatform()).thenReturn("differentPlatform");
        accessRequestService.storeAccessRequest(ownerRequestWrapper);


    }


}
