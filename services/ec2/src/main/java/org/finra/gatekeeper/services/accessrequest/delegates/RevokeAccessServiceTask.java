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

package org.finra.gatekeeper.services.accessrequest.delegates;

import org.activiti.engine.ManagementService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.Job;
import org.finra.gatekeeper.services.accessrequest.AccessRequestService;
import org.finra.gatekeeper.services.accessrequest.model.messaging.enums.EventType;
import org.finra.gatekeeper.services.aws.Ec2LookupService;
import org.finra.gatekeeper.services.aws.SnsService;
import org.finra.gatekeeper.services.aws.SsmService;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.accessrequest.model.AWSInstance;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.email.wrappers.EmailServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Service task to grant access using the details from an AccessRequest
 */
@Component
public class RevokeAccessServiceTask implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(RevokeAccessServiceTask.class);

    private final EmailServiceWrapper emailServiceWrapper;
    private final SsmService ssmService;
    private final SnsService snsService;
    private final ManagementService managementService;
    private final Ec2LookupService ec2LookupService;
    private final AccessRequestService accessRequestService;

    @Autowired
    public RevokeAccessServiceTask(EmailServiceWrapper emailServiceWrapper,
                                   SsmService ssmService,
                                   SnsService snsService,
                                   ManagementService managementService,
                                   Ec2LookupService ec2LookupService,
                                   AccessRequestService accessRequestService){
        this.emailServiceWrapper = emailServiceWrapper;
        this.ssmService = ssmService;
        this.snsService = snsService;
        this.managementService = managementService;
        this.ec2LookupService = ec2LookupService;
        this.accessRequestService = accessRequestService;
    }

    /**
     * Removes the user from the instance once the window they requested for is up.
     *
     * @param execution
     * @throws Exception
     */
    public void execute(DelegateExecution execution) throws Exception{
        Job job = managementService.createJobQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
        AccessRequest accessRequest = (AccessRequest)execution.getVariable("accessRequest");
        try {
            logger.info("Revoking access for Users, Attempts remaining: " + job.getRetries());
            AWSEnvironment env = new AWSEnvironment(accessRequest.getAccount(), accessRequest.getRegion());

            List<String> instanceIds = accessRequest.getInstances().stream().map(instance -> instance.getInstanceId()).collect(Collectors.toList());

            Map<String, String> instancesWithStatus = ssmService.checkInstancesWithSsm(env, instanceIds);

            List<String> onlineInstances = instancesWithStatus.entrySet().stream()
                    .filter(map -> map.getValue().equals("Online"))
                    .map(map -> map.getKey())
                    .collect(Collectors.toList());

            //any non-online reported instances in SSM or any instances not represented by SSM in general.
            List<AWSInstance> manualRemovalInstances = accessRequest.getInstances().stream()
                    .filter(instance -> !instancesWithStatus.containsKey(instance.getInstanceId()) || !instancesWithStatus.get(instance.getInstanceId()).equals("Online"))
                    .collect(Collectors.toList());

            //if there were "offline instances", we will double check to make sure they are not terminated
            List<String> offlineIds = manualRemovalInstances.stream()
                    .map(AWSInstance::getInstanceId)
                    .collect(Collectors.toList());

            Map<String, Boolean> instancesThatStillExist = ec2LookupService.checkIfInstancesExistOrTerminated(env, offlineIds);

            //any instance that comes up false in instancesThatStillExists is still around in AWS and needs a manual revocation.
            manualRemovalInstances = manualRemovalInstances.stream()
                    .filter(instance -> !instancesThatStillExist.get(instance.getInstanceId()))
                    .collect(Collectors.toList());

            //only revoke if there are instances reporting online to SSM
            if(onlineInstances.size() > 0) {
                accessRequest.getUsers().forEach(user -> {
                    ssmService.deleteUserAccount(env, onlineInstances, user.getUserId(), accessRequest.getPlatform());
                });
            }

            if(manualRemovalInstances.size() > 0){
                logger.info("Could not revoke access for " + manualRemovalInstances + " send a notification to the ops team to investigate these instances");
                emailServiceWrapper.notifyOps(accessRequest, manualRemovalInstances);
            }

            try {
                // If an SNS topic is provided run the queries, otherwise lets skip this step.
                if(snsService.isTopicSet()) {
                    snsService.pushToSNSTopic(accessRequestService.getLiveRequestsForUsersInRequest(EventType.EXPIRATION, accessRequest));
                } else {
                    logger.info("Skip querying of live request data as SNS topic ARN was not provided");
                }
            } catch (Exception e) {
                e.printStackTrace();
                emailServiceWrapper.notifyAdminsOfFailure(accessRequest, e);
                logger.error("Error pushing to SNS topic upon request expiration. Request ID: " + accessRequest.getId());
            }

        }catch(Exception e){
            //Since we avoid bad SSM configurations, this code generally only gets called if there's an exception because of something in our code.
            //if this happens, we'll be notified us as well as the ops teams in case our maximum retry limit is hit. This should be
            //super unlikely in the current state of the code.
            if(job.getRetries() - 1 == 0){
                logger.error("Maximum attempt limit reached. Notify Ops team for manual removal");
                emailServiceWrapper.notifyOps(accessRequest);
                emailServiceWrapper.notifyAdminsOfFailure(accessRequest,e);
            }else{
                throw e;
            }
        }
    }
}
