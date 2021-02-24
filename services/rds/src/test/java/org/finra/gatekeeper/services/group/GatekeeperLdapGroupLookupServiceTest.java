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

package org.finra.gatekeeper.services.group;

import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.auth.GatekeeperAuthorizationService;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperApprovalProperties;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.finra.gatekeeper.services.group.service.GatekeeperLdapGroupLookupService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.core.LdapTemplate;

import java.util.*;

import static org.mockito.Mockito.when;

/**
 * Unit tests for GatekeeperRoleService
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class GatekeeperLdapGroupLookupServiceTest {

    @Mock
    private GatekeeperLdapGroupLookupService gatekeeperLdapGroupLookupService;
    
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

        when(gatekeeperAuthProperties.getLdap()).thenReturn(
                new GatekeeperAuthProperties.GatekeeperLdapProperties());

        when(gatekeeperAuthorizationService.getUser()).thenReturn(
                new GatekeeperUserEntry("test", "dn", "userEntry@gk.org", "userName"));
        users = new ArrayList<>();
        users.add(userEntry);

        Set<String> allSdlcs = new HashSet<>();
        allSdlcs.add("dev");
        allSdlcs.add("qa");
        allSdlcs.add("prod");
        when(gatekeeperApprovalProperties.getAllSdlcs()).thenReturn(allSdlcs);

        gatekeeperLdapGroupLookupService = new GatekeeperLdapGroupLookupService(ldapTemplate, gatekeeperAuthProperties, gatekeeperRdsAuthProperties);

    }
    @Test
    public void testPull(){
        System.out.println(gatekeeperLdapGroupLookupService.loadGroups());
    }

    @Test
    public void testParseADGroups(){
        String[] parsedAttributes = gatekeeperLdapGroupLookupService.parseADGroups("APP_GK_PET_DBAC_P");
        Assert.assertArrayEquals(new String[]{"PET", "DBAC", "P"}, parsedAttributes);
    }

    @Test
    public void testParseNullADGroups(){
        String[] parsedAttributes = gatekeeperLdapGroupLookupService.parseADGroups(null);
        Assert.assertNull(parsedAttributes);
    }

    @Test
    public void testParseInvalidADGroups(){
        String[] parsedAttributes = gatekeeperLdapGroupLookupService.parseADGroups("ffdsfsfsstest");
        Assert.assertNull(parsedAttributes);
    }

    @Test
    public void testParseInvalidGKROLE(){
        String[] parsedAttributes = gatekeeperLdapGroupLookupService.parseADGroups("APP_GK_PET_DBBC_P");
        Assert.assertNull(parsedAttributes);
    }

    @Test
    public void testParseInvalidSDLC(){
        String[] parsedAttributes = gatekeeperLdapGroupLookupService.parseADGroups("APP_GK_PET_DBAC_E");
        Assert.assertNull(parsedAttributes);
    }

}
