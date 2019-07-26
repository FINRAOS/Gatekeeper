package org.finra.gatekeeper.common.services.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.finra.gatekeeper.common.properties.GatekeeperAccountProperties;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.finra.gatekeeper.common.services.account.model.Region;
import org.finra.gatekeeper.common.services.backend2backend.Backend2BackendService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ObjectMapper.class})
public class AccountInformationServiceTest {

    @Mock
    private Backend2BackendService backend2BackendService;
    private GatekeeperAccountProperties gatekeeperAccountProperties;

    private AccountInformationService accountInformationService;

    private Account a1 = new Account()
            .setAccountId("34939394939")
            .setAlias("Account One")
            .setName("myacc1")
            .setRegions(Arrays.asList(new Region("us-east-1")))
            .setSdlc("dev");

    private Account a2 = new Account()
            .setAccountId("478848334560")
            .setAlias("Account Two")
            .setName("myacc2")
            .setRegions(Arrays.asList(new Region("us-east-1")))
            .setSdlc("qa");

    private Account a3 = new Account()
            .setAccountId("123456789")
            .setAlias("Account Three")
            .setName("myacc3")
            .setRegions(Arrays.asList(new Region("us-east-1")))
            .setSdlc("prod");

    private Account a4 = new Account()
            .setAccountId("46473773348")
            .setAlias("Account Four")
            .setName("myacc4")
            .setRegions(Arrays.asList(new Region("us-east-1")))
            .setSdlc("prod");

    @Before
    public void setup(){
        Map<String, String> sdlcOverrides = new HashMap<>();
        sdlcOverrides.put("hello1", "myacc1, 123456789");
        sdlcOverrides.put("hello2", "myacc2");
        gatekeeperAccountProperties = new GatekeeperAccountProperties();
        MockitoAnnotations.initMocks(this);
        gatekeeperAccountProperties.setServiceURL("helloUrl");
        gatekeeperAccountProperties.setServiceURI("helloWorld");
        gatekeeperAccountProperties.setSdlcOverrides(sdlcOverrides);
        accountInformationService = new AccountInformationService(gatekeeperAccountProperties, backend2BackendService);
        Mockito.when(backend2BackendService.makeGetCall(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any())).thenReturn(new Account[]{a1, a2, a3, a4});
    }

    @Test
    public void testGetAccountsSdlcOverride(){
        List<Account> accounts = accountInformationService.getAccounts();
        Assert.assertEquals("hello1", accounts.get(0).getSdlc());
        Assert.assertEquals("hello2", accounts.get(1).getSdlc());
        Assert.assertEquals("hello1", accounts.get(2).getSdlc());
        Assert.assertEquals("prod", accounts.get(3).getSdlc());
    }
}
