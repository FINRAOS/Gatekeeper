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

import com.google.common.collect.Maps;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;
import org.finra.gatekeeper.services.group.service.GatekeeperLdapGroupLookupService;
import org.finra.gatekeeper.services.group.service.GatekeeperLdapParseService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;

import java.util.*;

import static org.mockito.Mockito.when;

/**
 * Unit tests for GatekeeperRoleService
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class GatekeeperLdapGroupLookupServiceTest {

    private GatekeeperLdapGroupLookupService gatekeeperLdapGroupLookupService;
    private GatekeeperLdapParseService gatekeeperLdapParseService;
    @Mock
    private LdapTemplate ldapTemplate;

    @Mock
    private GatekeeperProperties gatekeeperProperties;

    @Mock
    private GatekeeperProperties.AuthenticationProperties authenticationProperties;

    @Mock
    private GatekeeperProperties.AuthenticationProperties.GatekeeperLdapProperties gatekeeperLdapProperties;

    @Mock
    private GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties;

    
    @Before
    public void initMocks(){
        when(gatekeeperProperties.getAuth()).thenReturn(authenticationProperties);
        when(authenticationProperties.getLdap()).thenReturn(
                new GatekeeperProperties.AuthenticationProperties.GatekeeperLdapProperties()
                        .setUsersCnAttribute("cn")
                        .setUsersIdAttribute("sAMAccountName")
                        .setUsersEmailAttribute("mail")
                        .setDistinguishedName("distinguishedName")
                        .setGroupsBase("OU=GROUPS")
                        .setUsersBase("OU=Locations")
                        .setRestrictedGroupsBase("OU=GROUPS")
                        .setUsersNameAttribute("name"));


        when(gatekeeperRdsAuthProperties.getAdGroupsPattern()).thenReturn("APP_GK_([A-Z]{2,8})_(RO|DF|DBA|ROC|DBAC)_(Q|D|P)");
        when(gatekeeperRdsAuthProperties.getRestrictedPrefix()).thenReturn("APP_GK_");

        gatekeeperLdapParseService = new GatekeeperLdapParseService(gatekeeperRdsAuthProperties);

        gatekeeperLdapGroupLookupService = new GatekeeperLdapGroupLookupService(ldapTemplate, gatekeeperProperties, gatekeeperRdsAuthProperties, gatekeeperLdapParseService);

        List<GatekeeperADGroupEntry> fakeSet = new ArrayList<>();
        fakeSet.add(new GatekeeperADGroupEntry("PET", "RO", "D", "APP_GK_PET_RO_D"));
        fakeSet.add(new GatekeeperADGroupEntry("ESC", "RO", "D", "APP_GK_ESC_RO_D"));
        fakeSet.add(new GatekeeperADGroupEntry("PET", "DBA", "Q", "APP_GK_PET_DBA_Q"));

        when(ldapTemplate.search(Mockito.any(LdapQuery.class), Mockito.any(AttributesMapper.class))).thenReturn(fakeSet);

    }
    @Test
    public void testLoadGroups(){
        Map<String, Set<GatekeeperADGroupEntry>> expectedReturn = new HashMap<String, Set<GatekeeperADGroupEntry>>();
        Set<GatekeeperADGroupEntry> petSet = new HashSet<>();
        Set<GatekeeperADGroupEntry> escSet = new HashSet<>();

        petSet.add(new GatekeeperADGroupEntry("PET", "RO", "D", "APP_GK_PET_RO_D"));
        petSet.add(new GatekeeperADGroupEntry("PET", "DBA", "Q", "APP_GK_PET_DBA_Q"));
        escSet.add(new GatekeeperADGroupEntry("ESC", "RO", "D", "APP_GK_ESC_RO_D"));

        expectedReturn.put("ESC", escSet);
        expectedReturn.put("PET", petSet);

        char[] sdlc = new char[1];
        sdlc[0] = ' ';
        when(gatekeeperRdsAuthProperties.getUnrestrictedSDLC()).thenReturn(sdlc);

        Assert.assertTrue(Maps.difference(expectedReturn, gatekeeperLdapGroupLookupService.loadGroups()).areEqual());
    }

    @Test
    public void testLoadGroupsWithUnrestrictedSDLC(){
        Map<String, Set<GatekeeperADGroupEntry>> expectedReturn = new HashMap<String, Set<GatekeeperADGroupEntry>>();
        Set<GatekeeperADGroupEntry> petSet = new HashSet<>();
        Set<GatekeeperADGroupEntry> escSet = new HashSet<>();

        petSet.add(new GatekeeperADGroupEntry("PET", "RO", "D", "APP_GK_PET_RO_D"));
        escSet.add(new GatekeeperADGroupEntry("ESC", "RO", "D", "APP_GK_ESC_RO_D"));

        expectedReturn.put("ESC", escSet);
        expectedReturn.put("PET", petSet);

        char[] sdlc = new char[1];
        sdlc[0] = 'Q';
        when(gatekeeperRdsAuthProperties.getUnrestrictedSDLC()).thenReturn(sdlc);


        Assert.assertTrue(Maps.difference(expectedReturn, gatekeeperLdapGroupLookupService.loadGroups()).areEqual());
    }


}
