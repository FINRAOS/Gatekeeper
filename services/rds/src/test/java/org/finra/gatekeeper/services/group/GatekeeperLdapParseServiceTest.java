package org.finra.gatekeeper.services.group;

import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;
import org.finra.gatekeeper.services.group.service.GatekeeperLdapParseService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class GatekeeperLdapParseServiceTest {

    private GatekeeperLdapParseService gatekeeperLdapParseService;

    @Mock
    private GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties;

    @Before
    public void initMocks(){
        when(gatekeeperRdsAuthProperties.getAdGroupsPattern()).thenReturn("APP_GK_([A-Z]{2,8})_(RO|DF|DBA|ROC|DBAC)_(Q|D|P)");
        gatekeeperLdapParseService = new GatekeeperLdapParseService(gatekeeperRdsAuthProperties);
    }

    @Test
    public void testParseADGroups1(){
        String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups("APP_GK_CCC_DBAC_P");
        Assert.assertArrayEquals(new String[]{"CCC", "DBAC", "P", "APP_GK_CCC_DBAC_P"}, parsedAttributes);
    }

    @Test
    public void testParseADGroups2(){
        String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups("APP_GK_CCCaaa_DBAC_P");
        Assert.assertArrayEquals(new String[]{"CCCAAA", "DBAC", "P", "APP_GK_CCCAAA_DBAC_P"}, parsedAttributes);
    }

    @Test
    public void testParseNullADGroups(){
        String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups(null);
        Assert.assertArrayEquals(new String[]{"","",""}, parsedAttributes);
    }

    @Test
    public void testParseInvalidADGroups(){
        String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups("ffdsfsfsstest");
        Assert.assertArrayEquals(new String[]{"","",""}, parsedAttributes);
    }


    @Test
    public void testParseInvalidApplication1(){
        String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups("APP_GK_a_DBC_P");
        Assert.assertArrayEquals(new String[]{"","",""}, parsedAttributes);
    }
    @Test
    public void testParseInvalidApplication2(){
        String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups("APP_GK_AAAAAAAAA_DBC_P");
        Assert.assertArrayEquals(new String[]{"","",""}, parsedAttributes);
    }
    @Test
    public void testParseInvalidApplication3(){
        String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups("APP_GK_12_DBC_P");
        Assert.assertArrayEquals(new String[]{"","",""}, parsedAttributes);
    }


    @Test
    public void testParseInvalidGKROLE(){
        String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups("APP_GK_CCC_DBBC_P");
        Assert.assertArrayEquals(new String[]{"","",""}, parsedAttributes);
    }

    @Test
    public void testParseInvalidSDLC(){
        String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups("APP_GK_CCC_DBAC_E");
        Assert.assertArrayEquals(new String[]{"","",""}, parsedAttributes);
    }
}
