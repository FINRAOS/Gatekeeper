package org.finra.gatekeeper.common.properties;

import org.finra.gatekeeper.common.TestApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = TestApplication.class)
public class GatekeeperAccountPropertiesNotProvidedTest {

    @Autowired
    private GatekeeperAccountProperties accountProperties;

    private String expectedURL = "https://testservice.com";
    private String expectedURI = "/test";

    @Test
    public void testConfig(){
        Assert.assertEquals(accountProperties.getServiceURL(), expectedURL);
        Assert.assertEquals(accountProperties.getServiceURI(), expectedURI);
    }

    @Test
    public void testGetAccountSdlcOverrides(){
        Assert.assertTrue(accountProperties.getSdlcOverrides().isEmpty());
    }

    @Test
    public void testGetAccountGrouping(){
        Assert.assertTrue(accountProperties.getSdlcGrouping().isEmpty());
    }

}
