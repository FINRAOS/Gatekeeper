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
 */

package org.finra.gatekeeper.services.aws;

import com.amazonaws.services.ec2.model.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.finra.gatekeeper.configuration.properties.GatekeeperEC2Properties;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperAWSInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service that handles the AWS connection and does lookups
 */
@Component
public class Ec2LookupService {
    private final Logger logger = LoggerFactory.getLogger(Ec2LookupService.class);

    private final SsmService ssmService;
    private final AwsSessionService awsSessionService;
    private final GatekeeperEC2Properties gatekeeperEc2Properties;

    /*

    SM: see http://docs.aws.amazon.com/AWSEC2/latest/APIReference/API_DescribeInstances.html for more about this value ('instance-state-code')

    The state of the instance, as a 16-bit unsigned integer. The high byte is an opaque internal value and should be ignored. The low byte is set based on the state represented.
    The valid values are:
    0 (pending),
    16 (running),
    32 (shutting-down),
    48 (terminated),
    64 (stopping),
    80 (stopped).

    */
    private final String INSTANCE_RUNNING = "16";
    private final String SHUTTING_DOWN = "32";
    private final String TERMINATED = "48";
    private final String STOPPING = "64";
    private final String STOPPED = "80";

    private final String APPLICATION_TAG = "APPLICATION";
    private final String INSTANCE_ID_TAG = "INSTANCE ID";
    private final String INSTANCE_NAME_TAG = "NAME";
    private final String INSTANCE_IP = "IP";

    @Autowired
    public Ec2LookupService(SsmService ssmService,
                            AwsSessionService awsSessionService,
                            GatekeeperEC2Properties gatekeeperEc2Properties){
        this.ssmService = ssmService;
        this.awsSessionService = awsSessionService;
        this.gatekeeperEc2Properties = gatekeeperEc2Properties;
    }

    /* Account_Region -> Instances Cache, Want to minimize impact on AWS calls.*/
    private final LoadingCache<AWSEnvironment, List<GatekeeperAWSInstance>> awsInstanceCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .concurrencyLevel(10)
            .refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<AWSEnvironment, List<GatekeeperAWSInstance>>() {
                @Override
                public List<GatekeeperAWSInstance> load(AWSEnvironment environment) throws Exception {
                    return loadInstances(environment);
                }
            });



    private List<GatekeeperAWSInstance> doFilter(AWSEnvironment environment, Predicate<? super GatekeeperAWSInstance> filter) {
        List<GatekeeperAWSInstance> result = awsInstanceCache.getUnchecked(environment)
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
        List<String> instanceIds = result.stream().map(instance -> instance.getInstanceId()).collect(Collectors.toList());
        Map<String,String> instances = ssmService.checkInstancesWithSsm(environment, instanceIds);
        result.forEach(gatekeeperAWSInstance ->
            gatekeeperAWSInstance.setSsmStatus(instances.get(gatekeeperAWSInstance.getInstanceId()) != null ? instances.get(gatekeeperAWSInstance.getInstanceId()) : "Unknown")
        );

        return result;
    }

    public List<GatekeeperAWSInstance> getInstances(AWSEnvironment environment, String platform, String searchLabel, String searchString) {
        Predicate<GatekeeperAWSInstance> filter;
        switch (searchLabel) {
            case APPLICATION_TAG:
                filter = instance -> instance.getApplication().toLowerCase().contains(searchString.toLowerCase());
                break;
            case INSTANCE_ID_TAG:
                filter = instance -> instance.getInstanceId().toLowerCase().contains(searchString.toLowerCase());
                break;
            case INSTANCE_NAME_TAG:
                filter = instance -> instance.getName().toLowerCase().contains(searchString.toLowerCase());
                break;
            case INSTANCE_IP:
                filter = instance -> instance.getIp().toLowerCase().contains(searchString.toLowerCase());
                break;
            default:
                return new ArrayList<>();

        }

        Predicate<GatekeeperAWSInstance> platformFilter = instance -> instance.getPlatform().equals(platform);

        return doFilter(environment, filter.and(platformFilter));
    }

    /**
     * Checks if the supplied instances are still existing or are in a terminated state
     *
     * @param environment - the AWS environment we execute on
     * @param instanceIds - the list of instance id's to search for
     *
     * @return a Map of String -> Boolean where String is the instance ID and boolean is whether the instance is either missing or in terminated state,
     * if the boolean in the mapping is of these values then...
     *
     * TRUE: the instance is terminated
     * FALSE: the instance still exists and is in a non TERMINATED state
     */
    public Map<String, Boolean> checkIfInstancesExistOrTerminated(AWSEnvironment environment, List<String> instanceIds){
        Map<String, Boolean> outcome = instanceIds.stream()
                .collect(Collectors.toMap(Function.identity(), instance -> Boolean.TRUE));
        Filter instanceFilter = createAwsFilter("instance-id", instanceIds);
        Filter runningFilter = createAwsFilter("instance-state-code", Arrays.asList(INSTANCE_RUNNING, STOPPED, STOPPING, SHUTTING_DOWN));

        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();

        //Get any instances that have the specified ID's and are of any status but TERMINATED (because those instances
        //could just be in stopped state or transitioning to stopped so they are technically still around)
        describeInstancesRequest.setFilters(Arrays.asList(instanceFilter, runningFilter));

        //Any instances that came up in the result set will have their values flipped to FALSE in the map.
        awsSessionService.getEC2Session(environment)
                .describeInstances(describeInstancesRequest)
                .getReservations()
                .forEach(reservation -> {
                    reservation.getInstances()
                            .forEach(instance -> {
                                outcome.put(instance.getInstanceId(), Boolean.FALSE);
                            });
                });

        return outcome;
    }

    private String getTagValue(Instance instance, String key) {
        for (Tag tag : instance.getTags()) {
            if (tag.getKey().equals(key)) {
                return tag.getValue() != null ? tag.getValue() : "";
            }
        }
        return "";
    }

    private Filter createAwsFilter(String filterKey, Collection<String> vals){
        Filter filter = new Filter();
        filter.setName(filterKey);
        filter.setValues(vals);
        return filter;
    }

    private List<GatekeeperAWSInstance> loadInstances(AWSEnvironment environment) {
        logger.info("Refreshing Instance Data");
        List<GatekeeperAWSInstance> instanceList = new ArrayList<>();
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        ArrayList<Filter> describeInstanceRequestFilters = new ArrayList<>();
        describeInstanceRequestFilters.add(createAwsFilter("instance-state-code", Arrays.asList(INSTANCE_RUNNING)));
        describeInstancesRequest.setFilters(describeInstanceRequestFilters);

        DescribeInstancesResult result = awsSessionService.getEC2Session(environment).describeInstances(describeInstancesRequest);
        result.getReservations().forEach(reservation -> {
            reservation.getInstances().forEach(instance -> {
                String ipAddress = instance.getPrivateIpAddress() != null ? instance.getPrivateIpAddress() :
                        instance.getPublicIpAddress() != null ? instance.getPublicIpAddress() : "";

                instanceList.add(new GatekeeperAWSInstance(instance.getInstanceId(), getTagValue(instance, gatekeeperEc2Properties.getAppIdentityTag()),
                        getTagValue(instance, "Name"), ipAddress, instance.getPlatform() == null ? "Linux" : StringUtils.capitalize(instance.getPlatform())));
            });
        });

        return instanceList;
    }

}

