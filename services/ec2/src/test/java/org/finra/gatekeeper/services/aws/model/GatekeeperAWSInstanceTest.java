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

package org.finra.gatekeeper.services.aws.model;

import com.google.common.base.MoreObjects;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Tests for Account
 */
@RunWith(MockitoJUnitRunner.class)
public class GatekeeperAWSInstanceTest {

    @Test
    public void testConstructorGetters() {
        String instanceId = "i-h3110";
        String application = "TST";
        String name = "TSTBOX";
        String ip = "123.4.5.6";
        String platform="Linux";

        GatekeeperAWSInstance awsEnvironment = new GatekeeperAWSInstance(instanceId, application, name, ip, platform);

        Assert.assertEquals("Test Instance ID:", instanceId, awsEnvironment.getInstanceId());
        Assert.assertEquals("Test Application:", application, awsEnvironment.getApplication());
        Assert.assertEquals("Test Name:", name, awsEnvironment.getName());
        Assert.assertEquals("Test IP:", ip, awsEnvironment.getIp());
        Assert.assertEquals("Test Platform:", platform, awsEnvironment.getPlatform());

    }

    @Test
    public void testEquals(){
        String instanceId = "i-h3110";
        String application = "TST";
        String name = "TSTBOX";
        String ip = "123.4.5.6";
        String platform="Linux";

        GatekeeperAWSInstance awsInstance1 = new GatekeeperAWSInstance(instanceId, application, name, ip,platform);
        GatekeeperAWSInstance awsInstance2 = new GatekeeperAWSInstance(instanceId, application, name, ip,platform);

        String instanceId2 = "i-h310";
        String application2 = "TST2";
        String name2 = "TST2BOX";
        String ip2 = "1232.4.5.6";
        String platform2 = "Windows";

        GatekeeperAWSInstance awsInstance3 = new GatekeeperAWSInstance(instanceId2, application2, name2, ip2,platform2);
        GatekeeperAWSInstance awsInstance4 = new GatekeeperAWSInstance(instanceId, application2, name, ip,platform);
        GatekeeperAWSInstance awsInstance5 = new GatekeeperAWSInstance(instanceId, application, name2, ip,platform);
        GatekeeperAWSInstance awsInstance6 = new GatekeeperAWSInstance(instanceId, application, name, ip2,platform);
        GatekeeperAWSInstance awsInstance7 = new GatekeeperAWSInstance(instanceId, application, name, ip,platform2);


        Assert.assertEquals("Test Equals on self", awsInstance1, awsInstance1);
        Assert.assertNotEquals("Test Equals on different object", awsInstance1, new ArrayList());
        Assert.assertEquals("Test Equals on 2 different, but equal objects", awsInstance1, awsInstance2);
        Assert.assertNotEquals("Test Equals on different ID", awsInstance1, awsInstance3);
        Assert.assertNotEquals("Test Equals on different Application", awsInstance1, awsInstance4);
        Assert.assertNotEquals("Test Equals on different Name", awsInstance1, awsInstance5);
        Assert.assertNotEquals("Test Equals on different IP", awsInstance1, awsInstance6);
        Assert.assertNotEquals("Test Equals on different Platform", awsInstance1, awsInstance7);

    }

    @Test
    public void testHashCode(){
        String instanceId = "i-h3110";
        String application = "TST";
        String name = "TSTBOX";
        String ip = "123.4.5.6";
        String ssmStatus = "Unknown";
        String platform = "Linux";

        GatekeeperAWSInstance awsInstance1 = new GatekeeperAWSInstance(instanceId, application, name, ip, platform);

        Assert.assertEquals("Test hashCode()", Objects.hash(instanceId, application, name, ip, ssmStatus, platform), awsInstance1.hashCode());
    }

    @Test
    public void testToString(){
        String instanceId = "i-h3110";
        String application = "TST";
        String name = "TSTBOX";
        String ip = "123.4.5.6";
        String ssmStatus = "Unknown";
        String platform = "Linux";

        GatekeeperAWSInstance awsInstance1 = new GatekeeperAWSInstance(instanceId, application, name, ip, platform);

        String expStr = MoreObjects.toStringHelper(GatekeeperAWSInstance.class)
                .add("Instance ID", instanceId)
                .add("Application", application)
                .add("Name", name)
                .add("IP Address", ip)
                .add("SSM Status", ssmStatus)
                .add("Platform", platform)
                .toString();

        Assert.assertEquals("Test toString()", expStr, awsInstance1.toString());
    }
}
