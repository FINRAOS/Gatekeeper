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

package org.finra.gatekeeper.services.aws;

import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.*;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.interfaces.GKUserCredentialsProvider;
import org.finra.gatekeeper.rds.model.DbUser;
import org.finra.gatekeeper.rds.model.RoleType;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperRDSInstance;
import org.finra.gatekeeper.services.aws.model.LookupType;
import org.finra.gatekeeper.services.db.DatabaseConnectionService;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service that handles interfacing with the AWS for everything RDS related
 */
@Component
public class RdsLookupService {

    private final Logger logger = LoggerFactory.getLogger(RdsLookupService.class);

    protected static final String STATUS_MISSING_SGS = "Missing Gatekeeper RDS support Security Group";
    protected static final String STATUS_NO_INSTANCES = "This Cluster has no associated instances";
    protected static final String STATUS_NO_WRITERS = "This Cluster has no associated writer instances";
    protected static final String STATUS_UNSUPPORTED_DB_ENGINE = "DB Engine not supported";
    protected static final String STATUS_COULD_NOT_FETCH_ROLES = "Could not fetch roles available to DB";
    protected static final String STATUS_UNABLE_TO_LOGIN = "Gatekeeper user does not exist or password is incorrect.";

    private final AwsSessionService awsSessionService;
    private final DatabaseConnectionService databaseConnectionService;
    private final SGLookupService sgLookupService;
    private final GatekeeperProperties gatekeeperProperties;
    private final String STATUS_AVAILABLE = "available";
    private final String STATUS_BACKING_UP = "backing-up";

    @Autowired
    public RdsLookupService(AwsSessionService awsSessionService,
                            DatabaseConnectionService databaseConnectionService,
                            SGLookupService sgLookupService,
                            GatekeeperProperties gatekeeperProperties) {
        this.awsSessionService = awsSessionService;
        this.databaseConnectionService = databaseConnectionService;
        this.sgLookupService = sgLookupService;
        this.gatekeeperProperties = gatekeeperProperties;
    }


    public List<GatekeeperRDSInstance> getInstances(AWSEnvironment environment, String lookupType, String searchString) {
        switch(LookupType.valueOf(lookupType.toUpperCase())){
            case RDS:
                return doFilterRds(environment, searchString);
            case AURORA:
                return doFilterAurora(environment, searchString);
            default:
                return Collections.emptyList();
        }
    }

    public Optional<GatekeeperRDSInstance> getOneInstance(AWSEnvironment environment, String dbInstanceIdentifier, String instanceName) {
        logger.info(dbInstanceIdentifier);
        Long startTime = System.currentTimeMillis();
        List<String> securityGroupIds = sgLookupService.fetchSgsForAccountRegion(environment);
        AmazonRDSClient amazonRDSClient = awsSessionService.getRDSSession(environment);
        List<GatekeeperRDSInstance> gatekeeperRDSInstances;

        //if it's an aurora cluster, the resrouce will start with "cluster"
        if(dbInstanceIdentifier.startsWith("cluster")){
            DescribeDBClustersResult result = amazonRDSClient.describeDBClusters(
                    new DescribeDBClustersRequest().withDBClusterIdentifier(instanceName));
            gatekeeperRDSInstances = loadToGatekeeperRDSInstanceAurora(environment, amazonRDSClient, result.getDBClusters(), securityGroupIds);
        }else {
            DescribeDBInstancesResult result = amazonRDSClient.describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(instanceName));
            gatekeeperRDSInstances = loadToGatekeeperRDSInstance(environment, amazonRDSClient, result.getDBInstances(), securityGroupIds);
        }

        logger.info("Fetched Instance in " + ((double)(System.currentTimeMillis() - startTime) / 1000) + " Seconds");

        Optional<GatekeeperRDSInstance> gatekeeperRDSInstance = Optional.of(gatekeeperRDSInstances.get(0));
        return gatekeeperRDSInstance;
    }

    public Map<RoleType, List<String>> getSchemasForInstance(AWSEnvironment environment, String instanceId, String instanceName) throws Exception{
        logger.info("Getting Schema info for " +instanceName + "(" + instanceId + ") On Account " + environment.getAccount() + " and Region " + environment.getRegion());
        Optional<GatekeeperRDSInstance> instance = getOneInstance(environment, instanceId, instanceName);
        if(instance.isPresent()){
            return databaseConnectionService.getAvailableSchemasForDb(instance.get(), environment);
        }else{
            return unavailableMap();
        }
    }

    @PreAuthorize("@gatekeeperRoleService.isApprover()")
    public List<DbUser> getUsersForInstance(AWSEnvironment environment, String instanceId, String instanceName) throws Exception {
        Optional<GatekeeperRDSInstance> instance = getOneInstance(environment, instanceId, instanceName);

        if(instance.isPresent()){
            return databaseConnectionService.getUsersForDb(instance.get(), environment);
        }else{
            logger.error("Could not find database with " + instanceName + " on account " + environment.getAccount() + "(" + environment.getRegion() + ")");
            return Collections.emptyList();
        }
    }

    private List<GatekeeperRDSInstance> doFilterRds(AWSEnvironment environment, String searchString) {
        // Get all instances that match the given search string
        return loadInstances(environment, instance -> (instance.getDBInstanceIdentifier().toLowerCase().contains(searchString.toLowerCase())
                || instance.getDbiResourceId().toLowerCase().contains(searchString.toLowerCase())) && !instance.getEngine().contains("aurora"));
    }

    private List<GatekeeperRDSInstance> doFilterAurora(AWSEnvironment environment, String searchString) {
        // Get all instances that match the given search string
        return loadInstancesAurora(environment, instance ->
                instance.getDBClusterIdentifier().toLowerCase().contains(searchString.toLowerCase())
                        || instance.getDbClusterResourceId().toLowerCase().contains(searchString.toLowerCase()));
    }

    private String getApplicationTagforInstanceArn(AmazonRDSClient client, String arn){
        ListTagsForResourceRequest request = new ListTagsForResourceRequest();
        Optional<Tag> applicationTag = Optional.ofNullable(client.listTagsForResource(request.withResourceName(arn)).getTagList()
            .stream().filter(tag -> tag.getKey().equalsIgnoreCase(gatekeeperProperties.getAppIdentityTag()))
            .findFirst())
                .orElse(Optional.empty());

        return applicationTag.isPresent() ? applicationTag.get().getValue() : "NONE";
    }
    /**
     * Loads the DB instances from a aws fetch call for RDS databases into a list of Gatekeeper RDS Objects
     * @param instances
     * @return
     */
    private List<GatekeeperRDSInstance> loadToGatekeeperRDSInstance(AWSEnvironment environment, AmazonRDSClient client, List<DBInstance> instances, List<String> securityGroupIds){
        ArrayList<GatekeeperRDSInstance> gatekeeperRDSInstances = new ArrayList<>();

        instances.forEach(item -> {
            String application = getApplicationTagforInstanceArn(client, item.getDBInstanceArn());
            boolean enabled = false;
            String status = item.getDBInstanceStatus();
            Integer port = item.getEndpoint().getPort();
            List<String> availableRoles = null;
            String dbName = item.getDBName();
            String address = getAddress(item.getEndpoint().getAddress(),String.valueOf(port),dbName);
            if(item.getDBInstanceStatus().equalsIgnoreCase(STATUS_AVAILABLE)
                    || item.getDBInstanceStatus().equalsIgnoreCase(STATUS_BACKING_UP)) {
                enabled = item.getVpcSecurityGroups().stream()
                        .anyMatch(sg -> {
                            return securityGroupIds.contains(sg.getVpcSecurityGroupId());
                        });
                if(!enabled){
                    status = STATUS_MISSING_SGS;
                }
                // If the database engine is an oracle based engine then we need to go dig the SSL port out thru the options.
                if(enabled && item.getEngine().contains("oracle")) {
                    // need to get the oracle SSL port from the associated option group
                    logger.info("determining SSL port for Oracle DB " + item.getDBInstanceIdentifier() + " (" + item.getEngine() + ")");
                    List<String> optionGroups = item.getOptionGroupMemberships().stream()
                            .map(OptionGroupMembership::getOptionGroupName)
                            .collect(Collectors.toList());

                    // look through all option groups attached to the DB
                    for(String ogName: optionGroups) {
                        DescribeOptionGroupsRequest describeOptionGroupsRequest = new DescribeOptionGroupsRequest();
                        OptionGroup optionGroup = client.describeOptionGroups(describeOptionGroupsRequest.withOptionGroupName(ogName))
                                .getOptionGroupsList().get(0);

                        // look for the SSL Option
                        final Optional<Option> sslOption = optionGroup.getOptions().stream()
                                .filter(option -> option.getOptionName().equalsIgnoreCase("SSL"))
                                .findFirst();

                        // if the SSL Option is present then set the port and stop searching
                        if(sslOption.isPresent()){
                            port = sslOption.get().getPort();
                            item.getEndpoint().setPort(port); //for oracle need to set the ssl port to be different.
                            break;
                        }
                    }
                    logger.info("The SSL Port for " + item.getDBName() + " is: " + port);
                }

                if(enabled && item.getReadReplicaSourceDBInstanceIdentifier() != null){
                    status = "Unsupported (Read-Only replica of " +item.getReadReplicaSourceDBInstanceIdentifier() + ")";
                    enabled = false;
                }
                if(enabled){
                    //if enabled lets check if the DB is working
                    try {
                        String dbStatus = databaseConnectionService.checkDb(item, environment);
                        status = !dbStatus.isEmpty() ? dbStatus : status;
                        enabled = dbStatus.isEmpty(); // if there's no message back from the DB enabled is still true
                    }catch(GKUnsupportedDBException e){
                        logger.error(STATUS_UNSUPPORTED_DB_ENGINE, e);
                        status = STATUS_UNSUPPORTED_DB_ENGINE;
                        enabled = false;
                    }
                    // if status hasn't changed then get the roles
                    if(enabled && status.equals(item.getDBInstanceStatus())) {
                        // get available roles for DB
                        try {
                            availableRoles = databaseConnectionService.getAvailableRolesForDb(item, environment);
                            Collections.sort(availableRoles);
                            logger.info("Found the following roles on " + item.getDBInstanceIdentifier() + " (" + availableRoles +").");
                        } catch (Exception e) {
                            logger.error(STATUS_COULD_NOT_FETCH_ROLES, e);
                            if(e.getMessage().contains("password")){
                                status = STATUS_UNABLE_TO_LOGIN;
                            }else {
                                status = STATUS_COULD_NOT_FETCH_ROLES;
                            }
                            enabled = false;
                        }
                    }
                }
            }

            gatekeeperRDSInstances.add(new GatekeeperRDSInstance(item.getDbiResourceId(), item.getDBInstanceIdentifier(),
                    dbName != null ? dbName : "", item.getEngine(), status,
                    item.getDBInstanceArn(), item.getEndpoint().getAddress() + ":" + port, application, availableRoles, enabled));
        });

        return gatekeeperRDSInstances;
    }

    /**
     * This looks at database clusters for Aurora, similar to the function loadToGatekeeperRDSInstance() but the difference
     * being we have to look at the cluster as a whole vs the single instance
     */
    private List<GatekeeperRDSInstance> loadToGatekeeperRDSInstanceAurora(AWSEnvironment environment, AmazonRDSClient client, List<DBCluster> instances, List<String> securityGroupIds){
        ArrayList<GatekeeperRDSInstance> gatekeeperRDSInstances = new ArrayList<>();

        instances.forEach(item -> {
            // Only concerned with Aurora clusters apparently AWS lumps in docdb and neptune, etc clusters with the call on the RDS API
            // if the engine is not aurora then skip it.
            if(!item.getEngine().contains("aurora")){
                return;
            }

            String application = getApplicationTagforInstanceArn(client, item.getDBClusterArn());
            boolean enabled = false;
            String status = item.getStatus();
            Integer port = item.getPort();
            List<String> availableRoles = null;
            String dbName = item.getDatabaseName();

            if(item.getStatus().equalsIgnoreCase(STATUS_AVAILABLE)
                    || item.getStatus().equalsIgnoreCase(STATUS_BACKING_UP)) {
                enabled = item.getVpcSecurityGroups().stream()
                        .anyMatch(sg -> {
                            return securityGroupIds.contains(sg.getVpcSecurityGroupId());
                        });

                if(!enabled){
                    status = STATUS_MISSING_SGS;
                }

                if(enabled && item.getReplicationSourceIdentifier() != null){
                    status = "Unsupported (Read-Only replica of " + item.getReplicationSourceIdentifier() + ")";
                    enabled = false;
                }

                //if the cluster does not have any instances associated then it won't be able to work with Gatekeeper
                if(enabled && item.getDBClusterMembers().size() < 1){
                    status = STATUS_NO_INSTANCES;
                    enabled = false;
                } else if(enabled && item.getDBClusterMembers().stream().noneMatch(DBClusterMember::isClusterWriter)){
                    //if the cluster has no writer instances associated with it. Gatekeeper cannot create the user
                    status = STATUS_NO_WRITERS;
                    enabled = false;
                }

                if(enabled) {
                    try {
                        String dbStatus = databaseConnectionService.checkDb(item, environment);
                        status = !dbStatus.isEmpty() ? dbStatus : status;
                        enabled = dbStatus.isEmpty(); // if there's no message back from the DB enabled is still true
                    }catch(GKUnsupportedDBException e){
                        logger.error(STATUS_UNSUPPORTED_DB_ENGINE, e);
                        status = STATUS_UNSUPPORTED_DB_ENGINE;
                        enabled = false;
                    }

                    if(enabled){
                        // get available roles for DB
                        try {
                            availableRoles = databaseConnectionService.getAvailableRolesForDb(item, environment);
                            Collections.sort(availableRoles);
                            logger.info("Found the following roles on " + item.getDBClusterIdentifier() + " (" + availableRoles +").");
                        } catch (Exception e) {
                            logger.error(STATUS_COULD_NOT_FETCH_ROLES, e);
                            if (e.getMessage().contains("password")) {
                                status = STATUS_UNABLE_TO_LOGIN;
                            } else {
                                status = STATUS_COULD_NOT_FETCH_ROLES;
                            }
                            enabled = false;
                        }
                    }
                }
            }
            gatekeeperRDSInstances.add(new GatekeeperRDSInstance(item.getDbClusterResourceId(), item.getDBClusterIdentifier(),
                    dbName, item.getEngine(), status, item.getDBClusterArn(), item.getEndpoint() + ":" + port, application, availableRoles, enabled));
        });

        return gatekeeperRDSInstances;
    }
    private String getAddress(String address, String port, String dbName){
        return String.format("%s:%s/%s", address, port, dbName);
    }

    private List<GatekeeperRDSInstance> loadInstances(AWSEnvironment environment, Predicate<? super DBInstance> filter) {
        logger.info("Refreshing RDS Instance Data");
        long startTime = System.currentTimeMillis();
        DescribeDBInstancesRequest describeDBInstancesRequest = new DescribeDBInstancesRequest();
        List<String> securityGroupIds = sgLookupService.fetchSgsForAccountRegion(environment);
        AmazonRDSClient amazonRDSClient = awsSessionService.getRDSSession(environment);
        DescribeDBInstancesResult result = amazonRDSClient.describeDBInstances(describeDBInstancesRequest);

        List<GatekeeperRDSInstance> gatekeeperRDSInstances = loadToGatekeeperRDSInstance(environment, amazonRDSClient,
                result.getDBInstances()
                        .stream()
                        .filter(filter)
                        .collect(Collectors.toList()), securityGroupIds);

        //At a certain point (Usually ~100 instances) amazon starts paging the rds results, so we need to get each page, which is keyed off by a marker.
        while(result.getMarker() != null) {
            result = amazonRDSClient.describeDBInstances(describeDBInstancesRequest.withMarker(result.getMarker()));
            gatekeeperRDSInstances.addAll(loadToGatekeeperRDSInstance(environment, amazonRDSClient,
                    result.getDBInstances()
                            .stream()
                            .filter(filter)
                            .collect(Collectors.toList()), securityGroupIds));
        }
        logger.info("Refreshed instance data in " + ((double)(System.currentTimeMillis() - startTime) / 1000) + " Seconds");

        return gatekeeperRDSInstances;
    }

    private List<GatekeeperRDSInstance> loadInstancesAurora(AWSEnvironment environment, Predicate<? super DBCluster> filter) {
        logger.info("Looking up Aurora Clusters");
        Long startTime = System.currentTimeMillis();
        DescribeDBClustersRequest describeDBClustersRequest = new DescribeDBClustersRequest();
        List<String> securityGroupIds = sgLookupService.fetchSgsForAccountRegion(environment);
        AmazonRDSClient amazonRDSClient = awsSessionService.getRDSSession(environment);
        DescribeDBClustersResult result = amazonRDSClient.describeDBClusters(describeDBClustersRequest);

        List<GatekeeperRDSInstance> gatekeeperRDSInstances = loadToGatekeeperRDSInstanceAurora(environment,amazonRDSClient,
                result.getDBClusters()
                        .stream()
                        .filter(filter)
                        .collect(Collectors.toList()), securityGroupIds);

        //At a certain point (Usually ~100 instances) amazon starts paging the rds results, so we need to get each page, which is keyed off by a marker.
        while(result.getMarker() != null) {
            result = amazonRDSClient.describeDBClusters(describeDBClustersRequest.withMarker(result.getMarker()));
            gatekeeperRDSInstances.addAll(loadToGatekeeperRDSInstanceAurora(environment, amazonRDSClient,
                    result.getDBClusters()
                            .stream()
                            .filter(filter)
                            .collect(Collectors.toList()), securityGroupIds));
        }
        logger.info("Refreshed instance data in " + ((double)(System.currentTimeMillis() - startTime) / 1000) + " Seconds");

        return gatekeeperRDSInstances;
    }

    private Map<RoleType, List<String>> unavailableMap(){
        HashMap<RoleType, List<String>> empty = new HashMap<>();
        for(RoleType r : RoleType.values()){
            empty.put(r, Collections.singletonList("Unable to get available schemas"));
        }

        return empty;
    }
}
