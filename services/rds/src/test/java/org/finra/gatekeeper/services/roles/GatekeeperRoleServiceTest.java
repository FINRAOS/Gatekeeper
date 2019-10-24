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

package org.finra.gatekeeper.services.roles;

import org.finra.gatekeeper.common.authfilter.parser.GatekeeperUserProfile;
import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperUserProfile;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.auth.GatekeeperAuthorizationService;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperApprovalProperties;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.core.LdapTemplate;

import java.util.*;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

/**
 * Unit tests for GatekeeperRoleService
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class GatekeeperRoleServiceTest {

    @Mock
    private GatekeeperRoleService gatekeeperRoleService;
    
    @Mock
    private LdapTemplate ldapTemplate;

    @Mock
    private GatekeeperUserEntry userEntry;
    
    @Mock
    private GatekeeperUserEntry gatekeeperUserProfile;
    
    @Mock
    private GatekeeperAuthProperties gatekeeperAuthProperties;

    @Mock
    private GatekeeperAuthorizationService gatekeeperAuthorizationService;

    @Mock
    private GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties;

    @Mock
    private GatekeeperApprovalProperties gatekeeperApprovalProperties;

    private Set<String> memberships;
    
    private List<GatekeeperUserEntry> users;

    private Map<String, RoleMembership> roleMemberships;

    private Set<GatekeeperRdsRole> gatekeeperRdsRoles;
    
    
    @Before
    public void initMocks(){

        when(gatekeeperAuthProperties.getLdap()).thenReturn(
                new GatekeeperAuthProperties.GatekeeperLdapProperties()
                        .setUsersCnAttribute("cn")
                        .setUsersIdAttribute("sAMAccountName")
                        .setUsersEmailAttribute("mail")
                        .setUsersDnAttribute("distinguishedName")
                        .setGroupsBase("OU=GROUPS")
                        .setUsersBase("OU=Locations")
                        .setUsersNameAttribute("name"));
        when(gatekeeperRdsAuthProperties.getDbaGroupsPattern()).thenReturn("COMPANY_([a-zA-Z]+)_DBA");
        when(gatekeeperRdsAuthProperties.getOpsGroupsPattern()).thenReturn("COMPANY_([a-zA-Z]+)_OPS");
        when(gatekeeperRdsAuthProperties.getDevGroupsPattern()).thenReturn("COMPANY_([a-zA-Z]+)_DEV_(DEV|QA|QC|PROD)");

        when(userEntry.getEmail()).thenReturn("userEntry@gk.org");
        when(userEntry.getName()).thenReturn("userName");
        when(userEntry.getUserId()).thenReturn("test");
        when(gatekeeperUserProfile.getName()).thenReturn("userName");
        when(gatekeeperAuthProperties.getApproverGroup()).thenReturn("GK_RDS_APPROVER");

        when(gatekeeperRoleService.getUserProfile()).thenReturn(gatekeeperUserProfile);
        when(gatekeeperAuthorizationService.getUser()).thenReturn(
                new GatekeeperUserEntry("test", "dn", "userEntry@gk.org", "userName"));
        users = new ArrayList<>();
        users.add(userEntry);

        Set<String> allSdlcs = new HashSet<>();
        allSdlcs.add("dev");
        allSdlcs.add("qa");
        allSdlcs.add("prod");
        when(gatekeeperApprovalProperties.getAllSdlcs()).thenReturn(allSdlcs);

        gatekeeperRoleService = new GatekeeperRoleService(gatekeeperAuthorizationService,
                gatekeeperAuthProperties,
                gatekeeperRdsAuthProperties,
                gatekeeperApprovalProperties);

    }


    @Test
    public void testDba(){
        memberships = new HashSet<>();
        memberships.add("THIS_IS_A_TEST_GROUP");
        memberships.add("COMPANY_ROCKSTAR_DBA");
        Mockito.when(gatekeeperAuthorizationService.getMemberships()).thenReturn(memberships);

        Assert.assertTrue(gatekeeperRoleService.getRole().equals(GatekeeperRdsRole.DBA));
        
        Set<String> expectedMemberships = new HashSet<>();
        expectedMemberships.add("ROCKSTAR");
        Assert.assertTrue(gatekeeperRoleService.getDbaMemberships().equals(expectedMemberships));
    }

    @Test
    public void testOps(){
        memberships = new HashSet<>();
        memberships.add("COMPANY_ROCKSTAR_OPS");
        Mockito.when(gatekeeperAuthorizationService.getMemberships()).thenReturn(memberships);

        Assert.assertTrue(gatekeeperRoleService.getRole().equals(GatekeeperRdsRole.OPS));

        Set<String> expectedMemberships = new HashSet<>();
        expectedMemberships.add("ROCKSTAR");
        Assert.assertTrue(gatekeeperRoleService.getOpsMemberships().equals(expectedMemberships));
    }


    @Test
    public void testDev(){
        memberships = new HashSet<>();
        memberships.add("SOME_OTHER_GROUP");
        memberships.add("COMPANY_PRJCT_DEV_DEV");
        memberships.add("COMPANY_PRJCT_DEV_PROD");
        memberships.add("COMPANY_PRJCT_DEV_QA");
        memberships.add("COMPANY_TEST_DEV_QC");
        Mockito.when(gatekeeperAuthorizationService.getMemberships()).thenReturn(memberships);

        Map<String, Set<String>> expectedMemberships = new HashMap<>();
        Set<String> expectedPrjctSdlc = new HashSet<>();
        Set<String> expectedTestSdlc = new HashSet<>();
        expectedPrjctSdlc.add("DEV");
        expectedPrjctSdlc.add("QA");
        expectedPrjctSdlc.add("PROD");
        expectedTestSdlc.add("QA");
        expectedMemberships.put("PRJCT", expectedPrjctSdlc);
        expectedMemberships.put("TEST", expectedTestSdlc);
        Assert.assertEquals(3, expectedPrjctSdlc.size());
        Assert.assertEquals(1, expectedTestSdlc.size());
        Assert.assertEquals(2, expectedMemberships.size());
        Assert.assertEquals(gatekeeperRoleService.getRole(), GatekeeperRdsRole.DEV);
    }


    @Test
    public void testGetRoleMembershipsDev() {
        memberships = new HashSet<>();
        memberships.add("SOME_OTHER_GROUP");
        memberships.add("COMPANY_PRJCT_DEV_DEV");
        memberships.add("COMPANY_PRJCT_DEV_PROD");
        memberships.add("COMPANY_PRJCT_DEV_QA");
        memberships.add("COMPANY_TEST_DEV_QC");
        Mockito.when(gatekeeperAuthorizationService.getMemberships()).thenReturn(memberships);

        Map<String, RoleMembership> expectedResult = new HashMap<>();

        Map<GatekeeperRdsRole, Set<String>> prjctDevRoles = new HashMap<>();
        prjctDevRoles.put(GatekeeperRdsRole.DEV, new HashSet<>());
        prjctDevRoles.get(GatekeeperRdsRole.DEV).add("DEV");
        prjctDevRoles.get(GatekeeperRdsRole.DEV).add("QA");
        prjctDevRoles.get(GatekeeperRdsRole.DEV).add("PROD");

        expectedResult.put("PRJCT", new RoleMembership(prjctDevRoles));

        Map<GatekeeperRdsRole, Set<String>> testDevRoles = new HashMap<>();
        testDevRoles.put(GatekeeperRdsRole.DEV, new HashSet<>());
        testDevRoles.get(GatekeeperRdsRole.DEV).add("QA");

        expectedResult.put("TEST", new RoleMembership(testDevRoles));

        Map<String, RoleMembership> actualResult = gatekeeperRoleService.getRoleMemberships();

        Assert.assertEquals(expectedResult.get("PRJCT").getRoles().get(GatekeeperRdsRole.DEV).size(), actualResult.get("PRJCT").getRoles().get(GatekeeperRdsRole.DEV).size());
        Assert.assertEquals(expectedResult.get("TEST").getRoles().get(GatekeeperRdsRole.DEV).size(), actualResult.get("TEST").getRoles().get(GatekeeperRdsRole.DEV).size());
    }

    @Test
    public void testGetRoleMembershipsOps() {
        memberships = new HashSet<>();
        memberships.add("SOME_OTHER_GROUP");
        memberships.add("COMPANY_PRJCT_DEV_DEV");
        memberships.add("COMPANY_PRJCT_DEV_PROD");
        memberships.add("COMPANY_PRJCT_DEV_QA");
        memberships.add("COMPANY_TEST_DEV_QC");
        memberships.add("COMPANY_TEST_OPS");
        Mockito.when(gatekeeperAuthorizationService.getMemberships()).thenReturn(memberships);

        Map<String, RoleMembership> expectedResult = new HashMap<>();

        Map<GatekeeperRdsRole, Set<String>> prjctDevRoles = new HashMap<>();
        prjctDevRoles.put(GatekeeperRdsRole.DEV, new HashSet<>());
        prjctDevRoles.get(GatekeeperRdsRole.DEV).add("DEV");
        prjctDevRoles.get(GatekeeperRdsRole.DEV).add("QA");
        prjctDevRoles.get(GatekeeperRdsRole.DEV).add("PROD");

        expectedResult.put("PRJCT", new RoleMembership(prjctDevRoles));

        Map<GatekeeperRdsRole, Set<String>> testDevRoles = new HashMap<>();
        testDevRoles.put(GatekeeperRdsRole.DEV, new HashSet<>());
        testDevRoles.get(GatekeeperRdsRole.DEV).add("QA");
        testDevRoles.put(GatekeeperRdsRole.OPS, new HashSet<>());
        testDevRoles.get(GatekeeperRdsRole.OPS).add("DEV");
        testDevRoles.get(GatekeeperRdsRole.OPS).add("QA");
        testDevRoles.get(GatekeeperRdsRole.OPS).add("PROD");

        expectedResult.put("TEST", new RoleMembership(testDevRoles));

        Map<String, RoleMembership> actualResult = gatekeeperRoleService.getRoleMemberships();

        Assert.assertEquals(expectedResult.get("PRJCT").getRoles().get(GatekeeperRdsRole.DEV).size(), actualResult.get("PRJCT").getRoles().get(GatekeeperRdsRole.DEV).size());
        Assert.assertEquals(expectedResult.get("TEST").getRoles().get(GatekeeperRdsRole.DEV).size(), actualResult.get("TEST").getRoles().get(GatekeeperRdsRole.DEV).size());
        Assert.assertEquals(expectedResult.get("TEST").getRoles().get(GatekeeperRdsRole.OPS).size(), actualResult.get("TEST").getRoles().get(GatekeeperRdsRole.OPS).size());
    }

    @Test
    public void testGetRoleMembershipsDba() {
        memberships = new HashSet<>();
        memberships.add("SOME_OTHER_GROUP");
        memberships.add("COMPANY_PRJCT_DEV_DEV");
        memberships.add("COMPANY_PRJCT_DEV_PROD");
        memberships.add("COMPANY_PRJCT_DEV_QA");
        memberships.add("COMPANY_TEST_DEV_QC");
        memberships.add("COMPANY_TEST_DBA");
        Mockito.when(gatekeeperAuthorizationService.getMemberships()).thenReturn(memberships);

        Map<String, RoleMembership> expectedResult = new HashMap<>();

        Map<GatekeeperRdsRole, Set<String>> prjctDevRoles = new HashMap<>();
        prjctDevRoles.put(GatekeeperRdsRole.DEV, new HashSet<>());
        prjctDevRoles.get(GatekeeperRdsRole.DEV).add("DEV");
        prjctDevRoles.get(GatekeeperRdsRole.DEV).add("QA");
        prjctDevRoles.get(GatekeeperRdsRole.DEV).add("PROD");

        expectedResult.put("PRJCT", new RoleMembership(prjctDevRoles));

        Map<GatekeeperRdsRole, Set<String>> testDevRoles = new HashMap<>();
        testDevRoles.put(GatekeeperRdsRole.DEV, new HashSet<>());
        testDevRoles.get(GatekeeperRdsRole.DEV).add("QA");
        testDevRoles.put(GatekeeperRdsRole.DBA, new HashSet<>());
        testDevRoles.get(GatekeeperRdsRole.DBA).add("DEV");
        testDevRoles.get(GatekeeperRdsRole.DBA).add("QA");
        testDevRoles.get(GatekeeperRdsRole.DBA).add("PROD");

        expectedResult.put("TEST", new RoleMembership(testDevRoles));

        Map<String, RoleMembership> actualResult = gatekeeperRoleService.getRoleMemberships();

        Assert.assertEquals(expectedResult.get("PRJCT").getRoles().get(GatekeeperRdsRole.DEV).size(), actualResult.get("PRJCT").getRoles().get(GatekeeperRdsRole.DEV).size());
        Assert.assertEquals(expectedResult.get("TEST").getRoles().get(GatekeeperRdsRole.DEV).size(), actualResult.get("TEST").getRoles().get(GatekeeperRdsRole.DEV).size());
        Assert.assertEquals(expectedResult.get("TEST").getRoles().get(GatekeeperRdsRole.DBA).size(), actualResult.get("TEST").getRoles().get(GatekeeperRdsRole.DBA).size());
    }

    @Test
    public void testIsApprover() {
        when(gatekeeperAuthProperties.getApproverGroup()).thenReturn("GATEKEEPER_APPROVER_TEST_GROUP");

        memberships = new HashSet<>();
        memberships.add("GATEKEEPER_APPROVER_TEST_GROUP");
        memberships.add("SOME_OTHER_GROUP");
        memberships.add("COMPANY_PRJCT_DEV_DEV");
        memberships.add("COMPANY_PRJCT_DEV_PROD");
        memberships.add("COMPANY_PRJCT_DEV_QA");
        memberships.add("COMPANY_TEST_DEV_QC");
        memberships.add("COMPANY_TEST_OPS");
        Mockito.when(gatekeeperAuthorizationService.getMemberships()).thenReturn(memberships);

        boolean isApprover = gatekeeperRoleService.isApprover();
        Assert.assertTrue(isApprover);
    }

    @Test
    public void testGetUserRolesDevOnly() {
        initRoleMemberships();
        Set<GatekeeperRdsRole> result = gatekeeperRoleService.getUserRoles(roleMemberships);
        Assert.assertTrue(result.contains(GatekeeperRdsRole.DEV));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testGetUserRolesOps() {
        initRoleMemberships();
        addOpsRoleMemberships();

        Set<GatekeeperRdsRole> result = gatekeeperRoleService.getUserRoles(roleMemberships);
        Assert.assertTrue(result.contains(GatekeeperRdsRole.DEV));
        Assert.assertTrue(result.contains(GatekeeperRdsRole.OPS));
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testGetUserRolesDba() {
        initRoleMemberships();
        addOpsRoleMemberships();
        addDbaRoleMemberships();

        Set<GatekeeperRdsRole> result = gatekeeperRoleService.getUserRoles(roleMemberships);
        Assert.assertTrue(result.contains(GatekeeperRdsRole.DEV));
        Assert.assertTrue(result.contains(GatekeeperRdsRole.OPS));
        Assert.assertTrue(result.contains(GatekeeperRdsRole.DBA));
        Assert.assertEquals(3, result.size());
    }

    private void initRoleMemberships() {
        roleMemberships = new HashMap<>();
        gatekeeperRdsRoles = new HashSet<>();

        Set<String> devSdlcs = new HashSet<>();
        devSdlcs.add("DEV");

        RoleMembership testDev = new RoleMembership();
        testDev.getRoles().put(GatekeeperRdsRole.DEV, devSdlcs);
        roleMemberships.put("TEST", testDev);
    }

    private void addOpsRoleMemberships() {

        Set<String> prodSdlcs = new HashSet<>();
        prodSdlcs.add("DEV");
        prodSdlcs.add("QA");
        prodSdlcs.add("PROD");
        roleMemberships.get("TEST").getRoles().put(GatekeeperRdsRole.OPS, prodSdlcs);
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
    }

}
