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

/**
 *
 * Tests for AccessRequest object
 */
@RunWith(MockitoJUnitRunner.class)
public class AWSInstanceTest {

    @Test
    public void testConstructor(){
        String name = "The Dude";
        String application = "TST";
        String instanceId = "i-tst";
        String ip = "127.0.0.1";
        String state = "Running";
        String platform ="Linux";

        AWSInstance awsInstance = new AWSInstance(name, application, instanceId, ip,state,platform);

        Assert.assertEquals("Test Name: ", name, awsInstance.getName());
        Assert.assertEquals("Test APPLICATION:", application, awsInstance.getApplication());
        Assert.assertEquals("Test Instance ID:",  instanceId, awsInstance.getInstanceId());
        Assert.assertEquals("Test IP: ", ip, awsInstance.getIp());


    }

    @Test
    public void testSetterGetters(){
        Long id = 1L;
        String name = "The Dude";
        String application = "TST";
        String instanceId = "i-tst";
        String ip = "127.0.0.1";
        String platform ="Linux";

        AWSInstance awsInstance = new AWSInstance()
                .setId(id)
                .setName(name)
                .setApplication(application)
                .setInstanceId(instanceId)
                .setIp(ip)
                .setPlatform(platform);

        Assert.assertEquals("Test Id: ", id, awsInstance.getId());
        Assert.assertEquals("Test Name: ", name, awsInstance.getName());
        Assert.assertEquals("Test Application", application, awsInstance.getApplication());
        Assert.assertEquals("Test Instance ID:",  instanceId, awsInstance.getInstanceId());
        Assert.assertEquals("Test IP: ", ip, awsInstance.getIp());
        Assert.assertEquals("Test Platform: ", platform, awsInstance.getPlatform());

    }

    @Test
    public void testEquals(){
        Long id = 1L;
        String name = "The Dude";
        String application = "TST";
        String instanceId = "i-tst";
        String ip = "127.0.0.1";
        String state = "Running";
        String platform ="Linux";

        AWSInstance awsInstance = new AWSInstance(name, application, instanceId, ip, state,platform);
        awsInstance.setId(id);
        AWSInstance awsInstance2 = awsInstance;
        AWSInstance awsInstance3 = new AWSInstance(name, application, instanceId, ip, state,platform);
        awsInstance3.setId(id);
        Assert.assertEquals("Same address space", awsInstance, awsInstance2);
        Assert.assertEquals("Different Objects same values", awsInstance, awsInstance3);
        /*Negatives*/
        Assert.assertNotEquals("Different Object Types", awsInstance, "Hello World");
        Assert.assertNotEquals("Different ids", awsInstance.setId(2L), awsInstance3);
        awsInstance.setId(id);
        Assert.assertNotEquals("Different name", awsInstance.setName(""), awsInstance3);
        awsInstance.setName(name);
        Assert.assertNotEquals("Different application", awsInstance.setApplication(""), awsInstance3);
        awsInstance.setApplication(application);
        Assert.assertNotEquals("Different instance Id", awsInstance.setInstanceId(""), awsInstance3);
        awsInstance.setInstanceId(instanceId);
        Assert.assertNotEquals("Different RequestorId", awsInstance.setIp(""), awsInstance3);
        awsInstance.setIp(ip);
        Assert.assertNotEquals("Different Platform", awsInstance.setPlatform(""), awsInstance3);
        awsInstance.setIp(platform);
    }

    @Test
    public void testHashCode(){
        String name = "The Dude";
        String application = "TST";
        String instanceId = "i-tst";
        String ip = "127.0.0.1";
        String platform ="Linux";


        AWSInstance awsInstance = new AWSInstance()
                .setId(1L)
                .setName(name)
                .setApplication(application)
                .setInstanceId(instanceId)
                .setIp(ip)
                .setPlatform(platform);

        AWSInstance awsInstance2 = new AWSInstance()
                .setId(2L)
                .setName(name)
                .setApplication(application)
                .setInstanceId(instanceId)
                .setIp(ip)
                .setPlatform(platform);


        Assert.assertNotEquals(awsInstance.hashCode(), awsInstance2.hashCode());

    }

    @Test
    public void testToString(){
        Long id = 1L;
        String name = "TheDude";
        String application = "TST";
        String instanceId = "i-tst";
        String ip = "127.0.0.1";
        String status = "testStatus";
        String platform ="Linux";

        AWSInstance awsInstance = new AWSInstance()
                .setId(id)
                .setName(name)
                .setApplication(application)
                .setInstanceId(instanceId)
                .setIp(ip)
                .setStatus(status)
                .setPlatform(platform);


        String exp =  MoreObjects.toStringHelper(AWSInstance.class)
                .add("ID", id)
                .add("Instance Application", application)
                .add("Instance Name", name)
                .add("Instance ID", instanceId)
                .add("Instance IP", ip)
                .add("Status", status)
                .add("Platform", platform)
                .toString();

        Assert.assertEquals("Testing toString()", exp, awsInstance.toString());

    }
}
