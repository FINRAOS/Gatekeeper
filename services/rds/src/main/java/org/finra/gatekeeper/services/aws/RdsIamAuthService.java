/*
 * Copyright 2023. Gatekeeper Contributors
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
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;
import com.amazonaws.services.rds.model.*;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
/**
 * Service that handles interfacing with the AWS for everything RDS IAM Authentication related
 */
@Component
public class RdsIamAuthService {
    private final Logger logger = LoggerFactory.getLogger(RdsIamAuthService.class);

    private final AwsSessionService awsSessionService;

    @Autowired
    public RdsIamAuthService(AwsSessionService awsSessionService) {
        this.awsSessionService = awsSessionService;
    }


    public boolean isRdsIamAuthEnabled(AWSEnvironment environment, String dbInstanceIdentifier, String instanceName, String instanceType) {
        logger.info("Checking to see if RDS IAM Auth is enabled for instance: " + dbInstanceIdentifier);
        Long startTime = System.currentTimeMillis();
        AmazonRDSClient amazonRDSClient = awsSessionService.getRDSSession(environment);

        boolean iamAuth;

        switch (instanceType){
            case "AURORA_GLOBAL": {
                DBCluster primary = getPrimaryClusterForGlobalCluster(environment, instanceName).get();
                DescribeDBClustersRequest request = new DescribeDBClustersRequest().withDBClusterIdentifier(primary.getDBClusterIdentifier());
                DescribeDBClustersResult result = amazonRDSClient.describeDBClusters(request);
                iamAuth =  result.getDBClusters().get(0).getIAMDatabaseAuthenticationEnabled();
                break;
            }
            case "AURORA_REGIONAL": {
                DescribeDBClustersRequest request = new DescribeDBClustersRequest().withDBClusterIdentifier(instanceName);
                DescribeDBClustersResult result = amazonRDSClient.describeDBClusters(request);
                iamAuth =  result.getDBClusters().get(0).getIAMDatabaseAuthenticationEnabled();
                break;
            }
            case "RDS": {
                DescribeDBInstancesResult result = amazonRDSClient.describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(instanceName));
                iamAuth = result.getDBInstances().get(0).getIAMDatabaseAuthenticationEnabled();
                break;
            }
            case "DOCUMENTDB_REGIONAL":
            case "REDSHIFT": {
                iamAuth =  false;
                break;
            }
            default:
                logger.error("Something went wrong when checking IAM Authentication Status of the RDS. Defaulting to false");
                iamAuth =  false;
        }

        logger.info("Fetched Instance in " + ((double)(System.currentTimeMillis() - startTime) / 1000) + " Seconds");
        return iamAuth;
    }
    private Optional<DBCluster> getPrimaryClusterForGlobalCluster(AWSEnvironment environment, String globalClusterId){
        AmazonRDSClient amazonRDSClient = awsSessionService.getRDSSession(environment);
        GlobalCluster theCluster = amazonRDSClient.describeGlobalClusters(
                new DescribeGlobalClustersRequest().withGlobalClusterIdentifier(globalClusterId)
        ).getGlobalClusters().get(0);

        return Optional.of(amazonRDSClient.describeDBClusters(new DescribeDBClustersRequest()
                .withFilters(
                        new Filter()
                                .withName("db-cluster-id")
                                .withValues(getPrimaryCluster(theCluster).getDBClusterArn())))
                .getDBClusters().get(0));
    }

    private GlobalClusterMember getPrimaryCluster(GlobalCluster theCluster) {
        AtomicReference<GlobalClusterMember> theWriterCluster = new AtomicReference<>();
        theCluster.getGlobalClusterMembers().forEach( memberCluster -> {
            if(memberCluster.isWriter()){
                theWriterCluster.set(memberCluster);
            }
        });

        return theWriterCluster.get();
    }

    public String fetchIamAuthToken(AWSEnvironment environment, String host, String port, String username){
        logger.info("Generating RDS IAM Auth Token.");
        RdsIamAuthTokenGenerator generator = awsSessionService.getRdsIamAuthTokenGenerator(environment);
        String authToken = generator.getAuthToken(
                GetIamAuthTokenRequest.builder()
                        .hostname(host)
                        .port(Integer.parseInt(port))
                        .userName(username)
                        .build());
        return authToken;
    }
}
