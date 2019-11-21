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

package org.finra.gatekeeper.services.accessrequest.model;

import com.google.common.base.MoreObjects;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Tests for AccessRequest object
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessRequestTest {

    @Test
    public void testConstructor(){
        Integer hours = 45;
        String account = "Dev";
        String region = "us-west";
        String requestorId = "testId";
        String requestorName = "Test Dude";
        String requestorEmail = "Test@email.com";
        String requestReason = "Test Request Reason";
        String approverComments = "Test Approver Comments";
        String approverName = "Test Approver";
        String approverId = "TestA";
        String platform = "Test platform";
        List<User> users = Arrays.asList(new User("theguy", "thatguy", "thatguy@email.com"));
        List<AWSInstance> instances = Arrays.asList(new AWSInstance("The dude", "TST", "i-tst", "127.0.0.1","Another state", "Platform1"));

        AccessRequest accessRequest = new AccessRequest(hours, account,  region, requestorId, requestorName, requestorEmail, users, instances, requestReason, approverComments, approverId, approverName, platform);

        Assert.assertEquals("Test Hours: ", hours, accessRequest.getHours());
        Assert.assertEquals("Test Account:", account, accessRequest.getAccount());
        Assert.assertEquals("Test Region:",  region, accessRequest.getRegion());
        Assert.assertEquals("Test Requestor Id: ", requestorId, accessRequest.getRequestorId());
        Assert.assertEquals("Test Requestor Name:", requestorName, accessRequest.getRequestorName());
        Assert.assertEquals("Test Requestor Email:", requestorEmail, accessRequest.getRequestorEmail());
        Assert.assertEquals("Test Users", users, accessRequest.getUsers());
        Assert.assertEquals("Test Instances", instances, accessRequest.getInstances());
        Assert.assertEquals("Test Request Reason:", requestReason, accessRequest.getRequestReason());
        Assert.assertEquals("Test Approver Comments:", approverComments, accessRequest.getApproverComments());
        Assert.assertEquals("Test Approver ID: ", approverId, accessRequest.getActionedByUserId());
        Assert.assertEquals("Test Approver Name: ", approverName, accessRequest.getActionedByUserName());
        Assert.assertEquals("Test Platform:", platform, accessRequest.getPlatform());

    }

    @Test
    public void testSetterGetters(){
        Long id = 1L;
        Integer hours = 45;
        String account = "Dev";
        String region = "us-west";
        String requestorId = "testId";
        String requestorName = "Test Dude";
        String requestorEmail = "Test@email.com";
        String requestReason = "Test Request Reason";
        String approverComments = "Test Approver Comments";
        String approverName = "Test Approver";
        String approverId = "TestA";
        String platform = "Test platform";
        List<User> users = Arrays.asList(new User("theguy","thatguy", "thatguy@email.com"));
        List<AWSInstance> instances = Arrays.asList(new AWSInstance("The dude", "TST", "i-tst", "127.0.0.1","Another state", "Platform1"));

        AccessRequest accessRequest = new AccessRequest()
                .setId(id)
                .setHours(hours)
                .setAccount(account)
                .setRegion(region)
                .setRequestorId(requestorId)
                .setRequestorName(requestorName)
                .setRequestorEmail(requestorEmail)
                .setUsers(users)
                .setInstances(instances)
                .setRequestReason(requestReason)
                .setApproverComments(approverComments)
                .setActionedByUserId(approverId)
                .setActionedByUserName(approverName)
                .setPlatform(platform);

        Assert.assertEquals("Test Id: ", id, accessRequest.getId());
        Assert.assertEquals("Test Hours: ", hours, accessRequest.getHours());
        Assert.assertEquals("Test Account:", account, accessRequest.getAccount());
        Assert.assertEquals("Test Region:",  region, accessRequest.getRegion());
        Assert.assertEquals("Test Requestor Id: ", requestorId, accessRequest.getRequestorId());
        Assert.assertEquals("Test Requestor Name:", requestorName, accessRequest.getRequestorName());
        Assert.assertEquals("Test Requestor Email:", requestorEmail, accessRequest.getRequestorEmail());
        Assert.assertEquals("Test Users", users, accessRequest.getUsers());
        Assert.assertEquals("Test Instances", instances, accessRequest.getInstances());
        Assert.assertEquals("Test Request Reason:", requestReason, accessRequest.getRequestReason());
        Assert.assertEquals("Test Approver Comments:", approverComments, accessRequest.getApproverComments());
        Assert.assertEquals("Test Approver ID: ", approverId, accessRequest.getActionedByUserId());
        Assert.assertEquals("Test Approver Name: ", approverName, accessRequest.getActionedByUserName());
        Assert.assertEquals("Test Platform:", platform, accessRequest.getPlatform());

    }

    @Test
    public void testEquals(){
        Long id = 1L;
        Integer hours = 45;
        String account = "Dev";
        String region = "us-west";
        String requestorId = "testId";
        String requestorName = "Test Dude";
        String requestorEmail = "Test@email.com";
        String requestReason = "Test Request Reason";
        String approverComments = "Test Approver Comments";
        String approverName = "Test Approver";
        String approverId = "TestA";
        String platform = "Test platform";


        List<User> users = Arrays.asList(new User("theguy", "thatguy", "thatguy@email.com"));
        List<User> users2 = Arrays.asList(new User("theotherguy", "thatotherguy", "thatotherguy@email.com"));

        List<AWSInstance> instances = Arrays.asList(new AWSInstance("The dude", "TST", "i-tst", "127.0.0.1","Another state","Platform1"));
        List<AWSInstance> instances2 = Arrays.asList(new AWSInstance("The dude2", "TST2", "i2-tst", "127.0.0.2","Different state","Platform2"));

        AccessRequest accessRequest = new AccessRequest(hours, account,  region, requestorId, requestorName, requestorEmail, users, instances,requestReason, approverComments,approverId, approverName,platform);
        accessRequest.setId(id);
        AccessRequest accessRequest2 = accessRequest;
        AccessRequest accessRequest3 = new AccessRequest(hours, account,  region, requestorId, requestorName, requestorEmail, users, instances,requestReason, approverComments,approverId, approverName,platform);
        accessRequest3.setId(id);
        Assert.assertEquals("Same address space", accessRequest, accessRequest2);
        Assert.assertEquals("Different Objects same values", accessRequest, accessRequest3);
        /*Negatives*/
        Assert.assertNotEquals("Different Object Types", accessRequest, "Hello World");
        Assert.assertNotEquals("Different ids", accessRequest.setId(2L), accessRequest3);
        accessRequest.setId(id);
        Assert.assertNotEquals("Different hours", accessRequest.setHours(99), accessRequest3);
        accessRequest.setHours(hours);
        Assert.assertNotEquals("Different accounts", accessRequest.setAccount(""), accessRequest3);
        accessRequest.setAccount(account);
        Assert.assertNotEquals("Different Region", accessRequest.setRegion(""), accessRequest3);
        accessRequest.setRegion(region);
        Assert.assertNotEquals("Different RequestorId", accessRequest.setRequestorId(""), accessRequest3);
        accessRequest.setRequestorId(requestorId);
        Assert.assertNotEquals("Different RequestorName", accessRequest.setRequestorName(""), accessRequest3);
        accessRequest.setRequestorName(requestorName);
        Assert.assertNotEquals("Different RequestorEmail", accessRequest.setRequestorEmail(""), accessRequest3);
        accessRequest.setRequestorEmail(requestorEmail);
        Assert.assertNotEquals("Different Users", accessRequest.setUsers(users2), accessRequest3);
        accessRequest.setUsers(users);
        Assert.assertNotEquals("Different Instances", accessRequest.setInstances(instances2), accessRequest3);
        accessRequest.setInstances(instances);
        Assert.assertNotEquals("Different Reasons", accessRequest.setRequestReason("Different Reason"), accessRequest3);
        accessRequest.setRequestReason(requestReason);
        Assert.assertNotEquals("Different Comments", accessRequest.setApproverComments("Different Comment"), accessRequest3);
        accessRequest.setApproverComments(approverComments);
        Assert.assertNotEquals("Different Approver Id", accessRequest.setActionedByUserId("Test B"), accessRequest3);
        accessRequest.setActionedByUserId(approverId);
        Assert.assertNotEquals("Different Approver Name", accessRequest.setActionedByUserName("TestB"), accessRequest3);
        accessRequest.setActionedByUserId(approverName);
        Assert.assertNotEquals("Different Platforms", accessRequest.setPlatform("Different Platform"), accessRequest3);
        accessRequest.setPlatform(platform);



    }

    @Test
    public void testHashCode(){
        Long id = 1L;
        Integer hours = 45;
        String account = "Dev";
        String region = "us-west";
        String requestorId = "testId";
        String requestorName = "Test Dude";
        String requestorEmail = "Test@email.com";
        String ticketId = "TEST-123";
        String requestReason = "Test Request Reason";
        String approverComments = "Test Approver Comments";
        String approverName = "Test Approver";
        String approverId = "TestA";
        String platform = "Test platform";


        List<User> users = Arrays.asList(new User("theguy", "thatguy", "thatguy@email.com"));
        List<AWSInstance> instances = Arrays.asList(new AWSInstance("The dude", "TST", "i-tst", "127.0.0.1","Another state","Platform1"));

        AccessRequest accessRequest = new AccessRequest()
                .setId(id)
                .setHours(hours)
                .setAccount(account)
                .setRegion(region)
                .setRequestorId(requestorId)
                .setRequestorName(requestorName)
                .setRequestorEmail(requestorEmail)
                .setUsers(users)
                .setInstances(instances)
                .setTicketId(ticketId)
                .setRequestReason(requestReason)
                .setApproverComments(approverComments)
                .setActionedByUserId(approverId)
                .setActionedByUserName(approverName)
                .setPlatform(platform);

        Assert.assertEquals("Testing hashCode()",
                Objects.hash(id,
                        requestorId,
                        requestorName,
                        requestorEmail,
                        account,
                        region,
                        approverComments,
                        approverId,
                        approverName,
                        ticketId,
                        requestReason,
                        platform,
                        hours,
                        users,
                        instances),
                accessRequest.hashCode());

    }

    @Test
    public void testToString(){
        Long id = 1L;
        Integer hours = 45;
        String account = "Dev";
        String region = "us-west";
        String requestorId = "testId";
        String requestorName = "Test Dude";
        String requestorEmail = "Test@email.com";
        String ticketId = "TEST-123";
        String requestReason = "Test Request Reason";
        String approverComments = "Test Approver Comments";
        String approverName = "Test Approver";
        String approverId = "TestA";
        String platform = "Test platform";


        List<User> users = Arrays.asList(new User("theguy", "thatguy", "thatguy@email.com"));
        List<AWSInstance> instances = Arrays.asList(new AWSInstance("The dude", "TST", "i-tst", "127.0.0.1","Another state","Platform1"));

        AccessRequest accessRequest = new AccessRequest()
                .setId(id)
                .setHours(hours)
                .setAccount(account)
                .setRegion(region)
                .setRequestorId(requestorId)
                .setRequestorName(requestorName)
                .setRequestorEmail(requestorEmail)
                .setUsers(users)
                .setInstances(instances)
                .setTicketId(ticketId)
                .setRequestReason(requestReason)
                .setApproverComments(approverComments)
                .setActionedByUserId(approverId)
                .setActionedByUserName(approverName)
                .setPlatform(platform);

        String exp = MoreObjects.toStringHelper(AccessRequest.class)
                .add("ID", id)
                .add("Requestor ID:", requestorId)
                .add("Requestor Name", requestorName)
                .add("Requestor Email", requestorEmail)
                .add("Account", account)
                .add("Region", region)
                .add("Hours", hours)
                .add("Users", users)
                .add("Instances", instances)
                .add("Ticket ID", ticketId)
                .add("Request Reason", requestReason)
                .add("Approver Comments", approverComments)
                .add("Actioned By Id", approverId)
                .add("Actioned By Name", approverName)
                .add("Platform", platform)
                .toString();

        Assert.assertEquals("Testing toString()", exp, accessRequest.toString());

    }
}
