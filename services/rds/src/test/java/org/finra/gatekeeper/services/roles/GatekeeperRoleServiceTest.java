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
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ldap.core.LdapTemplate;

import java.util.*;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

/**
 * Unit tests for GatekeeperRoleService
 */
@RunWith(MockitoJUnitRunner.class)
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

    private Set<String> memberships;
    
    private List<GatekeeperUserEntry> users;
    
    
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

        gatekeeperRoleService = new GatekeeperRoleService(gatekeeperAuthorizationService,
                gatekeeperAuthProperties,
                gatekeeperRdsAuthProperties);

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

}
