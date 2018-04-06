/*
 *
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

package org.finra.gatekeeper.configuration;

import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for custom stuff in PropertyConfig
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyConfigOverridePolicyTest {

    @InjectMocks
    private GatekeeperOverrideProperties overridePolicy;

    @Before
    public void init(){
        ReflectionTestUtils.setField(overridePolicy,"maxDays", 180);
        Map<String, Map<String, Map<String, Integer>>> mockOverrides = new  HashMap<>();
        Map<String, Map<String, Integer>> mockDev = new HashMap<>();
        Map<String, Integer> mockDba = new HashMap<>();
        mockDba.put("dev",1);
        Map<String, Integer> mockDatafix = new HashMap<>();
        mockDatafix.put("dev",7);
        Map<String, Integer> mockReadonly = new HashMap<>();
        mockReadonly.put("dev",180);

        mockDev.put("dba", mockDba);
        mockDev.put("datafix", mockDatafix);
        mockDev.put("readonly", mockReadonly);

        mockOverrides.put("dev", mockDev);
        mockOverrides.put("dba", new HashMap<>());

        ReflectionTestUtils.setField(overridePolicy,"overrides", mockOverrides);

    }

    /*
     * If no entry found in override map it should just return whatever the maximum value was set at via properties
     */
    @Test
    public void testOverrideNone(){
        Assert.assertEquals(Integer.valueOf(180), overridePolicy.getMaxDaysForRequest(GatekeeperRdsRole.DEV, Collections.singletonList(new UserRole("readonly")), "qa"));
    }

    /*
     * If there is just one entry with an override return that
     */
    @Test
    public void testOverrideOne(){
        Assert.assertEquals(Integer.valueOf(7), overridePolicy.getMaxDaysForRequest(GatekeeperRdsRole.DEV, Collections.singletonList(new UserRole("datafix")), "dev"));
    }

    /*
     * Lowest value should be returned of 1, 7, 180 => 1
     */
    @Test
    public void testOverrideMulti(){
        Assert.assertEquals(Integer.valueOf(1), overridePolicy.getMaxDaysForRequest(GatekeeperRdsRole.DEV, Arrays.asList(new UserRole("datafix"), new UserRole("dba"), new UserRole("readonly")), "dev"));
    }
}
