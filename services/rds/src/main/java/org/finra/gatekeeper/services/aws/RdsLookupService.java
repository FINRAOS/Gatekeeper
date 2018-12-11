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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.model.DbUser;
import org.finra.gatekeeper.rds.model.RoleType;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperRDSInstance;
import org.finra.gatekeeper.services.db.DatabaseConnectionService;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service that handles interfacing with the AWS for everything RDS related
 */
@Component
public class RdsLookupService {

    private final Logger logger = LoggerFactory.getLogger(RdsLookupService.class);

    private final AwsSessionService awsSessionService;
    private final DatabaseConnectionService databaseConnectionService;
    private final SGLookupService sgLookupService;
    private final GatekeeperProperties gatekeeperProperties;

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

    /* Account_Region -> Instances Cache, Want to minimize impact on AWS calls.*/
    private final LoadingCache<AWSEnvironment, List<GatekeeperRDSInstance>> rdsInstanceCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .concurrencyLevel(10)
            .refreshAfterWrite(15, TimeUnit.MINUTES)
            .build(new CacheLoader<AWSEnvironment, List<GatekeeperRDSInstance>>() {
                @Override
                public List<GatekeeperRDSInstance> load(AWSEnvironment environment) throws Exception {
                    return loadInstances(environment);
                }
            });

    private List<GatekeeperRDSInstance> doFilter(AWSEnvironment environment, Predicate<? super GatekeeperRDSInstance> filter) {
        List<GatekeeperRDSInstance> result = rdsInstanceCache.getUnchecked(environment)
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
        return result;
    }

    public List<GatekeeperRDSInstance> getInstances(AWSEnvironment environment, String searchString) {
        return doFilter(environment, instance -> instance.getInstanceId().toLowerCase().contains(searchString.toLowerCase())
                || instance.getName().toLowerCase().contains(searchString.toLowerCase()));
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
    private List<GatekeeperRDSInstance> loadToGatekeeperRDSInstance(AmazonRDSClient client, List<DBInstance> instances, List<String> securityGroupIds){
        ArrayList<GatekeeperRDSInstance> gatekeeperRDSInstances = new ArrayList<>();

        instances.forEach(item -> {
            String application = getApplicationTagforInstanceArn(client, item.getDBInstanceArn());
            if(item.getDBInstanceStatus().equalsIgnoreCase("available")) {
                Boolean enabled = item.getVpcSecurityGroups().stream()
                        .anyMatch(sg -> {
                            return securityGroupIds.contains(sg.getVpcSecurityGroupId());
                        });

                String status = item.getDBInstanceStatus();
                String dbName = item.getDBName();
                List<String> availableRoles = null;
                if(dbName ==null && item.getEngine().equalsIgnoreCase("postgres")){
                    dbName = item.getEngine().toLowerCase();
                }

                if(!enabled){
                    status = "Missing FINRA-RDS-support Security Group";
                }else{
                    //if enabled lets check if the DB is working

                    try {

                        String dbStatus = databaseConnectionService.checkDb(item.getEngine(), getAddress(item.getEndpoint().getAddress(),String.valueOf(item.getEndpoint().getPort()),dbName));
                        status = !dbStatus.isEmpty() ? dbStatus : status;
                    }catch(GKUnsupportedDBException e){
                        logger.error("Database Engine is not supported", e);
                        status = "DB Engine not supported";
                    }

                    // get available roles for DB
                    try {
                        availableRoles = databaseConnectionService.getAvailableRolesForDb(item.getEngine(), getAddress(item.getEndpoint().getAddress(),String.valueOf(item.getEndpoint().getPort()),dbName));
                        Collections.sort(availableRoles);
                    }catch(Exception e){
                        logger.error("Could not fetch roles available to DB", e);
                        status = "Could not fetch roles available to DB";
                    }
                }

                gatekeeperRDSInstances.add(new GatekeeperRDSInstance(item.getDbiResourceId(), item.getDBInstanceIdentifier(),
                        dbName != null ? dbName : "", item.getEngine(), status,
                        item.getDBInstanceArn(), item.getEndpoint().getAddress() + ":" + item.getEndpoint().getPort(), application, availableRoles, enabled));
            }
        });

        return gatekeeperRDSInstances;
    }

    private String getAddress(String address, String port, String dbName){
        return String.format("%s:%s/%s", address, port, dbName);
    }

    private List<GatekeeperRDSInstance> loadInstances(AWSEnvironment environment) {
        logger.info("Refreshing RDS Instance Data");
        Long startTime = System.currentTimeMillis();
        DescribeDBInstancesRequest describeDBInstancesRequest = new DescribeDBInstancesRequest();
        List<String> securityGroupIds = sgLookupService.fetchSgsForAccountRegion(environment);
        AmazonRDSClient amazonRDSClient = awsSessionService.getRDSSession(environment);
        DescribeDBInstancesResult result = amazonRDSClient.describeDBInstances(describeDBInstancesRequest);

        List<GatekeeperRDSInstance> gatekeeperRDSInstances = loadToGatekeeperRDSInstance(amazonRDSClient, result.getDBInstances(), securityGroupIds);

        //At a certain point (Usually ~100 instances) amazon starts paging the rds results, so we need to get each page, which is keyed off by a marker.
        while(result.getMarker() != null) {
            result = amazonRDSClient.describeDBInstances(describeDBInstancesRequest.withMarker(result.getMarker()));
            gatekeeperRDSInstances.addAll(loadToGatekeeperRDSInstance(amazonRDSClient, result.getDBInstances(), securityGroupIds));
        }
        logger.info("Refreshed instance data in " + ((double)(System.currentTimeMillis() - startTime) / 1000) + " Seconds");

        return gatekeeperRDSInstances;
    }

    public Optional<GatekeeperRDSInstance> getOneInstance(AWSEnvironment environment, String dbInstanceIdentifier) {
        Long startTime = System.currentTimeMillis();
        DescribeDBInstancesRequest describeDBInstancesRequest = new DescribeDBInstancesRequest();
        List<String> securityGroupIds = sgLookupService.fetchSgsForAccountRegion(environment);
        AmazonRDSClient amazonRDSClient = awsSessionService.getRDSSession(environment);
        DescribeDBInstancesResult result = amazonRDSClient.describeDBInstances(describeDBInstancesRequest.withDBInstanceIdentifier(dbInstanceIdentifier));

        List<GatekeeperRDSInstance> gatekeeperRDSInstances = loadToGatekeeperRDSInstance(amazonRDSClient, result.getDBInstances(), securityGroupIds);

        logger.info("Fetched Instance in " + ((double)(System.currentTimeMillis() - startTime) / 1000) + " Seconds");

        Optional<GatekeeperRDSInstance> gatekeeperRDSInstance = Optional.of(gatekeeperRDSInstances.get(0));
        return gatekeeperRDSInstance;
    }

    private Filter createAwsFilter(String filterKey, Collection<String> vals){
        Filter filter = new Filter();
        filter.setName(filterKey);
        filter.setValues(vals);
        return filter;
    }

    public Map<RoleType, List<String>> getSchemasForInstance(AWSEnvironment environment, String instanceId) throws Exception{
        Optional<GatekeeperRDSInstance> instance = getInstance(environment, instanceId);
        if(instance.isPresent()){
            return databaseConnectionService.getAvailableSchemasForDb(instance.get());
        }else{
            return unavailableMap();
        }
    }

    @PreAuthorize("@gatekeeperRoleService.isApprover()")
    public List<DbUser> getUsersForInstance(AWSEnvironment environment, String instanceName) throws Exception {
        Optional<GatekeeperRDSInstance> instance = getOneInstance(environment, instanceName);

        if(instance.isPresent()){
            return databaseConnectionService.getUsersForDb(instance.get());
        }else{
            logger.error("Could not find database with " + instanceName + " on account " + environment.getAccount() + "(" + environment.getRegion() + ")");
            return Collections.emptyList();
        }
    }

    private Map<RoleType, List<String>> unavailableMap(){
        HashMap<RoleType, List<String>> empty = new HashMap<>();
        for(RoleType r : RoleType.values()){
            empty.put(r, Collections.singletonList("Unable to get available schemas"));
        }

        return empty;
    }

    private Optional<GatekeeperRDSInstance> getInstance(AWSEnvironment environment, String instanceId){
        // get it from the cache, it should be there!
        return rdsInstanceCache.getUnchecked(environment).stream()
                .filter(db -> db.getInstanceId().equals(instanceId))
                .findFirst();
    }
}
