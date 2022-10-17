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
package org.finra.gatekeeper.services.accessrequest.delegates;

import com.amazonaws.services.rds.model.DBCluster;
import org.activiti.engine.EngineServices;
import org.activiti.engine.ManagementService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.runtime.Job;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.model.RoleType;
import org.finra.gatekeeper.services.accessrequest.AccessRequestService;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.aws.RdsLookupService;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.DatabaseType;
import org.finra.gatekeeper.services.db.DatabaseConnectionService;
import org.finra.gatekeeper.services.email.EmailServiceWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class RevokeAccessServiceTaskTest {

    @Mock
    private EmailServiceWrapper emailServiceWrapper;
    @Mock
    private DatabaseConnectionService databaseConnectionService;
    @Mock
    private RdsLookupService rdsLookupService;
    @Mock
    private ManagementService managementService;
    @Mock
    private AccessRequestService accessRequestService;
    @Mock
    private MockDelegateExecution mockDelegateExecution;
    @Mock
    private MockJob mockJob;
    @Mock
    private JobQueryImpl jobQuery;

    @InjectMocks
    private RevokeAccessServiceTask revokeAccessServiceTask;

    private AccessRequest testRequest;

    @Before
    public void before(){
        testRequest = new AccessRequest()
                .setId(1L)
                .setDays(1)
                .setRoles(Arrays.asList(new UserRole()
                        .setRole("readonly")
                        .setId(11L)))
                .setAccount("test")
                .setAccountSdlc("dev")
                .setAwsRdsInstances(Arrays.asList(new AWSRdsDatabase()
                        .setEngine("postgres")
                        .setEndpoint("jdbc:testEndpont:5432")
                        .setApplication("TESTAPP")
                        .setDbName("testdb")
                        .setInstanceId("db-1234")
                        .setName("test-database")
                        .setStatus("APPROVED")
                        .setArn("arn:12345")
                        .setDatabaseType(DatabaseType.RDS)))
                .setUsers(Arrays.asList(new User()
                        .setId(111L)
                        .setUserId("gk_test")
                        .setEmail("testuser@company.com")
                        .setName("Test User")
                ))
                .setRequestorName("Test User")
                .setRequestReason("Just Unit Testing")
                .setRegion("us-east-1")
                .setRequestorId("test");

        Mockito.when(mockDelegateExecution.getProcessInstanceId()).thenReturn("1");
        Mockito.when(managementService.createJobQuery()).thenReturn(jobQuery);
        Mockito.when(jobQuery.processInstanceId(Mockito.any())).thenReturn(jobQuery);
        Mockito.when(jobQuery.singleResult()).thenReturn(mockJob);
        Mockito.when(mockJob.getRetries()).thenReturn(4);
        Mockito.when(mockDelegateExecution.getVariable(Mockito.any())).thenReturn(testRequest);
    }

    @Test
    public void testRevokeAccessCallsWithExpectedValues() throws Exception {
        revokeAccessServiceTask.execute(mockDelegateExecution);
        verifyRevoke();
    }

    @Test
    public void testRevokeAccessCallGlobalCluster() throws Exception {
        ArgumentCaptor<AWSEnvironment> awsEnvironmentArgumentCaptor = ArgumentCaptor.forClass(AWSEnvironment.class);
        ArgumentCaptor<String> clusterIdCaptor = ArgumentCaptor.forClass(String.class);
        DBCluster testCluster = new DBCluster().withEndpoint("someendpoint").withPort(5432);

        Mockito.when(rdsLookupService.getPrimaryClusterForGlobalCluster(Mockito.any(), Mockito.any())).thenReturn(Optional.of(testCluster));
        testRequest.getAwsRdsInstances().get(0).setDatabaseType(DatabaseType.AURORA_GLOBAL);

        revokeAccessServiceTask.execute(mockDelegateExecution);
        verifyRevoke();
        Mockito.verify(rdsLookupService, Mockito.times(1)).getPrimaryClusterForGlobalCluster(
                awsEnvironmentArgumentCaptor.capture(), clusterIdCaptor.capture());
        Assert.assertEquals(new AWSEnvironment(testRequest.getAccount(), testRequest.getRegion(), testRequest.getAccountSdlc()), awsEnvironmentArgumentCaptor.getValue());
        Assert.assertEquals(testRequest.getAwsRdsInstances().get(0).getName(), clusterIdCaptor.getValue());

    }

    private void verifyRevoke() throws GKUnsupportedDBException {
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> accountNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> databaseCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);

        Mockito.verify(accessRequestService, Mockito.times(1)).getLiveRequestsForUserOnDatabase(
                userIdCaptor.capture(), accountNameCaptor.capture(), databaseCaptor.capture(), roleCaptor.capture());

        Assert.assertEquals(testRequest.getUsers().get(0).getUserId(), userIdCaptor.getValue());
        Assert.assertEquals(testRequest.getAccount(), accountNameCaptor.getValue());
        Assert.assertEquals(testRequest.getAwsRdsInstances().get(0).getName(), databaseCaptor.getValue());
        Assert.assertEquals(testRequest.getRoles().get(0), roleCaptor.getValue());

        ArgumentCaptor<AWSRdsDatabase> dbArgumentCaptor = ArgumentCaptor.forClass(AWSRdsDatabase.class);
        ArgumentCaptor<AWSEnvironment> awsEnvironmentArgumentCaptor = ArgumentCaptor.forClass(AWSEnvironment.class);
        ArgumentCaptor<RoleType> roleTypeArgumentCaptor = ArgumentCaptor.forClass(RoleType.class);

        Mockito.verify(databaseConnectionService, Mockito.times(1))
                .revokeAccess(dbArgumentCaptor.capture(),awsEnvironmentArgumentCaptor.capture(),roleTypeArgumentCaptor.capture(),userIdCaptor.capture());

        Assert.assertEquals(testRequest.getAwsRdsInstances().get(0), dbArgumentCaptor.getValue());
        Assert.assertEquals(new AWSEnvironment(testRequest.getAccount(), testRequest.getRegion(), testRequest.getAccountSdlc()), awsEnvironmentArgumentCaptor.getValue());
        Assert.assertEquals(testRequest.getRoles().get(0), roleCaptor.getValue());
        Assert.assertEquals(testRequest.getUsers().get(0).getUserId(), userIdCaptor.getValue());
    }

    private class MockDelegateExecution implements DelegateExecution {
        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getProcessInstanceId() {
            return null;
        }

        @Override
        public String getEventName() {
            return null;
        }

        @Override
        public String getBusinessKey() {
            return null;
        }

        @Override
        public String getProcessBusinessKey() {
            return null;
        }

        @Override
        public String getProcessDefinitionId() {
            return null;
        }

        @Override
        public String getParentId() {
            return null;
        }

        @Override
        public String getSuperExecutionId() {
            return null;
        }

        @Override
        public String getCurrentActivityId() {
            return null;
        }

        @Override
        public String getCurrentActivityName() {
            return null;
        }

        @Override
        public String getTenantId() {
            return null;
        }

        @Override
        public EngineServices getEngineServices() {
            return null;
        }

        @Override
        public Map<String, Object> getVariables() {
            return null;
        }

        @Override
        public Map<String, VariableInstance> getVariableInstances() {
            return null;
        }

        @Override
        public Map<String, Object> getVariables(Collection<String> collection) {
            return null;
        }

        @Override
        public Map<String, VariableInstance> getVariableInstances(Collection<String> collection) {
            return null;
        }

        @Override
        public Map<String, Object> getVariables(Collection<String> collection, boolean b) {
            return null;
        }

        @Override
        public Map<String, VariableInstance> getVariableInstances(Collection<String> collection, boolean b) {
            return null;
        }

        @Override
        public Map<String, Object> getVariablesLocal() {
            return null;
        }

        @Override
        public Map<String, VariableInstance> getVariableInstancesLocal() {
            return null;
        }

        @Override
        public Map<String, Object> getVariablesLocal(Collection<String> collection) {
            return null;
        }

        @Override
        public Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> collection) {
            return null;
        }

        @Override
        public Map<String, Object> getVariablesLocal(Collection<String> collection, boolean b) {
            return null;
        }

        @Override
        public Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> collection, boolean b) {
            return null;
        }

        @Override
        public Object getVariable(String s) {
            return null;
        }

        @Override
        public VariableInstance getVariableInstance(String s) {
            return null;
        }

        @Override
        public Object getVariable(String s, boolean b) {
            return null;
        }

        @Override
        public VariableInstance getVariableInstance(String s, boolean b) {
            return null;
        }

        @Override
        public Object getVariableLocal(String s) {
            return null;
        }

        @Override
        public VariableInstance getVariableInstanceLocal(String s) {
            return null;
        }

        @Override
        public Object getVariableLocal(String s, boolean b) {
            return null;
        }

        @Override
        public VariableInstance getVariableInstanceLocal(String s, boolean b) {
            return null;
        }

        @Override
        public <T> T getVariable(String s, Class<T> aClass) {
            return null;
        }

        @Override
        public <T> T getVariableLocal(String s, Class<T> aClass) {
            return null;
        }

        @Override
        public Set<String> getVariableNames() {
            return null;
        }

        @Override
        public Set<String> getVariableNamesLocal() {
            return null;
        }

        @Override
        public void setVariable(String s, Object o) {

        }

        @Override
        public void setVariable(String s, Object o, boolean b) {

        }

        @Override
        public Object setVariableLocal(String s, Object o) {
            return null;
        }

        @Override
        public Object setVariableLocal(String s, Object o, boolean b) {
            return null;
        }

        @Override
        public void setVariables(Map<String, ?> map) {

        }

        @Override
        public void setVariablesLocal(Map<String, ?> map) {

        }

        @Override
        public boolean hasVariables() {
            return false;
        }

        @Override
        public boolean hasVariablesLocal() {
            return false;
        }

        @Override
        public boolean hasVariable(String s) {
            return false;
        }

        @Override
        public boolean hasVariableLocal(String s) {
            return false;
        }

        @Override
        public void createVariableLocal(String s, Object o) {

        }

        @Override
        public void removeVariable(String s) {

        }

        @Override
        public void removeVariableLocal(String s) {

        }

        @Override
        public void removeVariables(Collection<String> collection) {

        }

        @Override
        public void removeVariablesLocal(Collection<String> collection) {

        }

        @Override
        public void removeVariables() {

        }

        @Override
        public void removeVariablesLocal() {

        }
    }

    private class MockJob implements Job {

        @Override
        public String getId() {
            return null;
        }

        @Override
        public Date getDuedate() {
            return null;
        }

        @Override
        public String getProcessInstanceId() {
            return null;
        }

        @Override
        public String getExecutionId() {
            return null;
        }

        @Override
        public String getProcessDefinitionId() {
            return null;
        }

        @Override
        public int getRetries() {
            return 0;
        }

        @Override
        public String getExceptionMessage() {
            return null;
        }

        @Override
        public String getTenantId() {
            return null;
        }
    }
}
