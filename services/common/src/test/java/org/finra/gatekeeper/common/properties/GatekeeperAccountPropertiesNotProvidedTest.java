/*
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
 *
 */

package org.finra.gatekeeper.common.properties;

import org.finra.gatekeeper.common.TestApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = TestApplication.class)
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
