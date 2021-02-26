package org.finra.gatekeeper.services.group;

import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;
import org.finra.gatekeeper.services.group.service.GatekeeperGroupAuthService;
import org.finra.gatekeeper.services.group.service.GatekeeperLdapGroupLookupService;
import org.finra.gatekeeper.services.group.service.GatekeeperLdapRoleLookupService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GatekeeperGroupAuthServiceTest {


    private GatekeeperGroupAuthService gatekeeperGroupAuthService;

    @Mock
    private GatekeeperLdapGroupLookupService gatekeeperLdapGroupLookupService;
    @Mock
    private GatekeeperRdsAuthProperties rdsAuthProperties;
    @Mock
    private GatekeeperLdapRoleLookupService gatekeeperLdapRoleLookupService;
    @Mock
    private AccessRequestWrapper request;
    @Mock
    private GatekeeperUserEntry requestor;




    @Before
    public void initMocks(){
        Map<String, Set<GatekeeperADGroupEntry>> expectedReturn = new HashMap<String, Set<GatekeeperADGroupEntry>>();
        Set<GatekeeperADGroupEntry> petSet = new HashSet<>();
        Set<GatekeeperADGroupEntry> escSet = new HashSet<>();

        petSet.add(new GatekeeperADGroupEntry("PET", "RO", "D", "APP_GK_PET_RO_D"));
        petSet.add(new GatekeeperADGroupEntry("PET", "DBA", "D", "APP_GK_PET_DBA_D"));
        escSet.add(new GatekeeperADGroupEntry("ESC", "RO", "D", "APP_GK_ESC_RO_D"));

        expectedReturn.put("PET", petSet);
        expectedReturn.put("ESC", escSet);

        when(gatekeeperLdapGroupLookupService.getLdapAdGroups()).thenReturn(expectedReturn);

        gatekeeperGroupAuthService = new GatekeeperGroupAuthService(gatekeeperLdapGroupLookupService, rdsAuthProperties, gatekeeperLdapRoleLookupService);
    }

    @Test
    public void properRequest(){
        //Not unrestricted
        char[] sdlc = new char[1];
        sdlc[0] = ' ';
        when(rdsAuthProperties.getUnrestrictedSDLC()).thenReturn(sdlc);

        //Mock Instance
        AWSRdsDatabase database = new AWSRdsDatabase();
        database.setApplication("PET");
        List<AWSRdsDatabase> databaseList = new ArrayList<>();
        databaseList.add(database);
        when(request.getInstances()).thenReturn(databaseList);
        when(request.getAccountSdlc()).thenReturn("DEV");

        //Mock User
        User testUser = new User();
        testUser.setUserId("testUser");
        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        when(request.getUsers()).thenReturn(userList);
        when(requestor.getUserId()).thenReturn("testUser");

        //Mock roles
        UserRole ro = new UserRole().setRole("READONLY");
        UserRole dba = new UserRole().setRole("DBA");
        List<UserRole> roleList = new ArrayList<>();
        roleList.add(ro);
        roleList.add(dba);
        when(request.getRoles()).thenReturn(roleList);

        //Mock userRoles
        Map<String, Set<GatekeeperADGroupEntry>> userRoles = new HashMap<String, Set<GatekeeperADGroupEntry>>();
        Set<GatekeeperADGroupEntry> petSet = new HashSet<>();

        petSet.add(new GatekeeperADGroupEntry("PET", "RO", "D", "APP_GK_PET_RO_D"));
        petSet.add(new GatekeeperADGroupEntry("PET", "RO", "Q", "APP_GK_PET_RO_Q"));
        petSet.add(new GatekeeperADGroupEntry("PET", "DBA", "D", "APP_GK_PET_DBA_D"));

        userRoles.put("PET", petSet);
        when(gatekeeperLdapRoleLookupService.getLdapAdRoles("testUser")).thenReturn(userRoles);

        Assert.assertEquals("Allowed", gatekeeperGroupAuthService.hasGroupAuth(request, requestor));

    }

    @Test
    public void userMissingRole(){
        //Not unrestricted
        char[] sdlc = new char[1];
        sdlc[0] = ' ';
        when(rdsAuthProperties.getUnrestrictedSDLC()).thenReturn(sdlc);


        //Mock Instance
        AWSRdsDatabase database = new AWSRdsDatabase();
        database.setApplication("PET");
        List<AWSRdsDatabase> databaseList = new ArrayList<>();
        databaseList.add(database);
        when(request.getInstances()).thenReturn(databaseList);
        when(request.getAccountSdlc()).thenReturn("DEV");

        //Mock User
        User testUser = new User();
        testUser.setUserId("testUser");
        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        when(request.getUsers()).thenReturn(userList);
        when(requestor.getUserId()).thenReturn("testUser");

        //Mock roles
        UserRole ro = new UserRole().setRole("READONLY");
        UserRole dba = new UserRole().setRole("DBA");
        List<UserRole> roleList = new ArrayList<>();
        roleList.add(ro);
        roleList.add(dba);
        when(request.getRoles()).thenReturn(roleList);

        //Mock userRoles
        Map<String, Set<GatekeeperADGroupEntry>> userRoles = new HashMap<String, Set<GatekeeperADGroupEntry>>();
        Set<GatekeeperADGroupEntry> petSet = new HashSet<>();

        petSet.add(new GatekeeperADGroupEntry("PET", "RO", "D", "APP_GK_PET_RO_D"));

        userRoles.put("PET", petSet);
        when(gatekeeperLdapRoleLookupService.getLdapAdRoles("testUser")).thenReturn(userRoles);

        Assert.assertEquals("User does not have the following groups: APP_GK_PET_DBA_D", gatekeeperGroupAuthService.hasGroupAuth(request, requestor));

    }
    @Test
    public void userMissingAllRoles(){
        //Not unrestricted
        char[] sdlc = new char[1];
        sdlc[0] = ' ';
        when(rdsAuthProperties.getUnrestrictedSDLC()).thenReturn(sdlc);


        //Mock Instance
        AWSRdsDatabase database = new AWSRdsDatabase();
        database.setApplication("PET");
        List<AWSRdsDatabase> databaseList = new ArrayList<>();
        databaseList.add(database);
        when(request.getInstances()).thenReturn(databaseList);
        when(request.getAccountSdlc()).thenReturn("DEV");

        //Mock User
        User testUser = new User();
        testUser.setUserId("testUser");
        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        when(request.getUsers()).thenReturn(userList);
        when(requestor.getUserId()).thenReturn("testUser");

        //Mock roles
        UserRole ro = new UserRole().setRole("READONLY");
        UserRole dba = new UserRole().setRole("DBA");
        List<UserRole> roleList = new ArrayList<>();
        roleList.add(ro);
        roleList.add(dba);
        when(request.getRoles()).thenReturn(roleList);

        //Mock userRoles
        Map<String, Set<GatekeeperADGroupEntry>> userRoles = new HashMap<String, Set<GatekeeperADGroupEntry>>();
        Set<GatekeeperADGroupEntry> petSet = new HashSet<>();

        userRoles.put("PET", petSet);
        when(gatekeeperLdapRoleLookupService.getLdapAdRoles("testUser")).thenReturn(userRoles);

        Assert.assertEquals("User does not have the following groups: APP_GK_PET_RO_D , APP_GK_PET_DBA_D", gatekeeperGroupAuthService.hasGroupAuth(request, requestor));

    }

    @Test
    public void denyAccessForMoreThan1User(){
        //Not unrestricted
        char[] sdlc = new char[1];
        sdlc[0] = ' ';
        when(rdsAuthProperties.getUnrestrictedSDLC()).thenReturn(sdlc);

        //Mock Instance
        AWSRdsDatabase database = new AWSRdsDatabase();
        database.setApplication("PET");
        List<AWSRdsDatabase> databaseList = new ArrayList<>();
        databaseList.add(database);
        when(request.getInstances()).thenReturn(databaseList);
        when(request.getAccountSdlc()).thenReturn("DEV");

        //Mock User
        User testUser = new User();
        testUser.setUserId("testUser");
        User testUser2 = new User();
        testUser.setUserId("testUser2");

        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        userList.add(testUser2);
        when(request.getUsers()).thenReturn(userList);
        when(requestor.getUserId()).thenReturn("testUser");



        Assert.assertEquals("User may only request access for themselves for this AGS", gatekeeperGroupAuthService.hasGroupAuth(request, requestor));

    }

    @Test
    public void denyAccessForAnotherUser(){
        //Not unrestricted
        char[] sdlc = new char[1];
        sdlc[0] = ' ';
        when(rdsAuthProperties.getUnrestrictedSDLC()).thenReturn(sdlc);

        //Mock Instance
        AWSRdsDatabase database = new AWSRdsDatabase();
        database.setApplication("PET");
        List<AWSRdsDatabase> databaseList = new ArrayList<>();
        databaseList.add(database);
        when(request.getInstances()).thenReturn(databaseList);
        when(request.getAccountSdlc()).thenReturn("DEV");

        //Mock User
        User testUser = new User();
        testUser.setUserId("testUser2");

        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        when(request.getUsers()).thenReturn(userList);
        when(requestor.getUserId()).thenReturn("testUser");



        Assert.assertEquals("User may only request access for themselves for this AGS", gatekeeperGroupAuthService.hasGroupAuth(request, requestor));

    }

    @Test
    public void unrestrictedSDLC() {
        //Not unrestricted
        char[] sdlc = new char[1];
        sdlc[0] = 'D';
        when(rdsAuthProperties.getUnrestrictedSDLC()).thenReturn(sdlc);

        //Mock Instance
        AWSRdsDatabase database = new AWSRdsDatabase();
        database.setApplication("PET");
        List<AWSRdsDatabase> databaseList = new ArrayList<>();
        databaseList.add(database);
        when(request.getInstances()).thenReturn(databaseList);
        when(request.getAccountSdlc()).thenReturn("DEV");

        Assert.assertEquals("Allowed", gatekeeperGroupAuthService.hasGroupAuth(request, requestor));

    }

    @Test
    public void unrestrictedAGS() {
        //Not unrestricted
        char[] sdlc = new char[1];
        sdlc[0] = ' ';
        when(rdsAuthProperties.getUnrestrictedSDLC()).thenReturn(sdlc);

        //Mock Instance
        AWSRdsDatabase database = new AWSRdsDatabase();
        database.setApplication("TEST");
        List<AWSRdsDatabase> databaseList = new ArrayList<>();
        databaseList.add(database);
        when(request.getInstances()).thenReturn(databaseList);
        when(request.getAccountSdlc()).thenReturn("DEV");

        Assert.assertEquals("Allowed", gatekeeperGroupAuthService.hasGroupAuth(request, requestor));

    }


}
