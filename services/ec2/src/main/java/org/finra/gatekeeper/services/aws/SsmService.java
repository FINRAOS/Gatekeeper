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


import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.*;
import com.google.common.collect.Lists;
import org.finra.gatekeeper.configuration.properties.GatekeeperEmailProperties;
import org.finra.gatekeeper.configuration.properties.GatekeeperSsmProperties;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that handles the AWS connection and does lookups
 */
@Component
public class SsmService {

    private final Logger logger = LoggerFactory.getLogger(SsmService.class);

    private final AwsSessionService awsSessionService;
    private final GatekeeperSsmProperties gatekeeperSsmProperties;
    private final GatekeeperEmailProperties gatekeeperEmailProperties;


    @Autowired
    public SsmService(AwsSessionService awsSessionService,
                      GatekeeperSsmProperties gatekeeperSsmProperties,
                      GatekeeperEmailProperties gatekeeperEmailProperties){
        this.awsSessionService = awsSessionService;
        this.gatekeeperSsmProperties = gatekeeperSsmProperties;
        this.gatekeeperEmailProperties = gatekeeperEmailProperties;
    }


    public Map<String, String> checkInstancesWithSsm(AWSEnvironment environment, List<String> instanceIds){
        Map<String,String> instanceStatuses = new HashMap<>();

        AWSSimpleSystemsManagement ssmClient = awsSessionService.getSsmSession(environment);
        for(int i = 0; i < instanceIds.size(); i+=50){
            //since we're partitioning 50 at a time we need to make sure that we don't go over the index of the actual size of the set itself
            //if we do then we will use the upper value of the set.
            Integer upperBound = i+50 < instanceIds.size() ? i+50 : instanceIds.size();

            List<String> partitionedList = instanceIds.subList(i, upperBound);


            DescribeInstanceInformationRequest describeInstanceInformationRequest = new DescribeInstanceInformationRequest();
            InstanceInformationFilter filter = new InstanceInformationFilter();
            filter.setKey("InstanceIds");
            filter.setValueSet(partitionedList);
            List<InstanceInformationFilter> informationFilters = new ArrayList<>();
            informationFilters.add(filter);
            describeInstanceInformationRequest.setInstanceInformationFilterList(informationFilters);
            describeInstanceInformationRequest.setMaxResults(50);

            //make the initial call, SSM Chunks it up so we need to call it with tokens til the string returns empty.
            DescribeInstanceInformationResult describeInstanceInformationResult = ssmClient.describeInstanceInformation(describeInstanceInformationRequest);

            describeInstanceInformationResult.getInstanceInformationList().forEach(instance ->
                    instanceStatuses.put(instance.getInstanceId(), instance.getPingStatus()));

            while(describeInstanceInformationResult.getNextToken() != null) {
                //get the next chunk of results
                describeInstanceInformationResult = ssmClient.describeInstanceInformation(describeInstanceInformationRequest.withNextToken(describeInstanceInformationResult.getNextToken()));
                describeInstanceInformationResult.getInstanceInformationList().forEach(instance ->
                        instanceStatuses.put(instance.getInstanceId(), instance.getPingStatus()));
            }
        }

        return instanceStatuses;
    }

    /**
     * Used for creating Linux users.
     * @param environment Environment used to get instance
     * @param instanceIds Ids of the instances that the user will be added to
     * @param userName the name the user will use to access the instance
     * @param publicKey the public key to be added to the instance. private key will be emailed
     * @param platform the platform used to determine the document to be executed
     * @return True or false based on whether the user was added to all instances
     */
    public Map<String, String> createUserAccount(AWSEnvironment environment, List<String> instanceIds, String userName, String publicKey, String platform, String hours) {
        logger.info("Creating user account: " + userName + " on "+ platform +" instances: " + instanceIds + " on environment: " + environment + " for hours: " + hours);

        GatekeeperSsmProperties.SsmDocument documentProperties = getSsmDocument(platform, "create");

        Map<String, ArrayList<String>> parameters = new HashMap<>();
        parameters.put("hours", Lists.newArrayList(hours));
        parameters.put("userName", Lists.newArrayList(userName));
        parameters.put("publicKey", Lists.newArrayList(publicKey));
        parameters.put("executionTimeout", Lists.newArrayList(Integer.toString(documentProperties.getTimeout())));

        return performSSMExecution(environment, instanceIds, platform, parameters, documentProperties);
    }

    /**
     * Used for creating Windows users. Needs a bit more info than the Linux call due to email being constructed
     * and sent from the instance instead of this service.
     * @param environment Environment used to get instance
     * @param instanceIds Ids of the instances that the user will be added to
     * @param user The user being given temporary access - used for email (user email, user id, user name)
     * @param accessRequest The current request - used for email (account, requested hours)
     * @param platform The platform - used to determine the document to be executed
     * @return True or false based on whether the user was added to all instances
     */
    public Map<String, String> createUserAccount(AWSEnvironment environment, List<String> instanceIds, User user, AccessRequest accessRequest, String platform) {
        logger.info("Creating user account: " + user.getUserId() + " on "+ platform +" instances: " + instanceIds + " on environment: " + environment);

        GatekeeperSsmProperties.SsmDocument documentProperties = getSsmDocument(platform, "create");

        Map<String, ArrayList<String>> parameters = new HashMap<>();
        parameters.put("userId", Lists.newArrayList(user.getUserId()));
        parameters.put("userName", Lists.newArrayList(user.getName()));
        parameters.put("userEmail", Lists.newArrayList(user.getEmail()));
        parameters.put("hours", Lists.newArrayList(Integer.toString(accessRequest.getHours())));
        parameters.put("account", Lists.newArrayList(accessRequest.getAccount()));
        parameters.put("mailFrom", Lists.newArrayList(gatekeeperEmailProperties.getFrom()));
        parameters.put("opsEmail", Lists.newArrayList(gatekeeperEmailProperties.getOpsEmails()));
        parameters.put("accessRequest", Lists.newArrayList(Long.toString(accessRequest.getId())));
        parameters.put("teamEmail", Lists.newArrayList(gatekeeperEmailProperties.getTeam()));
        parameters.put("executionTimeout", Lists.newArrayList(Integer.toString(documentProperties.getTimeout())));

        return performSSMExecution(environment, instanceIds, platform, parameters, documentProperties);
    }


    public Map<String, String> deleteUserAccount(AWSEnvironment environment, List<String> instanceIds, String userName, String platform) {
        logger.info("Deleting user account: " + userName + " on instances: " + instanceIds + " on environment: " + environment);

        GatekeeperSsmProperties.SsmDocument documentProperties = getSsmDocument(platform, "delete");

        ArrayList<String> userNameList = new ArrayList<>();
        userNameList.add(userName);

        Map<String, ArrayList<String>> parameters = new HashMap<>();
        parameters.put("userName", userNameList);

        return executeSsm(environment, instanceIds, platform, parameters, documentProperties);

    }

    private GatekeeperSsmProperties.SsmDocument getSsmDocument(String platform, String document){
        return gatekeeperSsmProperties.getPlatformDocuments(platform).get(document);
    }

    private  Map<String, String> executeSsm(AWSEnvironment environment, List<String> instanceIds, String platform, Map<String, ArrayList<String>> parameters, GatekeeperSsmProperties.SsmDocument documentProperties){
        AWSSimpleSystemsManagement ssmClient = awsSessionService.getSsmSession(environment);

        SendCommandRequest scr = new SendCommandRequest()
                .withInstanceIds(instanceIds)
                .withDocumentName(documentProperties.getDocumentName());


        for(String key : parameters.keySet()){
            scr.addParametersEntry(key, parameters.get(key));
        }

        return waitForSsmCommand(ssmClient,
                ssmClient.sendCommand(scr).getCommand().getCommandId(),
                instanceIds.size(),
                documentProperties.getTimeout(), documentProperties.getWaitInterval());
    }

    private GatekeeperSsmProperties.SsmDocument getSsmDocumentName(String platform, String key){
        Map<String, GatekeeperSsmProperties.SsmDocument> platformDocs = gatekeeperSsmProperties.getPlatformDocuments(platform);
        return platformDocs.get(key);
    }

    private Map<String, String> checkEveryInstance(ListCommandInvocationsResult result) {
        Map<String, String> resultMapping = new HashMap<>();
        for (CommandInvocation ci : result.getCommandInvocations()) {
            resultMapping.put(ci.getInstanceId(), ci.getStatus());
        }

        return resultMapping;
    }

    private boolean finished(ListCommandInvocationsResult result, int instanceCount) {
        if(result.getCommandInvocations().size() < instanceCount){
            return false;
        }
        for (CommandInvocation ci : result.getCommandInvocations()) {
            logger.info("Document Name: " + ci.getDocumentName() + " Command ID: " + ci.getCommandId() + " Instance Id: " + ci.getInstanceId() + " Status: " + ci.getStatus());
            if((ci.getStatus().equals(CommandStatus.InProgress.toString())
                    || ci.getStatus().equals(CommandStatus.Pending.toString()))){
                return false;
            }
        }
        return true;
    }


    private Map<String, String> waitForSsmCommand(AWSSimpleSystemsManagement ssmClient, String commandId, int instanceCount, int timeout, int interval) {
        ListCommandInvocationsRequest lcir = new ListCommandInvocationsRequest().withCommandId(commandId);
        lcir.setMaxResults(50);
        ListCommandInvocationsResult result = ssmClient.listCommandInvocations(lcir);
        Map<String, String> results = checkEveryInstance(result);
        int waited=0;
        logger.info("Waiting for SSM command with id " + commandId + " to complete.");
        while (waited < instanceCount*timeout
                && !finished(result,instanceCount)) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                continue;
            }
            waited += interval;
            result = ssmClient.listCommandInvocations(lcir);
            results = checkEveryInstance(result);
            logger.info("time waited: " + waited + " timeout: " + instanceCount*timeout);
        }
        /**
         * This cancels any pending commands so that when timeout happens, users won't get added
         * This is mainly important when all instances timeout. there would be no attempt to remove.
         * Need to get result again
         */
        results = cancelPendingExecution(ssmClient, results.keySet(), commandId, results);
        return results;
    }

    private Map<String, String> cancelPendingExecution(AWSSimpleSystemsManagement ssmClient, Set<String> instanceIds, String commandId, Map<String, String> results){
        CancelCommandRequest cancelCommandRequest = new CancelCommandRequest()
                .withInstanceIds(instanceIds)
                .withCommandId(commandId);
        ssmClient.cancelCommand(cancelCommandRequest);
        for(String instance : results.keySet()){
            String status = results.get(instance);
            if(status.equals(CommandStatus.InProgress.toString())
                    || status.equals(CommandStatus.Pending.toString())
                    || status.equals(CommandStatus.Cancelling.toString())){
                results.put(instance, CommandStatus.Cancelled.toString());
            }
            
        }
        return results;
    }

    private Map<String, String> performSSMExecution(AWSEnvironment environment, List<String> instanceIds, String platform, Map<String, ArrayList<String>> parameters, GatekeeperSsmProperties.SsmDocument documentProperties){
        Map<String, String> result = executeSsm(environment, instanceIds, platform, parameters, documentProperties);
        int retries = gatekeeperSsmProperties.getSsmGrantRetryCount();
        while(retries-- > 0){
            logger.info("Look for instances that failed to retry. There are " + retries + " retries left");
            // get all the instance ID's that have a failed CommandStatus, anything that timed out will be ignored because there are network problems.
            List<String> instancesToRetry = result.entrySet().stream()
                    .filter(instanceResult -> instanceResult.getValue().equals(CommandStatus.Failed.toString()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if(instancesToRetry.isEmpty()){
                logger.info("No instances need to be retried. Proceeding");
                break;
            }
            logger.info("There are " + instancesToRetry.size() + " Instances to retry (" + instancesToRetry.toString() + ")");
            //re-execute for the failed instances
            result.putAll(executeSsm(environment, instancesToRetry, platform, parameters, documentProperties));
        }
        return result;
    }
}
