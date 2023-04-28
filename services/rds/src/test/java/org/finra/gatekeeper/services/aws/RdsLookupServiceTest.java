/*
 *
 * Copyright 2022. Gatekeeper Contributors
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

package org.finra.gatekeeper.services.aws;

import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.*;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.interfaces.GKUserCredentialsProvider;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperRDSInstance;
import org.finra.gatekeeper.services.aws.model.DatabaseType;
import org.finra.gatekeeper.services.db.DatabaseConnectionService;
import org.finra.gatekeeper.services.group.service.GatekeeperLdapGroupLookupService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RdsLookupServiceTest {

    @Mock
    private AwsSessionService awsSessionService;
    @Mock
    private DatabaseConnectionService databaseConnectionService;
    @Mock
    private SGLookupService sgLookupService;
    @Mock
    private GatekeeperLdapGroupLookupService rdsGroupLookupService;
    @Mock
    private GKUserCredentialsProvider gkUserCredentialsProvider;

    private GatekeeperProperties gatekeeperProperties;
    @Mock
    private AmazonRDSClient amazonRDSClient;

    private RdsLookupService rdsLookupService;
    private AWSEnvironment test;

    @Captor
    private ArgumentCaptor<DescribeDBClustersRequest> captor;
    @Captor
    private ArgumentCaptor<List<DBCluster>> dbClusterCaptor;

    private final String RDS_ENGINE_PG = "postgres";
    private final String RDS_ENGINE_ORACLE = "oracle";
    private final String AURORA_ENGINE = "aurora-postgresql";
    private final String APP_IDENTITY = "Application";
    private final String STATUS_AVAILABLE = "available";
    private final String STATUS_STOPPED = "stopped";
    private final String SG_ONE = "sg-123";
    private final String SG_TWO = "sg-456";
    private final String DB_NAME = "postgres";
    private final Integer DB_PORT = 5432;
    private final List<String> roles = Arrays.asList("gk_readonly", "gk_datafix", "gk_dba");
    private final AWSRdsDatabase postgres = new AWSRdsDatabase()
            .setEngine(RDS_ENGINE_PG);

    private final AWSRdsDatabase oracle = new AWSRdsDatabase()
            .setEngine(RDS_ENGINE_ORACLE);

    private DBInstance dbA, dbB, dbC, dbD, dbE, dbF, dbG, dbH, dbI;
    private DBCluster clusterA, clusterB, clusterC, clusterD, clusterE, clusterF, clusterG, clusterH, clusterI, clusterJ;


    @Before
    public void setUp() throws Exception {
        gatekeeperProperties = new GatekeeperProperties()
                .setAppIdentityTag(APP_IDENTITY);

        Mockito.when(rdsGroupLookupService.getLdapAdGroups()).thenReturn(new HashMap<>());

        rdsLookupService = new RdsLookupService(awsSessionService, databaseConnectionService, sgLookupService, gatekeeperProperties, rdsGroupLookupService);
        test = new AWSEnvironment("test", "test", "dev");

        Mockito.when(awsSessionService.getRDSSession(test)).thenReturn(amazonRDSClient);

        //RDS
        Mockito.when(amazonRDSClient.describeDBInstances(Mockito.any())).thenReturn(initializeInstances());
        Mockito.when(amazonRDSClient.describeOptionGroups(Mockito.any())).thenReturn(new DescribeOptionGroupsResult()
                .withOptionGroupsList(
                        new OptionGroup()
                                .withOptionGroupName("test-og")
                                .withOptions(new Option().withOptionName("SSL").withPort(1234))));
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(dbA), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(dbB), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(dbC), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(dbD), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(dbE), Mockito.any())).thenThrow(GKUnsupportedDBException.class);
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(dbF), Mockito.any())).thenReturn("Gatekeeper user missing createrole");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(dbG), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(dbH), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(dbI), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.getAvailableRolesForDb(Mockito.eq(dbA), Mockito.any())).thenReturn(roles);
        Mockito.when(databaseConnectionService.getAvailableRolesForDb(Mockito.eq(dbI), Mockito.any())).thenReturn(roles);
        Mockito.when(databaseConnectionService.getAvailableRolesForDb(Mockito.eq(dbG), Mockito.any())).thenThrow(new Exception("Unable to get roles"));
        Mockito.when(databaseConnectionService.getAvailableRolesForDb(Mockito.eq(dbH), Mockito.any())).thenThrow(new Exception("Unable to login: password authentication failed"));

        //AURORA
        Mockito.when(amazonRDSClient.describeDBClusters(Mockito.any())).thenReturn(initializeClusters());
        Mockito.when(amazonRDSClient.listTagsForResource(Mockito.any())).thenReturn(initializeTags());
        Mockito.when(amazonRDSClient.describeGlobalClusters(Mockito.any())).thenReturn(initializeGlobalClusters());
        Mockito.when(sgLookupService.fetchSgsForAccountRegion(test)).thenReturn(Arrays.asList(SG_ONE, SG_TWO));
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterA), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterB), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterC), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterD), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterE), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterF), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterG), Mockito.any())).thenThrow(GKUnsupportedDBException.class);
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterH), Mockito.any())).thenReturn("Gatekeeper user missing createrole");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterI), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.checkDb(Mockito.eq(clusterJ), Mockito.any())).thenReturn("");
        Mockito.when(databaseConnectionService.getAvailableRolesForDb(Mockito.eq(clusterA), Mockito.any())).thenReturn(roles);
        Mockito.when(databaseConnectionService.getAvailableRolesForDb(Mockito.eq(clusterI), Mockito.any())).thenThrow(new Exception("Unable to get roles"));
        Mockito.when(databaseConnectionService.getAvailableRolesForDb(Mockito.eq(clusterJ), Mockito.any())).thenThrow(new Exception("Unable to login: password authentication failed"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void unsupportedTestGetInstancesFilter(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, "zzz", "gk");
        Assert.assertEquals(0, instances.size());
    }

    @Test
    public void rdsTestGetInstancesFilter(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk");
        Assert.assertEquals(8, instances.size());
        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-A");
        Assert.assertEquals(1, instances.size());
        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-B");
        Assert.assertEquals(0, instances.size());
    }

    @Test
    public void rdsTestGetInstancesHappy(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-A");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertTrue(instance.getEnabled());
        Assert.assertEquals("TEST", instance.getApplication());
        Assert.assertEquals("available", instance.getStatus());
        Assert.assertEquals(new HashSet<>(Arrays.asList("gk_readonly","gk_datafix","gk_dba")), new HashSet<>(instance.getAvailableRoles()));
    }

    @Test
    public void rdsTestGetInstancesMissingSgs(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-C");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_MISSING_SGS, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void rdsTestGetInstancesMissingReadReplica(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-D");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals("Unsupported (Read-Only replica of replica1)", instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void rdsTestGetInstancesUnsupported(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-E");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_UNSUPPORTED_DB_ENGINE, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void rdsTestGetInstanceMisconfiguredUser(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-F");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals("Gatekeeper user missing createrole", instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void rdsTestGetInstanceCantGetRoles(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-G");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_COULD_NOT_FETCH_ROLES, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void rdsTestGetInstanceCantLogin(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-H");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_UNABLE_TO_LOGIN, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void rdsTestGetInstanceOracle(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.RDS.toString(), "gk-I");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals("TEST", instance.getApplication());
        Assert.assertEquals("available", instance.getStatus());
        Assert.assertEquals(new HashSet<>(Arrays.asList("gk_readonly","gk_datafix","gk_dba")), new HashSet<>(instance.getAvailableRoles()));
        Assert.assertEquals("instance5:1234", instance.getEndpoint());
    }

    //AURORA
    @Test
    public void auroraTestGetInstancesFilter(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk");
        Assert.assertEquals(10, instances.size());
        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-A");
        Assert.assertEquals(1, instances.size());
        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "cluster-gk2");
        Assert.assertEquals(1, instances.size());
        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "clusta-gk2");
        Assert.assertEquals(0, instances.size());
    }

    @Test
    public void auroraTestGetInstancesHappy(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-A");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertTrue(instance.getEnabled());
        Assert.assertEquals("TEST", instance.getApplication());
        Assert.assertEquals("available", instance.getStatus());
        Assert.assertEquals(new HashSet<>(Arrays.asList("gk_readonly","gk_datafix","gk_dba")), new HashSet<>(instance.getAvailableRoles()));
    }

    @Test
    public void auroraTestGetInstancesReadReplica(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-C");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals("Unsupported (Read-Only replica of replica1)", instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void auroraTestGetInstancesStopped(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-B");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(STATUS_STOPPED, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void auroraTestGetInstancesMissingSgs(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-D");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_MISSING_SGS, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void auroraTestGetInstancesEmptyCluster(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-E");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_NO_INSTANCES, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void auroraTestGetInstancesNoWriters(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-F");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_NO_WRITERS, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void auroraTestGetInstanceUnsupported(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-G");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_UNSUPPORTED_DB_ENGINE, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void auroraTestGetInstanceMisconfiguredUser(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-H");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals("Gatekeeper user missing createrole", instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void auroraTestGetInstanceCantGetRoles(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-I");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_COULD_NOT_FETCH_ROLES, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void auroraTestGetInstanceCantLogin(){
        List<GatekeeperRDSInstance> instances;

        instances = rdsLookupService.getInstances(test, DatabaseType.AURORA_REGIONAL.toString(), "gk-J");
        Assert.assertEquals(1, instances.size());
        GatekeeperRDSInstance instance = instances.get(0);
        Assert.assertEquals(RdsLookupService.STATUS_UNABLE_TO_LOGIN, instance.getStatus());
        Assert.assertFalse(instance.getEnabled());
    }

    @Test
    public void rdsTestGetOneInstance(){
        Mockito.when(amazonRDSClient.describeDBInstances(Mockito.any())).thenReturn(
                new DescribeDBInstancesResult().withDBInstances(
                        initializeInstance("instance1", RDS_ENGINE_PG, "gk-A-instance", "db-gk1", STATUS_AVAILABLE, SG_ONE, null)));
        Optional<GatekeeperRDSInstance> instance = rdsLookupService.getOneInstance(test, "gk-A-instance", "gk-A-instance", "RDS");
        Assert.assertTrue(instance.isPresent());
        Assert.assertEquals("TEST", instance.get().getApplication());
        Assert.assertEquals("available", instance.get().getStatus());
        Assert.assertEquals(new HashSet<>(Arrays.asList("gk_readonly","gk_datafix","gk_dba")), new HashSet<>(instance.get().getAvailableRoles()));
    }

    @Test
    public void auroraTestGetOneInstance(){
        Mockito.when(amazonRDSClient.describeDBClusters(Mockito.any())).thenReturn(
                new DescribeDBClustersResult().withDBClusters(
                        initializeCluster("dbendpoint1", "gk-A-cluster", "cluster-gk1", STATUS_AVAILABLE, SG_ONE, null, 2, true))
        );
        Optional<GatekeeperRDSInstance> instance = rdsLookupService.getOneInstance(test, "cluster-gk1", "gk-A-cluster", "AURORA_REGIONAL");
        Assert.assertTrue(instance.isPresent());
        Assert.assertEquals("TEST", instance.get().getApplication());
        Assert.assertEquals("available", instance.get().getStatus());
        Assert.assertEquals(new HashSet<>(Arrays.asList("gk_readonly","gk_datafix","gk_dba")), new HashSet<>(instance.get().getAvailableRoles()));
    }

    //AURORA Global

    @Test
    public void auroraGlobalTestGetInstancesFilter(){
        // Verify that the aws SDK is only called for the 3 faked aurora global clusters that were provided
        // This method fetches all of the global clusters that match the text filter and then goes to pull down the primary cluster from the instance.
        // once it has all of the primary clusters it will then go and make a DescribeDbClusters call with the primary cluster arn's that were part of the global cluster
        // once that is done it will go and call the regular aurora code with the name of the cluster being set as the name of the global cluster instead.

        rdsLookupService.getInstances(test, DatabaseType.AURORA_GLOBAL.toString(), "gcluster-");
        Mockito.verify(amazonRDSClient, Mockito.times(1)).describeDBClusters(captor.capture());
        Assert.assertEquals(3, captor.getValue().getFilters().get(0).getValues().size());
    }

    private DescribeDBInstancesResult initializeInstances(){
        return new DescribeDBInstancesResult().withDBInstances(Arrays.asList(
                (dbA = initializeInstance("instance1", RDS_ENGINE_PG, "gk-A-instance", "db-gk1", STATUS_AVAILABLE, SG_ONE, null)),
                (dbC = initializeInstance("instance3", RDS_ENGINE_PG, "gk-C-instance", "db-gk3", STATUS_AVAILABLE, "gk-unsupport", null)),
                (dbD = initializeInstance("instance4", RDS_ENGINE_PG, "gk-D-instance", "db-gk4", STATUS_AVAILABLE, SG_ONE, "replica1")),
                (dbE = initializeInstance("unsupported", RDS_ENGINE_PG, "gk-E-instance", "db-gk5", STATUS_AVAILABLE, SG_TWO, null)),
                (dbF = initializeInstance("missing", RDS_ENGINE_PG, "gk-F-instance", "db-gk6", STATUS_AVAILABLE, SG_TWO, null)),
                (dbG = initializeInstance("rolesfailunable", RDS_ENGINE_PG, "gk-G-instance", "db-gk7", STATUS_AVAILABLE, SG_TWO, null)),
                (dbH = initializeInstance("rolesfailpassword", RDS_ENGINE_PG, "gk-H-instance", "db-gk8", STATUS_AVAILABLE, SG_TWO, null)),
                (dbI = initializeInstance("instance5", RDS_ENGINE_ORACLE, "gk-I-instance", "db-gk9", STATUS_AVAILABLE, SG_TWO, null))
        ));
    }

    private DescribeDBClustersResult initializeClusters(){
        return new DescribeDBClustersResult().withDBClusters(Arrays.asList(
                (clusterA = initializeCluster("dbendpoint1", "gk-A-cluster", "cluster-gk1", STATUS_AVAILABLE, SG_ONE, null, 2, true)),
                (clusterB = initializeCluster("dbendpoint2", "gk-B-cluster", "cluster-gk2", STATUS_STOPPED, SG_ONE, null, 1, true)),
                (clusterC = initializeCluster("dbendpoint3", "gk-C-cluster", "cluster-gk3", STATUS_AVAILABLE, SG_TWO, "replica1", 1, true)),
                (clusterD = initializeCluster("dbendpoint4", "gk-D-cluster", "cluster-gk4", STATUS_AVAILABLE, "gk-unsupportedsg", null, 3, true)),
                (clusterE = initializeCluster("dbendpoint5", "gk-E-cluster", "cluster-gk5", STATUS_AVAILABLE, SG_TWO, null, 0, true)),
                (clusterF = initializeCluster("dbendpoint6", "gk-F-cluster", "cluster-gk6", STATUS_AVAILABLE, SG_ONE, null, 2, false)),
                (clusterG = initializeCluster("unsupported", "gk-G-cluster", "cluster-gk7", STATUS_AVAILABLE, SG_ONE, null, 2, true)),
                (clusterH = initializeCluster("missing", "gk-H-cluster", "cluster-gk8", STATUS_AVAILABLE, SG_ONE, null, 2, true)),
                (clusterI = initializeCluster("rolesfailunable", "gk-I-cluster", "cluster-gk9", STATUS_AVAILABLE, SG_ONE, null, 2, true)),
                (clusterJ = initializeCluster("rolesfailpassword", "gk-J-cluster", "cluster-gk10", STATUS_AVAILABLE, SG_ONE, null, 2, true))
        ));
    }

    private DBInstance initializeInstance(String endpoint, String engine, String instanceId, String dbiResourceId, String status, String sg, String readreplica){
        return new DBInstance()
                .withEndpoint(new Endpoint()
                        .withAddress(endpoint)
                        .withPort(9999))
                .withEngine(engine)
                .withDBName("postgres")
                .withDBInstanceIdentifier(instanceId)
                .withDbiResourceId(dbiResourceId)
                .withDBInstanceStatus(status)
                .withVpcSecurityGroups(
                        new VpcSecurityGroupMembership()
                            .withVpcSecurityGroupId(sg)
                )
                .withIAMDatabaseAuthenticationEnabled(false)
                .withReadReplicaSourceDBInstanceIdentifier(readreplica)
                .withOptionGroupMemberships(
                        new OptionGroupMembership().withOptionGroupName("test-og")
                );
    }

    private DBCluster initializeCluster(String endpoint, String clusterId, String clusterResourceId, String status, String sg, String readreplica, int numInstances, boolean hasWriter){
        List<DBClusterMember> members = new ArrayList<>();
        for(int i = 0; i < numInstances; i++){
            members.add(new DBClusterMember().withDBInstanceIdentifier("member"+i).withIsClusterWriter(i == 1 && hasWriter));
        }

        return new DBCluster().withEndpoint(endpoint)
                .withEngine(AURORA_ENGINE)
                .withDBClusterIdentifier(clusterId)
                .withDbClusterResourceId(clusterResourceId)
                .withDatabaseName("postgres")
                .withPort(5432)
                .withStatus(status)
                .withVpcSecurityGroups(
                        new VpcSecurityGroupMembership()
                                .withVpcSecurityGroupId(sg)
                )
                .withDBClusterMembers(members)
                .withIAMDatabaseAuthenticationEnabled(false)
                .withReplicationSourceIdentifier(readreplica);

    }

    private DescribeGlobalClustersResult initializeGlobalClusters(){
        return new DescribeGlobalClustersResult().withGlobalClusters(Arrays.asList(
                //Have to include "test" or whatever matches the region for the test AWS Environment due to only looking for regional clusters in the current region
                initializeGlobalCluster("gcluster-1", "test-member1", "reader1"),
                initializeGlobalCluster("gcluster-2", "test-member5", "reader2"),
                initializeGlobalCluster("gcluster-3", "test-member8", "reader3")
        ));
    }

    private GlobalCluster initializeGlobalCluster(String globalClusterId, String primaryClusterId, String readerClusterId){
        return new GlobalCluster()
                .withDatabaseName("test")
                .withEngine("aurora-postgres")
                .withGlobalClusterIdentifier(globalClusterId)
                .withGlobalClusterMembers(
                        new GlobalClusterMember()
                            .withDBClusterArn(primaryClusterId)
                            .withIsWriter(true),
                        new GlobalClusterMember()
                            .withDBClusterArn(readerClusterId)
                            .withIsWriter(false)
                );
    }
    private ListTagsForResourceResult initializeTags(){
        return new ListTagsForResourceResult().withTagList(
                new Tag().withKey(APP_IDENTITY)
                        .withValue("TEST"));
    }
}
