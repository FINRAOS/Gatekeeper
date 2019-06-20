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
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

/**
 * Tests for custom stuff in PropertyConfig
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyConfigOverridePolicyTest {

    @InjectMocks
    private GatekeeperOverrideProperties overridePolicy;

    private Map<String, RoleMembership> roleMembershipMap;
    private RoleMembership roleMembership;
    private Map<String, Map<String, Integer>> mockDevOverridePolicy;
    private Map<String, Map<String, Integer>> mockOpsOverridePolicy;
    private Map<String, Map<String, Integer>> mockDbaOverridePolicy;
    private Map<String, Map<String, Integer>> mockApproverOverridePolicy;
    private Map<String, Map<String, Integer>> mockUnauthorizedOverridePolicy;

    @Before
    public void init(){
        ReflectionTestUtils.setField(overridePolicy,"maxDays", 180);
        Map<String, Map<String, Map<String, Integer>>> mockOverrides = new  HashMap<>();
        mockDevOverridePolicy = new HashMap<>();
        mockOpsOverridePolicy = new HashMap<>();
        mockDbaOverridePolicy = new HashMap<>();
        mockApproverOverridePolicy = new HashMap<>();
        mockUnauthorizedOverridePolicy = new HashMap<>();

        Map<String, Integer> mockDevDba = new HashMap<>();
        mockDevDba.put("dev",1);
        Map<String, Integer> mockDevDatafix = new HashMap<>();
        mockDevDatafix.put("dev",7);
        Map<String, Integer> mockDevReadonly = new HashMap<>();
        mockDevReadonly.put("dev",180);

        mockDevOverridePolicy.put("dba", mockDevDba);
        mockDevOverridePolicy.put("datafix", mockDevDatafix);
        mockDevOverridePolicy.put("readonly", mockDevReadonly);

        Map<String, Integer> mockOpsDba = new HashMap<>();
        Map<String, Integer> mockOpsDatafix = new HashMap<>();
        Map<String, Integer> mockOpsReadonly = new HashMap<>();
        mockOpsDba.put("dev",2);
        mockOpsDatafix.put("dev",3);
        mockOpsReadonly.put("dev",4);

        mockOpsOverridePolicy.put("dba", mockOpsDba);
        mockOpsOverridePolicy.put("datafix", mockOpsDatafix);
        mockOpsOverridePolicy.put("readonly", mockOpsReadonly);

        Map<String, Integer> mockDbaDba = new HashMap<>();
        Map<String, Integer> mockDbaDatafix = new HashMap<>();
        Map<String, Integer> mockDbaReadonly = new HashMap<>();

        mockDbaDba.put("dev",5);
        mockDbaDatafix.put("dev",6);
        mockDbaReadonly.put("dev",7);

        mockDbaOverridePolicy.put("dba", mockDbaDba);
        mockDbaOverridePolicy.put("datafix", mockDbaDatafix);
        mockDbaOverridePolicy.put("readonly", mockDbaReadonly);

        Map<String, Integer> mockApproverDba = new HashMap<>();
        Map<String, Integer> mockApproverDatafix = new HashMap<>();
        Map<String, Integer> mockApproverReadonly = new HashMap<>();

        mockApproverDba.put("dev",8);
        mockApproverDatafix.put("dev",9);
        mockApproverReadonly.put("dev",10);

        mockApproverOverridePolicy.put("dba", mockApproverDba);
        mockApproverOverridePolicy.put("datafix", mockApproverDatafix);
        mockApproverOverridePolicy.put("readonly", mockApproverReadonly);


        mockOverrides.put("dev", mockDevOverridePolicy);
        mockOverrides.put("dba", mockDbaOverridePolicy);
        mockOverrides.put("ops", mockOpsOverridePolicy);
        mockOverrides.put("approver", mockApproverOverridePolicy);
        mockOverrides.put("unauthorized", mockUnauthorizedOverridePolicy);

        roleMembershipMap = new HashMap<>();
        roleMembership = new RoleMembership();



        ReflectionTestUtils.setField(overridePolicy,"overrides", mockOverrides);

    }

    /*
     * If no entry found in override map it should just return whatever the maximum value was set at via properties
     */
    @Test
    public void testOverrideNone(){
        initDevRoleMembershipMap();
        Assert.assertEquals(Integer.valueOf(180), overridePolicy.getMaxDaysForRequest(roleMembership, Collections.singletonList(new UserRole("readonly")), "qa"));
    }

    /*
     * If there is just one entry with an override return that
     */
    @Test
    public void testOverrideOne(){
        initDevRoleMembershipMap();
        Assert.assertEquals(Integer.valueOf(7), overridePolicy.getMaxDaysForRequest(roleMembership, Collections.singletonList(new UserRole("datafix")), "dev"));
    }

    /*
     * Lowest value should be returned of 1, 7, 180 => 1
     */
    @Test
    public void testOverrideMulti(){
        initDevRoleMembershipMap();
        Assert.assertEquals(Integer.valueOf(1), overridePolicy.getMaxDaysForRequest(roleMembership, Arrays.asList(new UserRole("datafix"), new UserRole("dba"), new UserRole("readonly")), "dev"));
    }

    @Test
    public void testGetOverridePolicyApprover() {
        initDbaRoleMembershipMap();
        initDevRoleMembershipMap();
        initOpsRoleMembershipMap();
        roleMembershipMap.put("TEST", roleMembership);

        Map<String, Map<String, Map<String, Integer>>> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, true);
        Assert.assertEquals(mockApproverOverridePolicy, actualOverridePolicy.get("TEST"));
    }

    @Test
    public void testGetOverridePolicyDba() {
        initDbaRoleMembershipMap();
        roleMembershipMap.put("TEST", roleMembership);

        Map<String, Map<String, Map<String, Integer>>> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, false);
        Assert.assertEquals(mockDbaOverridePolicy, actualOverridePolicy.get("TEST"));
    }

    @Test
    public void testGetOverridePolicyDev() {
        initDevRoleMembershipMap();

        Map<String, Map<String, Map<String, Integer>>> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, false);
        Assert.assertEquals(mockDevOverridePolicy, actualOverridePolicy.get("TEST"));
    }

    @Test
    public void testGetOverridePolicyOps() {
        initOpsRoleMembershipMap();

        Map<String, Map<String, Map<String, Integer>>> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, false);
        Assert.assertEquals(mockOpsOverridePolicy, actualOverridePolicy.get("TEST"));
    }

    @Test
    public void testGetOverridePolicyUnauthorized() {
        initUnauthorizedMembershipMap();

        Map<String, Map<String, Map<String, Integer>>> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, false);
        Assert.assertEquals(mockUnauthorizedOverridePolicy, actualOverridePolicy.get("TEST"));
    }


    private void initDbaRoleMembershipMap() {
        Set<String> sdlcs = new HashSet<>();
        sdlcs.add("DEV");
        sdlcs.add("QA");
        sdlcs.add("PROD");
        roleMembership.getRoles().put(GatekeeperRdsRole.DBA, sdlcs);
        roleMembershipMap.put("TEST", roleMembership);
    }

    private void initDevRoleMembershipMap() {
        Set<String> sdlcs = new HashSet<>();
        sdlcs.add("DEV");
        sdlcs.add("QA");
        roleMembership.getRoles().put(GatekeeperRdsRole.DEV, sdlcs);
        roleMembershipMap.put("TEST", roleMembership);
    }

    private void initOpsRoleMembershipMap() {
        Set<String> sdlcs = new HashSet<>();
        sdlcs.add("DEV");
        sdlcs.add("QA");
        sdlcs.add("PROD");
        roleMembership.getRoles().put(GatekeeperRdsRole.OPS, sdlcs);
        roleMembershipMap.put("TEST", roleMembership);
    }

    private void initUnauthorizedMembershipMap() {
        roleMembershipMap.put("TEST", roleMembership);
    }
}
