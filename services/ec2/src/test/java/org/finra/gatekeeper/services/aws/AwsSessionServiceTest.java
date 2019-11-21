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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClient;
import org.finra.gatekeeper.common.properties.GatekeeperAwsProperties;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.finra.gatekeeper.common.services.account.model.Region;
import org.finra.gatekeeper.services.aws.factory.AwsSessionFactory;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


/**
 * Tests for AWS SSM Service
 */
@Configuration
@ActiveProfiles("unit-test")
@RunWith(MockitoJUnitRunner.class)
public class AwsSessionServiceTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private ClientConfiguration clientConfiguration;

    @Mock
    private AccountInformationService accountInformationService;

    @Mock
    private AWSSecurityTokenServiceClient awsSecurityTokenServiceClient;

    @Mock
    private AmazonEC2Client amazonEC2Client;

    @Mock
    private AWSSimpleSystemsManagementClient awsSimpleSystemsManagementClient;

    @Mock
    private AwsSessionFactory awsSessionFactory;

    @Mock
    private GatekeeperAwsProperties gatekeeperAwsProperties;

    @InjectMocks
    private AwsSessionService awsSessionService;

    private AWSEnvironment awsEnvironment;

    @Before
    public void before() {
        awsEnvironment = new AWSEnvironment("Dev", "us-west-2");
        Mockito.when(gatekeeperAwsProperties.getSessionTimeout()).thenReturn(900000);
        Mockito.when(gatekeeperAwsProperties.getSessionTimeoutPad()).thenReturn(60000);

        List<Region> regions = new ArrayList<>();
        Region testRegion1 = new Region();
        Region testRegion2 = new Region();
        testRegion1.setName("us-west-2");
        testRegion2.setName("us-east-1");
        regions.add(testRegion1);
        regions.add(testRegion2);
        Account fakeAccount = new Account();
        fakeAccount.setAccountId("123");
        fakeAccount.setAlias("hello");
        fakeAccount.setRegions(regions);
        fakeAccount.setSdlc("Test");
        fakeAccount.setName("Test Account");

        AssumeRoleResult fakeRoleResult = new AssumeRoleResult();
        Credentials fakeFreshCredentials = new Credentials();   // ( ͡° ͜ʖ ͡°)
        fakeFreshCredentials.setAccessKeyId("testing");
        fakeFreshCredentials.setSecretAccessKey("s3cr3t");
        fakeFreshCredentials.setSessionToken("s35510nt0k3n");
        fakeRoleResult.setCredentials(fakeFreshCredentials);
        when(accountInformationService.getAccountByAlias("Dev")).thenReturn(fakeAccount);
        when(awsSecurityTokenServiceClient.assumeRole(any())).thenReturn(fakeRoleResult);
        when(awsSessionFactory.createEc2Session(any(), any())).thenReturn(amazonEC2Client);
        when(awsSessionFactory.createSsmSession(any(), any())).thenReturn(awsSimpleSystemsManagementClient);


    }

    @Test
    public void testGetEc2Session(){
        AmazonEC2 client = awsSessionService.getEC2Session(awsEnvironment);
        Assert.assertNotNull("Verify EC2 Session is fetched", client );
        Mockito.verify(awsSessionFactory, times(1)).createEc2Session(anyObject(), eq(awsEnvironment.getRegion()));
    }

    @Test
    public void testGetSsmSession(){
        AWSSimpleSystemsManagement client = awsSessionService.getSsmSession(awsEnvironment);
        Assert.assertNotNull("Verify Ssm Session is fetched", client);
        Mockito.verify(awsSessionFactory, times(1)).createSsmSession(anyObject(), eq(awsEnvironment.getRegion()));
    }

    /**
     * Verifies the gatekeeper exception gets tossed if a bad environment is provided.
     */
    @Test
    public void testExceptionThrown(){
        String env = "Test";
        awsEnvironment = new AWSEnvironment(env, "us-east-2");
        when(accountInformationService.getAccountByAlias("Test")).thenReturn(null);
        try {
            awsSessionService.getEC2Session(awsEnvironment);
        }catch(Exception e){
            Assert.assertEquals("Message with no alias is correct ", "org.finra.gatekeeper.exception.GatekeeperException: No account found with alias: " + env, e.getMessage());
        }
    }

}
