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

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This service just fetches a specific Security Group specified under "app.requiredSecurityGroup" in application.yml
 * it will be used to communicate to the UI whether to lock out a DB from selection or not
 */
@Component
public class SGLookupService {

    private final Logger logger = LoggerFactory.getLogger(SGLookupService.class);

    private final String securityGroupNames;

    private AwsSessionService awsSessionService;

    @Autowired
    public SGLookupService(AwsSessionService awsSessionService,
                           GatekeeperProperties gatekeeperProperties){
        this.awsSessionService = awsSessionService;
        this.securityGroupNames = gatekeeperProperties.getRequiredSecurityGroups();
    }

    /*
     * Security Group IDs in practice should NEVER change (will be impossible to delete them when in use.) So the service
     * will determine the id's of the provided SG in config for each environment and just cache them. We could just map the
     * SGs in the properties file sure, but that would mean re-deployment every time we add in a new environment, which is annoying.
     *
     * in the absurd case that a security group does get deleted and re-created, then just bounce this service.
     *
     * Account -> Security Group ID Cache, Want to minimize impact on AWS calls. For multiple VPC's there could be multiple with the same name
     * so we'll store the id's in a list. This should account for any potential peerings that have been set up
     */

    private final LoadingCache<AWSEnvironment, List<String>> environmentSgIdCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .concurrencyLevel(10)
            .refreshAfterWrite(9999, TimeUnit.DAYS)
            .build(new CacheLoader<AWSEnvironment, List<String>>() {
                @Override
                public List<String> load(AWSEnvironment environment) throws Exception {
                    return loadSgsForAccountRegion(environment);
                }
            });


    private List<String> loadSgsForAccountRegion(AWSEnvironment environment) {
        logger.info("Grabbing SGs for environment " + environment);
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();

        Filter groupNameFilter = new Filter();
        groupNameFilter.setName("group-name");
        groupNameFilter.setValues(Arrays.asList(securityGroupNames.split(",")));

        AmazonEC2Client amazonEC2Client = awsSessionService.getEC2Session(environment);
        DescribeSecurityGroupsResult result = amazonEC2Client.describeSecurityGroups(describeSecurityGroupsRequest.withFilters(groupNameFilter));

        logger.info("found " + result.getSecurityGroups().size() + " Security Groups with name(s) '" + securityGroupNames + "'");
        return result.getSecurityGroups().stream()
                .map(SecurityGroup::getGroupId)
                .collect(Collectors.toList());

    }

    public List<String> fetchSgsForAccountRegion(AWSEnvironment environment){
        return environmentSgIdCache.getUnchecked(environment);
    }
}
