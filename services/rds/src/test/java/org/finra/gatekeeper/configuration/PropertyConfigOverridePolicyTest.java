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

import org.finra.gatekeeper.services.accessrequest.model.OverridePolicy;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

/**
 * Tests for custom stuff in PropertyConfig
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyConfigOverridePolicyTest {

    @InjectMocks
    private GatekeeperOverrideProperties overridePolicy;

    @Mock
    private GatekeeperRoleService gatekeeperRoleService;

    private Map<String, Map<String, Map<String, Integer>>> mockOverrides;

    private Map<String, RoleMembership> roleMemberships;

    private Set<GatekeeperRdsRole> gatekeeperRdsRoles;

    private List<UserRole> userRoles;

    private String DBA_ROLE = "dba";
    private String DATAFIX_ROLE = "datafix";
    private String READONLY_ROLE = "readonly";
    private String DBA_CONFIDENTIAL_ROLE = "dba_confidential";
    private String READONLY_CONFIDENTIAL_ROLE = "readonly_confidential";

    private Integer DEV_DBA_OVERRIDE = 1;
    private Integer DEV_DATAFIX_OVERRIDE = 7;
    private Integer DEV_READONLY_OVERRIDE = 14;

    private Integer OPS_DBA_OVERRIDE = 2;
    private Integer OPS_DATAFIX_OVERRIDE = 8;
    private Integer OPS_READONLY_OVERRIDE = 12;

    private Integer DBA_DBA_OVERRIDE = 3;
    private Integer DBA_DATAFIX_OVERRIDE = 6;
    private Integer DBA_READONLY_OVERRIDE = 21;

    private Integer MAX_DAYS = 180;

    @Before
    public void init(){
        ReflectionTestUtils.setField(overridePolicy,"maxDays", MAX_DAYS);
        initMockOverrides();
        initRoleMemberships();
    }

    @Test
    public void testGetOverridesDev(){
        OverridePolicy result = overridePolicy.getOverrides(roleMemberships);

        Assert.assertEquals(result.getOverridePolicy().get(READONLY_ROLE).get("dev"), DEV_READONLY_OVERRIDE);
        Assert.assertEquals(result.getOverridePolicy().get(DATAFIX_ROLE).get("dev"), DEV_DATAFIX_OVERRIDE);
        Assert.assertEquals(result.getOverridePolicy().get(DBA_ROLE).get("dev"), DEV_DBA_OVERRIDE);
        Assert.assertFalse(result.getOverridePolicy().containsKey(DBA_CONFIDENTIAL_ROLE));
        Assert.assertFalse(result.getOverridePolicy().containsKey(READONLY_CONFIDENTIAL_ROLE));
    }

    @Test
    public void testGetOverridesOps(){
        addOpsRoleMemberships();
        addOpsOverrides();

        OverridePolicy result = overridePolicy.getOverrides(roleMemberships);

        Assert.assertEquals(result.getOverridePolicy().get(READONLY_ROLE).get("dev"), DEV_READONLY_OVERRIDE);
        Assert.assertEquals(result.getOverridePolicy().get(DATAFIX_ROLE).get("dev"), OPS_DATAFIX_OVERRIDE);
        Assert.assertEquals(result.getOverridePolicy().get(DBA_ROLE).get("dev"), OPS_DBA_OVERRIDE);
        Assert.assertFalse(result.getOverridePolicy().containsKey(DBA_CONFIDENTIAL_ROLE));
        Assert.assertFalse(result.getOverridePolicy().containsKey(READONLY_CONFIDENTIAL_ROLE));
    }

    @Test
    public void testGetOverridesDba(){
        addOpsRoleMemberships();
        addOpsOverrides();
        addDbaRoleMemberships();
        addDbaOverrides();

        OverridePolicy result = overridePolicy.getOverrides(roleMemberships);

        Assert.assertEquals(result.getOverridePolicy().get(READONLY_ROLE).get("dev"), DBA_READONLY_OVERRIDE);
        Assert.assertEquals(result.getOverridePolicy().get(DATAFIX_ROLE).get("dev"), OPS_DATAFIX_OVERRIDE);
        Assert.assertEquals(result.getOverridePolicy().get(DBA_ROLE).get("dev"), DBA_DBA_OVERRIDE);
        Assert.assertFalse(result.getOverridePolicy().containsKey(DBA_CONFIDENTIAL_ROLE));
        Assert.assertFalse(result.getOverridePolicy().containsKey(READONLY_CONFIDENTIAL_ROLE));
    }

    @Test
    public void testEmptyRolesList(){
        Mockito.when(gatekeeperRoleService.getUserRoles(Mockito.any())).thenReturn(new HashSet<>());
        OverridePolicy result = overridePolicy.getOverrides(roleMemberships);

        Assert.assertTrue(result.getOverridePolicy().isEmpty());
    }

    @Test
    public void testEmptyRoleMemberships(){
        Mockito.when(gatekeeperRoleService.getUserRoles(Mockito.any())).thenReturn(new HashSet<>());
        OverridePolicy result = overridePolicy.getOverrides(new HashMap<>());

        Assert.assertTrue(result.getOverridePolicy().isEmpty());
    }

    @Test
    public void testGetMaxDaysForRequestDev() {
        initUserRoles();
        Integer result = overridePolicy.getMaxDaysForRequest(roleMemberships, userRoles, "dev");
        Assert.assertEquals(result, DEV_DBA_OVERRIDE);
    }

    @Test
    public void testGetMaxDaysForRequestOps() {
        initUserRoles();
        addOpsRoleMemberships();
        addOpsOverrides();
        Integer result = overridePolicy.getMaxDaysForRequest(roleMemberships, userRoles, "dev");
        Assert.assertEquals(result, OPS_DBA_OVERRIDE);
    }

    @Test
    public void testGetMaxDaysForRequestDba() {
        initUserRoles();
        addOpsRoleMemberships();
        addOpsOverrides();
        addDbaRoleMemberships();
        addDbaOverrides();
        Integer result = overridePolicy.getMaxDaysForRequest(roleMemberships, userRoles, "dev");
        Assert.assertEquals(result, DBA_DBA_OVERRIDE);
    }

    @Test
    public void testNoUserRoles() {
        Integer result = overridePolicy.getMaxDaysForRequest(roleMemberships, new ArrayList<>(), "dev");
        Assert.assertEquals(result, MAX_DAYS);
    }

    @Test
    public void testUserRolesNoOverridePolicy() {
        initUserRolesNoOverridePolicy();
        Integer result = overridePolicy.getMaxDaysForRequest(roleMemberships, userRoles, "dev");
        Assert.assertEquals(result, MAX_DAYS);
    }

    private void initMockOverrides(){
        mockOverrides = new HashMap<>();
        Map<String, Map<String, Integer>> mockDevPolicy = new HashMap<>();
        Map<String, Integer> mockDba = new HashMap<>();
        mockDba.put("dev", DEV_DBA_OVERRIDE);
        Map<String, Integer> mockDatafix = new HashMap<>();
        mockDatafix.put("dev", DEV_DATAFIX_OVERRIDE);
        Map<String, Integer> mockReadonly = new HashMap<>();
        mockReadonly.put("dev", DEV_READONLY_OVERRIDE);

        mockDevPolicy.put(DBA_ROLE, mockDba);
        mockDevPolicy.put(DATAFIX_ROLE, mockDatafix);
        mockDevPolicy.put(READONLY_ROLE, mockReadonly);

        mockOverrides.put("dev", mockDevPolicy);
        ReflectionTestUtils.setField(overridePolicy,"overrides", mockOverrides);
    }

    private void addOpsOverrides(){
        Map<String, Map<String, Integer>> mockOpsPolicy = new HashMap<>();
        Map<String, Integer> mockDba = new HashMap<>();
        mockDba.put("dev", OPS_DBA_OVERRIDE);
        Map<String, Integer> mockDatafix = new HashMap<>();
        mockDatafix.put("dev", OPS_DATAFIX_OVERRIDE);
        Map<String, Integer> mockReadonly = new HashMap<>();
        mockReadonly.put("dev", OPS_READONLY_OVERRIDE);

        mockOpsPolicy.put(DBA_ROLE, mockDba);
        mockOpsPolicy.put(DATAFIX_ROLE, mockDatafix);
        mockOpsPolicy.put(READONLY_ROLE, mockReadonly);

        mockOverrides.put("ops", mockOpsPolicy);
        ReflectionTestUtils.setField(overridePolicy,"overrides", mockOverrides);
    }

    private void addDbaOverrides(){
        Map<String, Map<String, Integer>> mockDbaPolicy = new HashMap<>();
        Map<String, Integer> mockDba = new HashMap<>();
        mockDba.put("dev", DBA_DBA_OVERRIDE);
        Map<String, Integer> mockDatafix = new HashMap<>();
        mockDatafix.put("dev", DBA_DATAFIX_OVERRIDE);
        Map<String, Integer> mockReadonly = new HashMap<>();
        mockReadonly.put("dev", DBA_READONLY_OVERRIDE);

        mockDbaPolicy.put(DBA_ROLE, mockDba);
        mockDbaPolicy.put(DATAFIX_ROLE, mockDatafix);
        mockDbaPolicy.put(READONLY_ROLE, mockReadonly);

        mockOverrides.put("dba", mockDbaPolicy);
        ReflectionTestUtils.setField(overridePolicy,"overrides", mockOverrides);
    }

    private void initRoleMemberships() {
        roleMemberships = new HashMap<>();
        gatekeeperRdsRoles = new HashSet<>();

        Set<String> devSdlcs = new HashSet<>();
        devSdlcs.add("DEV");
        Set<String> qaSdlcs = new HashSet<>();
        qaSdlcs.add("DEV");
        qaSdlcs.add("QA");

        RoleMembership testDev = new RoleMembership();
        testDev.getRoles().put(GatekeeperRdsRole.DEV, devSdlcs);
        roleMemberships.put("TESTDEV", testDev);

        gatekeeperRdsRoles.add(GatekeeperRdsRole.DEV);

        Mockito.when(gatekeeperRoleService.getUserRoles(Mockito.any())).thenReturn(gatekeeperRdsRoles);
    }

    private void addOpsRoleMemberships() {

        Set<String> prodSdlcs = new HashSet<>();
        prodSdlcs.add("DEV");
        prodSdlcs.add("QA");
        prodSdlcs.add("PROD");

        RoleMembership testOps = new RoleMembership();
        testOps.getRoles().put(GatekeeperRdsRole.OPS, prodSdlcs);
        roleMemberships.put("TESTOPS", testOps);

        gatekeeperRdsRoles.add(GatekeeperRdsRole.OPS);

        Mockito.when(gatekeeperRoleService.getUserRoles(Mockito.any())).thenReturn(gatekeeperRdsRoles);
    }

    private void addDbaRoleMemberships() {

        Set<String> prodSdlcs = new HashSet<>();
        prodSdlcs.add("DEV");
        prodSdlcs.add("QA");
        prodSdlcs.add("PROD");

        RoleMembership testDba = new RoleMembership();
        testDba.getRoles().put(GatekeeperRdsRole.DBA, prodSdlcs);
        roleMemberships.put("TESTDBA", testDba);

        gatekeeperRdsRoles.add(GatekeeperRdsRole.DBA);

        Mockito.when(gatekeeperRoleService.getUserRoles(Mockito.any())).thenReturn(gatekeeperRdsRoles);
    }

    private void initUserRoles() {
        userRoles = new ArrayList<>();

        userRoles.add(new UserRole(DBA_ROLE));
        userRoles.add(new UserRole(DATAFIX_ROLE));
        userRoles.add(new UserRole(READONLY_ROLE));
    }

    private void initUserRolesNoOverridePolicy() {
        userRoles = new ArrayList<>();

        userRoles.add(new UserRole(DBA_CONFIDENTIAL_ROLE));
        userRoles.add(new UserRole(READONLY_CONFIDENTIAL_ROLE));
    }
}
