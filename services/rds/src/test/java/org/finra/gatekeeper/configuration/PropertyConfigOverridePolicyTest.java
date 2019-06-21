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

import org.finra.gatekeeper.configuration.model.AppSpecificOverridePolicy;
import org.finra.gatekeeper.rds.model.RoleType;
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
    private AppSpecificOverridePolicy mockDevOverridePolicy;
    private AppSpecificOverridePolicy mockOpsOverridePolicy;
    private AppSpecificOverridePolicy mockDbaOverridePolicy;
    private AppSpecificOverridePolicy mockApproverOverridePolicy;
    private AppSpecificOverridePolicy mockUnauthorizedOverridePolicy;

    @Before
    public void init(){
        roleMembershipMap = new HashMap<>();
        roleMembership = new RoleMembership();

        initMockDevOverridePolicy();
        initMockOpsOverridePolicy();
        initMockDbaOverridePolicy();
        initMockApproverOverridePolicy();
        initMockUnauthorizedOverridePolicy();

        setMockOverridePolicy();
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

        Map<String, AppSpecificOverridePolicy> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, true);
        Assert.assertEquals(mockApproverOverridePolicy, actualOverridePolicy.get("TEST"));
    }

    @Test
    public void testGetOverridePolicyDba() {
        initDbaRoleMembershipMap();
        roleMembershipMap.put("TEST", roleMembership);

        Map<String, AppSpecificOverridePolicy> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, false);
        Assert.assertEquals(mockDbaOverridePolicy, actualOverridePolicy.get("TEST"));
    }

    @Test
    public void testGetOverridePolicyDev() {
        initDevRoleMembershipMap();

        Map<String, AppSpecificOverridePolicy> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, false);
        Assert.assertEquals(mockDevOverridePolicy, actualOverridePolicy.get("TEST"));
    }

    @Test
    public void testGetOverridePolicyOps() {
        initOpsRoleMembershipMap();

        Map<String, AppSpecificOverridePolicy> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, false);
        Assert.assertEquals(mockOpsOverridePolicy, actualOverridePolicy.get("TEST"));
    }

    @Test
    public void testGetOverridePolicyUnauthorized() {
        initUnauthorizedMembershipMap();

        Map<String, AppSpecificOverridePolicy> actualOverridePolicy = overridePolicy.getOverridePolicy(roleMembershipMap, false);
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

    private void initMockDevOverridePolicy() {
        mockDevOverridePolicy = new AppSpecificOverridePolicy();

        Map<String, Integer> mockDevDba = new HashMap<>();
        Map<String, Integer> mockDevDatafix = new HashMap<>();
        Map<String, Integer> mockDevReadonly = new HashMap<>();
        mockDevDba.put("dev",1);
        mockDevDatafix.put("dev",7);
        mockDevReadonly.put("dev",180);
        Map<RoleType, Map<String, Integer>> mockDevOverridePolicyMap = new HashMap<>();
        mockDevOverridePolicyMap.put(RoleType.DBA, mockDevDba);
        mockDevOverridePolicyMap.put(RoleType.DATAFIX, mockDevDatafix);
        mockDevOverridePolicyMap.put(RoleType.READONLY, mockDevReadonly);
        mockDevOverridePolicy.setAppSpecificOverridePolicy(mockDevOverridePolicyMap);
    }

    private void initMockOpsOverridePolicy() {
        mockOpsOverridePolicy = new AppSpecificOverridePolicy();

        Map<String, Integer> mockOpsDba = new HashMap<>();
        Map<String, Integer> mockOpsDatafix = new HashMap<>();
        Map<String, Integer> mockOpsReadonly = new HashMap<>();
        mockOpsDba.put("dev",2);
        mockOpsDatafix.put("dev",3);
        mockOpsReadonly.put("dev",4);
        Map<RoleType, Map<String, Integer>> mockOpsOverridePolicyMap = new HashMap<>();
        mockOpsOverridePolicyMap.put(RoleType.DBA, mockOpsDba);
        mockOpsOverridePolicyMap.put(RoleType.DATAFIX, mockOpsDatafix);
        mockOpsOverridePolicyMap.put(RoleType.READONLY, mockOpsReadonly);
        mockOpsOverridePolicy.setAppSpecificOverridePolicy(mockOpsOverridePolicyMap);
    }

    private void initMockDbaOverridePolicy() {
        mockDbaOverridePolicy = new AppSpecificOverridePolicy();

        Map<String, Integer> mockDbaDba = new HashMap<>();
        Map<String, Integer> mockDbaDatafix = new HashMap<>();
        Map<String, Integer> mockDbaReadonly = new HashMap<>();
        mockDbaDba.put("dev",5);
        mockDbaDatafix.put("dev",6);
        mockDbaReadonly.put("dev",7);
        Map<RoleType, Map<String, Integer>> mockDbaOverridePolicyMap = new HashMap<>();
        mockDbaOverridePolicyMap.put(RoleType.DBA, mockDbaDba);
        mockDbaOverridePolicyMap.put(RoleType.DATAFIX, mockDbaDatafix);
        mockDbaOverridePolicyMap.put(RoleType.READONLY, mockDbaReadonly);
        mockDbaOverridePolicy.setAppSpecificOverridePolicy(mockDbaOverridePolicyMap);
    }

    private void initMockApproverOverridePolicy() {
        mockApproverOverridePolicy = new AppSpecificOverridePolicy();

        Map<String, Integer> mockApproverDba = new HashMap<>();
        Map<String, Integer> mockApproverDatafix = new HashMap<>();
        Map<String, Integer> mockApproverReadonly = new HashMap<>();
        mockApproverDba.put("dev",8);
        mockApproverDatafix.put("dev",9);
        mockApproverReadonly.put("dev",10);
        Map<RoleType, Map<String, Integer>> mockApproverOverridePolicyMap = new HashMap<>();
        mockApproverOverridePolicyMap.put(RoleType.DBA, mockApproverDba);
        mockApproverOverridePolicyMap.put(RoleType.DATAFIX, mockApproverDatafix);
        mockApproverOverridePolicyMap.put(RoleType.READONLY, mockApproverReadonly);
        mockApproverOverridePolicy.setAppSpecificOverridePolicy(mockApproverOverridePolicyMap);
    }

    private void initMockUnauthorizedOverridePolicy() {
        mockUnauthorizedOverridePolicy = new AppSpecificOverridePolicy();
    }

    private void setMockOverridePolicy() {
        ReflectionTestUtils.setField(overridePolicy,"maxDays", 180);

        Map<String, Map<String, Map<String, Integer>>> mockOverrides = new HashMap<>();

        mockOverrides.put("dev", stringifyRoleTypeInMap(mockDevOverridePolicy.getAppSpecificOverridePolicy()));
        mockOverrides.put("dba", stringifyRoleTypeInMap(mockDbaOverridePolicy.getAppSpecificOverridePolicy()));
        mockOverrides.put("ops", stringifyRoleTypeInMap(mockOpsOverridePolicy.getAppSpecificOverridePolicy()));
        mockOverrides.put("approver", stringifyRoleTypeInMap(mockApproverOverridePolicy.getAppSpecificOverridePolicy()));
        mockOverrides.put("unauthorized", stringifyRoleTypeInMap(mockUnauthorizedOverridePolicy.getAppSpecificOverridePolicy()));

        ReflectionTestUtils.setField(overridePolicy,"overrides", mockOverrides);

    }

    private Map<String, Map<String, Integer>> stringifyRoleTypeInMap(Map<RoleType, Map<String, Integer>> mapRoleTypeForm) {
        Map<String, Map<String,Integer>> mapStringForm = new HashMap<>();
        mapRoleTypeForm.forEach((roleType, policy) -> {
            mapStringForm.put(roleType.toString(), policy);
        });

        return mapStringForm;
    }
}
