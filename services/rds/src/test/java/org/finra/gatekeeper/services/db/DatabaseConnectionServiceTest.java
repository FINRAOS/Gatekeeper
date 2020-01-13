/*
 *
 * Copyright 2020. Gatekeeper Contributors
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

package org.finra.gatekeeper.services.db;

import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.Endpoint;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.model.*;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperRDSInstance;
import org.finra.gatekeeper.services.db.factory.DatabaseConnectionFactory;
import org.finra.gatekeeper.services.db.mockconnection.MockDBConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DatabaseConnectionServiceTest {

    @Mock
    private DatabaseConnectionFactory databaseConnectionFactory;

    @Mock
    private AccountInformationService accountInformationService;

    @Spy
    private MockDBConnection mockDBConnection;

    RdsQuery expectedRequest;
    RdsGrantAccessQuery expectedGrantRequest;
    RdsRevokeAccessQuery expectedRevokeRequest;
    RdsCheckUsersTableQuery expectedCheckUsersRequest;

    private final String TEST_ENGINE = "testengine";
    private final String TEST_UNSUPPORTED_ENGINE = "unsupportedEngine";
    private AWSRdsDatabase database;
    private AWSEnvironment environment;
    private GatekeeperRDSInstance supportedGatekeeperRDSInstance;
    private GatekeeperRDSInstance unsupportedGatekeeperRDSInstance;
    private DBInstance rdsInstance;
    private DBCluster auroraCluster;
    private Account account;
    private DbUser gkDbUser;
    private DbUser gkDbUser2;
    private DbUser nonGkDbUser;
    private String endpoint;

    private DatabaseConnectionService databaseConnectionService;

    @Before
    public void setUp() throws GKUnsupportedDBException {
        endpoint = "test-db-name.1234567890.us-east-1.rds.amazonaws.com";
        database = new AWSRdsDatabase()
                .setEngine(TEST_ENGINE)
                .setApplication("TESTAPP")
                .setDbName("testdb")
                .setInstanceId("db-123456")
                .setName("test-db-name")
                .setEndpoint(endpoint)
                .setStatus("Available")
                .setArn("testArn");

        supportedGatekeeperRDSInstance = new GatekeeperRDSInstance(database.getInstanceId(), database.getName(), database.getDbName(), database.getEngine(), database.getStatus(), database.getArn(),
                database.getEndpoint(), database.getApplication(), Arrays.asList("READONLY", "DBA", "DATAFIX"), true);
        unsupportedGatekeeperRDSInstance = new GatekeeperRDSInstance(database.getInstanceId(), database.getName(), database.getDbName(), TEST_UNSUPPORTED_ENGINE, database.getStatus(), database.getArn(),
                database.getEndpoint(), database.getApplication(), Arrays.asList("READONLY", "DBA", "DATAFIX"), true);
        rdsInstance = new DBInstance()
                .withDBInstanceIdentifier(database.getName())
                .withEngine(database.getEngine())
                .withDBName(database.getDbName())
                .withDbiResourceId(database.getInstanceId())
                .withEndpoint(new Endpoint().withAddress(database.getEndpoint()).withPort(5432))
                .withDBInstanceStatus(database.getStatus())
                .withDBInstanceArn(database.getArn());

        auroraCluster = new DBCluster()
                .withDBClusterIdentifier(database.getName())
                .withEngine(database.getEngine())
                .withDatabaseName(database.getDbName())
                .withDbClusterResourceId(database.getInstanceId())
                .withEndpoint(database.getEndpoint())
                .withPort(5432)
                .withStatus(database.getStatus())
                .withDBClusterArn(database.getArn());

        environment = new AWSEnvironment("test", "us-east-1", "DEV");

        account = new Account()
                .setName("Test Account")
                .setSdlc("DEV")
                .setAccountId("012345678901")
                .setAlias("test")
                .setGrouping(0);

        gkDbUser = new DbUser()
                .setUsername("gk_testhappy")
                .setRoles(Collections.singletonList("READONLY"));

        gkDbUser2 = new DbUser()
                .setUsername("gk_testhappy2")
                .setRoles(Collections.singletonList("READONLY"));

        nonGkDbUser = new DbUser()
                .setUsername("test")
                .setRoles(Collections.emptyList());

        String endpoint = database.getEndpoint() + "/" + database.getDbName();
        expectedRequest = new RdsQuery()
                .withAccount(account.getAlias())
                .withAccountId(account.getAccountId())
                .withRegion(environment.getRegion())
                .withSdlc(environment.getSdlc())
                .withAddress(endpoint)
                .withDbInstanceName(database.getName());
        expectedRequest = new RdsQuery()
                .withAccount(account.getAlias())
                .withAccountId(account.getAccountId())
                .withRegion(environment.getRegion())
                .withSdlc(environment.getSdlc())
                .withAddress(endpoint)
                .withDbInstanceName(database.getName());
        expectedRevokeRequest = new RdsRevokeAccessQuery(account.getAlias(), account.getAccountId(), environment.getRegion(), environment.getSdlc(), endpoint, database.getName());
        expectedGrantRequest = new RdsGrantAccessQuery(account.getAlias(), account.getAccountId(), environment.getRegion(), environment.getSdlc(), endpoint, database.getName());
        expectedCheckUsersRequest = new RdsCheckUsersTableQuery(account.getAlias(), account.getAccountId(), environment.getRegion(), environment.getSdlc(), endpoint, database.getName());

        Mockito.when(databaseConnectionFactory.getConnection(TEST_ENGINE)).thenReturn(mockDBConnection);
        Mockito.when(databaseConnectionFactory.getConnection(TEST_UNSUPPORTED_ENGINE)).thenThrow(new GKUnsupportedDBException("UnsupportedDB"));
        Mockito.when(accountInformationService.getAccountByAlias("test")).thenReturn(account);
        databaseConnectionService = new DatabaseConnectionService(databaseConnectionFactory, accountInformationService);
    }

    /*
     * Grant Access
     */
    @Test
    public void testGrantAccessHappy() throws Exception {
        ArgumentCaptor<RdsGrantAccessQuery> argumentCaptor = ArgumentCaptor.forClass(RdsGrantAccessQuery.class);
        String testUser = "tstuserhappy";
        String testPassword = "testpassword";
        Integer timeDays = 1;
        boolean outcome = databaseConnectionService.grantAccess(database, environment, testUser, RoleType.READONLY, testPassword, timeDays);
        Mockito.verify(mockDBConnection).grantAccess(argumentCaptor.capture());
        expectedGrantRequest.withUser(testUser)
                .withPassword(testPassword)
                .withRole(RoleType.READONLY)
                .withTime(1);
        RdsGrantAccessQuery actual = argumentCaptor.getValue();
        Assert.assertEquals(expectedGrantRequest, actual);
        Assert.assertTrue(outcome);
    }

    @Test
    public void testGrantAccessUnsupported() throws Exception {
        String testUser = "tstuser";
        String testPassword = "testpassword";
        Integer timeDays = 1;
        database.setEngine(TEST_UNSUPPORTED_ENGINE);
        boolean outcome = databaseConnectionService.grantAccess(database, environment, testUser, RoleType.READONLY, testPassword, timeDays);
        Assert.assertFalse(outcome);
    }

    @Test
    public void testGrantAccessError() throws Exception {
        String testPassword = "testpassword";
        Integer timeDays = 1;
        boolean outcome = databaseConnectionService.grantAccess(database, environment, null, RoleType.READONLY, testPassword, timeDays);
        Assert.assertFalse(outcome);
    }

    /*
     * Revoke Access
     */

    @Test
    public void testRevokeAccessHappy() throws Exception {
        ArgumentCaptor<RdsRevokeAccessQuery> argumentCaptor = ArgumentCaptor.forClass(RdsRevokeAccessQuery.class);
        String testUser = "tstuserhappy";
        boolean outcome = databaseConnectionService.revokeAccess(database, environment, RoleType.READONLY, testUser);
        Mockito.verify(mockDBConnection).revokeAccess(argumentCaptor.capture());
        expectedRevokeRequest.withUser(testUser)
                .withRole(RoleType.READONLY);
        RdsRevokeAccessQuery actualRevokeRequest = argumentCaptor.getValue();
        Assert.assertEquals(expectedRevokeRequest, actualRevokeRequest);
        Assert.assertTrue(outcome);
    }

    @Test
    public void testRevokeAccessUnsupportedDB() throws Exception {
        String testUser = "tstuserhappy";
        database.setEngine(TEST_UNSUPPORTED_ENGINE);
        boolean outcome = databaseConnectionService.revokeAccess(database, environment, RoleType.READONLY, testUser);
        expectedRevokeRequest.withUser(testUser)
                .withRole(RoleType.READONLY);
        Assert.assertFalse(outcome);
    }

    @Test
    public void testRevokeAccessException() throws Exception {
        String testUser = "tstuserhappy";
        boolean outcome = databaseConnectionService.revokeAccess(database, environment, RoleType.READONLY, null);
        expectedRevokeRequest.withUser(testUser)
                .withRole(RoleType.READONLY);
        Assert.assertFalse(outcome);
    }

    /*
     * Force Revoke Access
     */

    @Test
    public void testForceRevokeAccessHappySingle() throws Exception {
        ArgumentCaptor<RdsRevokeAccessQuery> argumentCaptor = ArgumentCaptor.forClass(RdsRevokeAccessQuery.class);
        List<DbUser> userList = Collections.singletonList(gkDbUser);
        List<String> removedUsers = databaseConnectionService.forceRevokeAccessUsersOnDatabase(supportedGatekeeperRDSInstance, environment, userList);
        Mockito.verify(mockDBConnection).revokeAccess(argumentCaptor.capture());
        expectedRevokeRequest.withUser(gkDbUser.getUsername());
        RdsRevokeAccessQuery actualRevokeRequest = argumentCaptor.getValue();
        Assert.assertEquals(expectedRevokeRequest, actualRevokeRequest);
        Assert.assertEquals(Collections.emptyList(), removedUsers);
    }

    @Test
    public void testForceRevokeAccessHappyMulti() throws Exception {
        ArgumentCaptor<RdsRevokeAccessQuery> argumentCaptor = ArgumentCaptor.forClass(RdsRevokeAccessQuery.class);
        List<DbUser> userList = Arrays.asList(gkDbUser, gkDbUser2);
        List<String> removedUsers = databaseConnectionService.forceRevokeAccessUsersOnDatabase(supportedGatekeeperRDSInstance, environment, userList);
        Mockito.verify(mockDBConnection, Mockito.times(2)).revokeAccess(argumentCaptor.capture());
        RdsRevokeAccessQuery actualRevokeRequest1 = argumentCaptor.getAllValues().get(0);
        RdsRevokeAccessQuery actualRevokeRequest2 = argumentCaptor.getAllValues().get(1);
        expectedRevokeRequest.withUser(gkDbUser.getUsername());
        Assert.assertEquals(expectedRevokeRequest, actualRevokeRequest1);
        expectedRevokeRequest.withUser(gkDbUser2.getUsername());
        Assert.assertEquals(expectedRevokeRequest, actualRevokeRequest2);
        Assert.assertEquals(Collections.emptyList(), removedUsers);
    }

    @Test
    public void testForceRevokeAccessHappyFailedOne() throws Exception {
        ArgumentCaptor<RdsRevokeAccessQuery> argumentCaptor = ArgumentCaptor.forClass(RdsRevokeAccessQuery.class);
        gkDbUser2.setUsername("gk_userfail");
        List<DbUser> userList = Arrays.asList(gkDbUser, gkDbUser2);
        List<String> removedUsers = databaseConnectionService.forceRevokeAccessUsersOnDatabase(supportedGatekeeperRDSInstance, environment, userList);
        Mockito.verify(mockDBConnection, Mockito.times(2)).revokeAccess(argumentCaptor.capture());
        RdsRevokeAccessQuery actualRevokeRequest1 = argumentCaptor.getAllValues().get(0);
        RdsRevokeAccessQuery actualRevokeRequest2 = argumentCaptor.getAllValues().get(1);
        expectedRevokeRequest.withUser(gkDbUser.getUsername());
        Assert.assertEquals(expectedRevokeRequest, actualRevokeRequest1);
        expectedRevokeRequest.withUser(gkDbUser2.getUsername());
        Assert.assertEquals(expectedRevokeRequest, actualRevokeRequest2);
        Assert.assertEquals(Collections.singletonList(gkDbUser2.getUsername()), removedUsers);
    }

    @Test(expected = GatekeeperException.class)
    public void testForceRevokeAccessNonGkUserIncluded() throws Exception {
        List<DbUser> userList = Arrays.asList(gkDbUser, gkDbUser2, nonGkDbUser);
        List<String> removedUsers = databaseConnectionService.forceRevokeAccessUsersOnDatabase(supportedGatekeeperRDSInstance, environment, userList);
        Assert.assertEquals(Collections.singletonList(gkDbUser2.getUsername()), removedUsers);
    }

    /*
     * getAvailableSchemas UI
     */

    @Test
    public void testGetAvailableSchemasForDbUIHappy() throws Exception {
        ArgumentCaptor<RdsQuery> argumentCaptor = ArgumentCaptor.forClass(RdsQuery.class);
        Map<RoleType, List<String>> result = databaseConnectionService.getAvailableSchemasForDb(supportedGatekeeperRDSInstance, environment);
        Mockito.verify(mockDBConnection).getAvailableTables(argumentCaptor.capture());
        RdsQuery actualCall = argumentCaptor.getValue();
        Assert.assertEquals(expectedRequest, actualCall);
        Assert.assertEquals(result.size(), 5);
    }

    @Test
    public void testGetAvailableSchemasForDbNonUIHappy() throws Exception {
        ArgumentCaptor<RdsQuery> argumentCaptor = ArgumentCaptor.forClass(RdsQuery.class);
        Map<RoleType, List<String>> result = databaseConnectionService.getAvailableSchemasForDb(database, environment);
        Mockito.verify(mockDBConnection).getAvailableTables(argumentCaptor.capture());
        RdsQuery actualCall = argumentCaptor.getValue();
        Assert.assertEquals(expectedRequest, actualCall);
        Assert.assertEquals(result.size(), 5);
    }

    /*
     * checkDb
     */

    @Test
    public void testCheckDbRdsHappy() throws Exception {
        ArgumentCaptor<RdsQuery> argumentCaptor = ArgumentCaptor.forClass(RdsQuery.class);
        String result = databaseConnectionService.checkDb(rdsInstance, environment);
        Mockito.verify(mockDBConnection).checkDb(argumentCaptor.capture());
        RdsQuery actualCall = argumentCaptor.getValue();
        //since this is using AWS provided arguments the endpoint is expected to be constructed with a combo of the port and the db
        expectedRequest.withAddress(endpoint+":5432/testdb");
        Assert.assertEquals(expectedRequest, actualCall);
        Assert.assertEquals("", result);
    }

    @Test
    public void testCheckDbRdsUnhappy() throws Exception {
        rdsInstance.setDBInstanceIdentifier("failthetest");
        String result = databaseConnectionService.checkDb(rdsInstance, environment);
        Assert.assertEquals("Failed", result);
    }

    @Test
    public void testCheckDbAuroraHappy() throws Exception {
        ArgumentCaptor<RdsQuery> argumentCaptor = ArgumentCaptor.forClass(RdsQuery.class);
        String result = databaseConnectionService.checkDb(auroraCluster, environment);
        Mockito.verify(mockDBConnection).checkDb(argumentCaptor.capture());
        RdsQuery actualCall = argumentCaptor.getValue();
        //since this is using AWS provided arguments the endpoint is expected to be constructed with a combo of the port and the db
        expectedRequest.withAddress(endpoint+":5432/testdb");
        Assert.assertEquals(expectedRequest, actualCall);
        Assert.assertEquals("", result);
    }

    @Test
    public void testCheckDbAuroraUnhappy() throws Exception {
        auroraCluster.setDBClusterIdentifier("failthetest");
        String result = databaseConnectionService.checkDb(auroraCluster, environment);
        Assert.assertEquals("Failed", result);
    }

    /*
     * getUsersForDb
     */

    @Test
    public void testGetUsersForDb() throws Exception {
        ArgumentCaptor<RdsQuery> argumentCaptor = ArgumentCaptor.forClass(RdsQuery.class);
        List<DbUser> result = databaseConnectionService.getUsersForDb(supportedGatekeeperRDSInstance, environment);
        Mockito.verify(mockDBConnection).getUsers(argumentCaptor.capture());
        RdsQuery actualCall = argumentCaptor.getValue();
        Assert.assertEquals(expectedRequest, actualCall);
        Assert.assertEquals(2, result.size());
    }

    /*
     * getAvailableRolesForDb
     */

    @Test
    public void testGetAvailableRolesForDbRds() throws Exception {
        ArgumentCaptor<RdsQuery> argumentCaptor = ArgumentCaptor.forClass(RdsQuery.class);
        List<String> result = databaseConnectionService.getAvailableRolesForDb(rdsInstance, environment);
        Mockito.verify(mockDBConnection).getAvailableRoles(argumentCaptor.capture());
        RdsQuery actualCall = argumentCaptor.getValue();
        //since this is using AWS provided arguments the endpoint is expected to be constructed with a combo of the port and the db
        expectedRequest.withAddress(endpoint+":5432/testdb");
        Assert.assertEquals(expectedRequest, actualCall);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void testGetAvailableRolesForDbAurora() throws Exception {
        ArgumentCaptor<RdsQuery> argumentCaptor = ArgumentCaptor.forClass(RdsQuery.class);
        List<String> result = databaseConnectionService.getAvailableRolesForDb(auroraCluster, environment);
        Mockito.verify(mockDBConnection).getAvailableRoles(argumentCaptor.capture());
        RdsQuery actualCall = argumentCaptor.getValue();
        //since this is using AWS provided arguments the endpoint is expected to be constructed with a combo of the port and the db
        expectedRequest.withAddress(endpoint+":5432/testdb");
        Assert.assertEquals(expectedRequest, actualCall);
        Assert.assertEquals(3, result.size());
    }

    /*
     * checkUsersAndDb
     */

    @Test
    public void testCheckUsersAndDbs() throws Exception {
        ArgumentCaptor<RdsCheckUsersTableQuery> argumentCaptor = ArgumentCaptor.forClass(RdsCheckUsersTableQuery.class);
        List<UserRole> roles = Arrays.asList(new UserRole().setRole("READONLY"));
        List<User> users = Arrays.asList(new User().setUserId("happyTestUser"));
        List<String> result = databaseConnectionService.checkUsersAndDbs(roles, users, database, environment);
        Mockito.verify(mockDBConnection).checkIfUsersHasTables(argumentCaptor.capture());
        expectedCheckUsersRequest.withUsers(Arrays.asList("testUserHappy"));
        RdsCheckUsersTableQuery actualCall = argumentCaptor.getValue();
        Assert.assertEquals(expectedRequest, actualCall);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testCheckUsersAndDbsError() throws Exception {
        ArgumentCaptor<RdsCheckUsersTableQuery> argumentCaptor = ArgumentCaptor.forClass(RdsCheckUsersTableQuery.class);
        List<UserRole> roles = Arrays.asList(new UserRole().setRole("READONLY"));
        List<User> users = Arrays.asList(new User().setUserId("testUser"), new User().setUserId("happyTestUser"));
        List<String> result = databaseConnectionService.checkUsersAndDbs(roles, users, database, environment);
        Mockito.verify(mockDBConnection).checkIfUsersHasTables(argumentCaptor.capture());
        expectedCheckUsersRequest.withUsers(Arrays.asList("testUserHappy"));
        RdsCheckUsersTableQuery actualCall = argumentCaptor.getValue();
        Assert.assertEquals(expectedRequest, actualCall);
        Assert.assertTrue(result.isEmpty());
    }
}
