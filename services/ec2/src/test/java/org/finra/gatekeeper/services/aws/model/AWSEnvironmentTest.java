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
public class AWSEnvironmentTest {

    @Test
    public void testConstructorGetters() {
        String account = "Hello";
        String region = "World";
        AWSEnvironment awsEnvironment = new AWSEnvironment(account, region);

        Assert.assertEquals("Test Account:", account, awsEnvironment.getAccount());
        Assert.assertEquals("Test Region:", region, awsEnvironment.getRegion());
    }

    @Test
    public void testEquals(){
        String account = "Hello";
        String region = "World";
        AWSEnvironment awsEnvironment1 = new AWSEnvironment(account, region);
        AWSEnvironment awsEnvironment2 = new AWSEnvironment(account, region);

        String account2 = "Hellos";
        String region2 = "Worlds";
        AWSEnvironment awsEnvironment3 = new AWSEnvironment(account2, region2);
        AWSEnvironment awsEnvironment4 = new AWSEnvironment(account, region2);


        Assert.assertEquals("Test Equals on self", awsEnvironment1, awsEnvironment1);
        Assert.assertNotEquals("Test Equals on different object", awsEnvironment1, new ArrayList());
        Assert.assertEquals("Test Equals on 2 different, but equal objects", awsEnvironment1, awsEnvironment2);
        Assert.assertNotEquals("Test Equals on different Account", awsEnvironment1, awsEnvironment3);
        Assert.assertNotEquals("Test Equals on different Region", awsEnvironment2, awsEnvironment4);
    }

    @Test
    public void testHashCode(){
        String account = "Hello";
        String region = "World";
        AWSEnvironment awsEnvironment1 = new AWSEnvironment(account, region);
        Assert.assertEquals("Test hashCode() method", Objects.hash(account, region), awsEnvironment1.hashCode());
    }

    @Test
    public void testToString(){
        String account = "Hello";
        String region = "World";
        AWSEnvironment awsEnvironment1 = new AWSEnvironment(account, region);
        Assert.assertEquals("Test toString() Method", MoreObjects.toStringHelper(AWSEnvironment.class).add("account", account).add("region", region).toString(), awsEnvironment1.toString());
    }
}
