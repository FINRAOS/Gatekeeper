package org.finra.gatekeeper.common.properties;

import org.finra.gatekeeper.common.TestApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("unit-test")
@WebAppConfiguration
@SpringApplicationConfiguration(classes = TestApplication.class)
public class GatekeeperAccountPropertiesTest {

    @Autowired
    private GatekeeperAccountProperties accountProperties;

    private String expectedURL = "https://testservice.com";
    private String expectedURI = "/test";

    @Test
    public void testConfig(){
        Assert.assertEquals(accountProperties.getServiceURL(), expectedURL);
        Assert.assertEquals(accountProperties.getServiceURI(), expectedURI);
        Assert.assertEquals(2, accountProperties.getSdlcOverrides().keySet().size());
        Assert.assertEquals("myacc2", accountProperties.getSdlcOverrides().get("hello2"));
    }

    @Test
    public void testGetAccountSdlcOverrides(){
        Assert.assertEquals("hello2", accountProperties.getAccountSdlcOverrides().get("myacc2"));
        Assert.assertEquals("hello1", accountProperties.getAccountSdlcOverrides().get("myacc1"));
        Assert.assertEquals("hello1", accountProperties.getAccountSdlcOverrides().get("123456789"));
    }
}
