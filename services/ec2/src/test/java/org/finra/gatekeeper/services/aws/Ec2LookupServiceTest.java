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

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import org.finra.gatekeeper.configuration.properties.GatekeeperEC2Properties;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperAWSInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.mockito.Mockito.when;



/**
 * Tests for AWS EC2 Service
 */
@Configuration
@ActiveProfiles("unit-test")
@RunWith(MockitoJUnitRunner.class)
public class Ec2LookupServiceTest {
    @Mock
    private AwsSessionService awsSessionService;

    @Mock
    private AmazonEC2Client amazonEC2Client;

    @Mock
    private SsmService ssmService;

    @Mock
    private GatekeeperEC2Properties gatekeeperEC2Properties;

    @InjectMocks
    private Ec2LookupService Ec2LookupService;

    @Captor
    ArgumentCaptor<List<String>> argumentCaptor;

    private DescribeInstancesResult mockedResult;
    private AWSEnvironment awsEnvironment;

    private Reservation fakeInstance(String instanceID, String instanceIP, String instanceName, String instanceApplication, String platform) {
        Reservation container = new Reservation();
        List<Instance> instances = new ArrayList<>();
        Instance i = new Instance();
        List<Tag> tags = new ArrayList<>();
        i.setInstanceId(instanceID);
        i.setPrivateIpAddress(instanceIP);
        Tag nameTag = new Tag();
        nameTag.setKey("Name");
        nameTag.setValue(instanceName);
        Tag applicationTag = new Tag();
        applicationTag.setKey("Application");
        applicationTag.setValue(instanceApplication);
        tags.add(applicationTag);
        tags.add(nameTag);
        i.setTags(tags);
        i.setPlatform(platform);
        instances.add(i);
        container.setInstances(instances);

        return container;
    }

    @Before
    public void before() {
        awsEnvironment = new AWSEnvironment("Dev", "us-west-2");

        mockedResult = new DescribeInstancesResult();
        mockedResult.setReservations(Arrays.asList(new Reservation[]{
                fakeInstance("i-12345", "1.2.3.4", "TestOne", "TEP","Linux"),
                fakeInstance("i-abcde", "123.2.3.4", "TestOneTwo", "TST", "Linux"),
                fakeInstance("i-123ab", "456.2.3.4", "HelloOne", "TEP", "Linux"),
                fakeInstance("i-456cd", "123.22.3.4", "HelloTwo", "TEP", "Linux"),
                fakeInstance("i-12347", "132.23.43.4", "TestThree", "TEP", "Linux")
        }));

        Mockito.when(gatekeeperEC2Properties.getAppIdentityTag()).thenReturn("Application");
        Mockito.when(awsSessionService.getEC2Session(any())).thenReturn(amazonEC2Client);
        Mockito.when(amazonEC2Client.describeInstances(any())).thenReturn(mockedResult);
    }

    @Test
    public void testSearchInstancesID() {
        List<GatekeeperAWSInstance> instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "INSTANCE ID", "123");
        Mockito.verify(ssmService, times(1)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 3, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "INSTANCE ID", "ab");
        Mockito.verify(ssmService, times(2)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 2, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment,  "Linux","INSTANCE ID", "i-");
        Mockito.verify(ssmService, times(3)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 5, argumentCaptor.getValue().size());
    }

    @Test
    public void testSearchInstancesApplication() {
        List<GatekeeperAWSInstance> instances = Ec2LookupService.getInstances(awsEnvironment,  "Linux","APPLICATION", "T");
        Mockito.verify(ssmService, times(1)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 5, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "APPLICATION", "TEP");
        Mockito.verify(ssmService, times(2)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 4, argumentCaptor.getValue().size());

    }

    @Test
    public void testSearchInstancesName() {
        List<GatekeeperAWSInstance> instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "NAME", "e");
        Mockito.verify(ssmService, times(1)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 5, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment,  "Linux","NAME", "one");
        Mockito.verify(ssmService, times(2)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 3, argumentCaptor.getValue().size());

    }

    @Test
    public void testSearchInstancesIP() {
        List<GatekeeperAWSInstance> instances = Ec2LookupService.getInstances(awsEnvironment,  "Linux","IP", ".4");
        Mockito.verify(ssmService, times(1)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 5, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment,  "Linux","IP", "132.23.43.4");
        Mockito.verify(ssmService, times(2)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 1, argumentCaptor.getValue().size());

    }

    @Test
    public void testSearchInstancesBad() {
        List<GatekeeperAWSInstance> instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "Derp", ".4");
        Assert.assertEquals("Test search result", 0, instances.size());

    }

    @Test
    public void testPlatformFilter(){
        mockedResult.setReservations(Arrays.asList(new Reservation[]{
                fakeInstance("i-12345", "1.2.3.4", "TestOne", "TEP","Linux"),
                fakeInstance("i-abcde", "123.2.3.4", "TestOneTwo", "TST", "Linux"),
                fakeInstance("i-123ab", "456.2.3.4", "HelloOne", "TEP", "Linux"),
                fakeInstance("i-456cd", "123.22.3.4", "HelloTwo", "TEP", "Linux"),
                fakeInstance("i-12345", "132.23.43.4", "TestThree", "TEP", "Windows")
        }));
        Mockito.when(amazonEC2Client.describeInstances(any())).thenReturn(mockedResult);
        List<GatekeeperAWSInstance> instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "IP", "1");
        Mockito.verify(ssmService, times(1)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 3, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment, "Windows", "IP", "1");
        Mockito.verify(ssmService, times(2)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 1, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "NAME", "T");
        Mockito.verify(ssmService, times(3)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 3, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment, "Windows", "NAME", "T");
        Mockito.verify(ssmService, times(4)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 1, argumentCaptor.getValue().size());


        instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "APPLICATION", "TEP");
        Mockito.verify(ssmService, times(5)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 3, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment, "Windows", "APPLICATION", "TEP");
        Mockito.verify(ssmService, times(6)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 1, argumentCaptor.getValue().size());


        instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "INSTANCE ID", "i-1");
        Mockito.verify(ssmService, times(7)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 2, argumentCaptor.getValue().size());

        instances = Ec2LookupService.getInstances(awsEnvironment, "Windows", "INSTANCE ID", "i-1");
        Mockito.verify(ssmService, times(8)).checkInstancesWithSsm(any(), argumentCaptor.capture());
        Assert.assertEquals("Test search result", 1, argumentCaptor.getValue().size());
    }

    @Test
    public void testDefaultStatus() {
        Map<String,String> statusMap = new HashMap<>();
        statusMap.put("i-12345",null);
        statusMap.put("i-abcde","Active");
        statusMap.put("i-456cd","Online");
        statusMap.put("i-123ab","Inactive");
        when(ssmService.checkInstancesWithSsm(any(),any())).thenReturn(statusMap);
        List<GatekeeperAWSInstance> instances = Ec2LookupService.getInstances(awsEnvironment, "Linux", "APPLICATION", "T");

        Assert.assertEquals("Test search result", 5, instances.size());
        for(GatekeeperAWSInstance instance : instances){
            if(statusMap.get(instance.getInstanceId())==null){
                Assert.assertEquals("Unknown should be default when the resulting lookup is null","Unknown",instance.getSsmStatus());
            }else{
                Assert.assertEquals("Statuses should match",statusMap.get(instance.getInstanceId()),instance.getSsmStatus());
            }
        }
    }

    @Test
    public void testCheckIfInstancesExistOrTerminatedAllFalse(){
        mockedResult.setReservations(Arrays.asList(new Reservation[]{
                fakeInstance("i-12345", "1.2.3.4", "TestOne", "TEP","Linux"),
                fakeInstance("i-abcde", "123.2.3.4", "TestOneTwo", "TST", "Linux"),
                fakeInstance("i-123ab", "456.2.3.4", "HelloOne", "TEP", "Linux"),
                fakeInstance("i-456cd", "123.22.3.4", "HelloTwo", "TEP", "Linux"),
        }));

        Mockito.when(amazonEC2Client.describeInstances(any())).thenReturn(mockedResult);

        List<String> instanceIds = Arrays.asList("i-12345", "i-abcde", "i-456cd", "i-123ab");
        Map<String, Boolean> result = Ec2LookupService.checkIfInstancesExistOrTerminated(awsEnvironment, instanceIds);


        //there should be 4 items in the collection
        Assert.assertEquals("There should be 4 elements in the map", instanceIds.size(), result.size());
        //all the entries in the map should be false
        Assert.assertTrue("No entry should be set to true", result.entrySet().stream().noneMatch(Map.Entry::getValue));
    }

    @Test
    public void testCheckIfInstancesExistOrTerminatedMixed(){
        mockedResult.setReservations(Arrays.asList(new Reservation[]{
                fakeInstance("i-12345", "1.2.3.4", "TestOne", "TEP","Linux"),
                fakeInstance("i-abcde", "123.2.3.4", "TestOneTwo", "TST", "Linux"),
                fakeInstance("i-456cd", "123.22.3.4", "HelloTwo", "TEP", "Linux"),
        }));

        Mockito.when(amazonEC2Client.describeInstances(any())).thenReturn(mockedResult);

        List<String> instanceIds = Arrays.asList( "i-12345", "i-abcde", "i-456cd", "i-123ab");
        Map<String, Boolean> result = Ec2LookupService.checkIfInstancesExistOrTerminated(awsEnvironment, instanceIds);

        //there should be 3 items in the collection
        Assert.assertEquals("The elements in the instance ID list are in the result map", instanceIds.size(), result.size());

        Assert.assertTrue("The instance i-123ab is 'terminated'", result.get("i-123ab"));
        Assert.assertFalse("The instance i-12345 'exists'", result.get("i-12345"));
        Assert.assertFalse("The instance i-abcde 'exists'", result.get("i-abcde"));
        Assert.assertFalse("The instance i-456cd 'exists'", result.get("i-456cd"));

    }

}
