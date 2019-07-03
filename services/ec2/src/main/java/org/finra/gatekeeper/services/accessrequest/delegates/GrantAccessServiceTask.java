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

import com.amazonaws.services.simplesystemsmanagement.model.CommandStatus;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.aws.SnsService;
import org.finra.gatekeeper.services.accessrequest.model.messaging.enums.EventType;
import org.finra.gatekeeper.services.aws.SsmService;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.accessrequest.AccessRequestService;
import org.finra.gatekeeper.services.accessrequest.model.*;
import org.finra.gatekeeper.services.email.model.GatekeeperLinuxNotification;
import org.finra.gatekeeper.services.email.model.GatekeeperWindowsNotification;
import org.finra.gatekeeper.services.email.wrappers.EmailServiceWrapper;
import org.finra.gatekeeper.services.keypairs.KeypairService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Service task to grant access using the details from an AccessRequest
 */
@Component
public class GrantAccessServiceTask implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(GrantAccessServiceTask.class);

    private final SsmService ssmService;
    private final KeypairService keypairService;
    private final EmailServiceWrapper emailServiceWrapper;
    private final AccessRequestService accessRequestService;
    private final SnsService snsService;

    @Autowired
    public GrantAccessServiceTask(SsmService ssmService,
                                  SnsService snsService,
                                  KeypairService keypairService,
                                  EmailServiceWrapper emailServiceWrapper,
                                  AccessRequestService accessRequestService){
        this.ssmService = ssmService;
        this.keypairService = keypairService;
        this.emailServiceWrapper = emailServiceWrapper;
        this.accessRequestService = accessRequestService;
        this.snsService = snsService;
    }

    /**
     * This makes the calls (keypair, ssm, and email) for granting access.
     *
     * @param execution the Activiti object
     * @throws Exception for anything that goes wrong
     */
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.getVariable("attempts") == null) {
            execution.setVariable("attempts", 1);
        } else {
            execution.setVariable("attempts", (Integer) execution.getVariable("attempts") + 1);
        }

        AccessRequest accessRequest = accessRequestService.updateInstanceStatus((AccessRequest) execution.getVariable("accessRequest"));
        logger.info("Granting Access to " + accessRequest);
        try {

            // Prepare parameters
            AWSEnvironment env = new AWSEnvironment(accessRequest.getAccount(), accessRequest.getRegion());
            logger.info("Environment for this access request is " + env.getAccount() + " ( " + env.getRegion() + " )");
            List<String> instances = accessRequest.getInstances()
                    .stream().filter(instance -> instance.getStatus().equals("Online"))
                    .map(AWSInstance::getInstanceId)
                    .collect(Collectors.toList());

            // Do all of this for each user in the request
            if(instances.size()>0) {
                List<GatekeeperLinuxNotification> linuxNotifications = new ArrayList<>();
                List<GatekeeperWindowsNotification> windowsNotifications = new ArrayList<>();

                String platform = accessRequest.getPlatform();
                switch(platform){
                    case "Linux":
                        linuxNotifications = createLinuxUser(accessRequest, env, instances, platform);
                        break;
                    case "Windows":
                        windowsNotifications = createWindowsUser(accessRequest, env, instances, platform);
                    break;
                    default:
                        throw new GatekeeperException("Unsupported platform " + platform);
                }

                // If an SNS topic is provided run the queries, otherwise lets skip this step.
                if(snsService.isTopicSet()) {
                    snsService.pushToSNSTopic(accessRequestService.getLiveRequestsForUsersInRequest(EventType.APPROVAL, accessRequest));
                } else {
                    logger.info("Skip querying of live request data as SNS topic ARN was not provided");
                }

                linuxNotifications.forEach(notification -> emailServiceWrapper.notifyOfCredentials(accessRequest, notification)); // pass along the key to the user(s)
                windowsNotifications.forEach(notification -> emailServiceWrapper.notifyOfCancellation(accessRequest, notification)); // pass along any cancelled executions to the user(s)
            }


        } catch (Exception e) {
            emailServiceWrapper.notifyAdminsOfFailure(accessRequest, e);
            execution.setVariable("requestStatus", RequestStatus.APPROVAL_ERROR);
            throw e;
        }

        if (execution.getVariable("requestStatus") == null) {
            execution.setVariable("requestStatus", RequestStatus.GRANTED);
        }
    }

    /**
     * Grants access to users on Linux instances. Creates a key pair, sends private to user and public key off
     * to SSM along with user and instance
     * @param accessRequest The access request being handled. Contains the user info
     * @param env The environment object used by the ssm document for client creation
     * @param instances the instances that the user is being created on
     * @param platform - used to determine document, should always be Linux on this call
     * @throws GatekeeperException
     */
    private List<GatekeeperLinuxNotification> createLinuxUser(AccessRequest accessRequest, AWSEnvironment env, List<String> instances, String platform) throws GatekeeperException{
        Map<String, Boolean> userStatus = new HashMap<>();
        List<GatekeeperLinuxNotification> notifications = new ArrayList<>();
        for (User u : accessRequest.getUsers()) {
            // Generate keypair
            KeyPair kp = keypairService.createKeypair();
            if (kp == null) {
                throw new GatekeeperException("Could not generate Keypair");
            }

            // Form public and private key strings
            PublicKey publicKey = kp.getPublic();
            String publicKeyString = keypairService.getPublicKeyString(publicKey);
            if (publicKeyString == null) {
                throw new GatekeeperException("Could not encode public key");
            }
            PrivateKey privKey = kp.getPrivate();
            String privateKeyString = keypairService.getPEM(privKey);

            // Call SSM to create account (one call does all instances)
            Map<String, String> createStatus = ssmService.createUserAccount(env, instances, u.getUserId(), publicKeyString, platform, accessRequest.getHours().toString());
            userStatus.put(u.getName(), createStatus.containsValue(CommandStatus.Success.toString()));

            // Send email with private key
            if(userStatus.get(u.getName())) {
                notifications.add(new GatekeeperLinuxNotification()
                    .setUser(u)
                    .setKey(privateKeyString)
                    .setCreateStatus(createStatus));
            }
        }

        if(!userStatus.containsValue(true)){
            throw new GatekeeperException("Could not create user account on one or more "+ platform +" instances");
        }

        return notifications;

    }

    /**
     * Grants access to users on Windows instances. Just calls SSM since passwords are generated by the SSM document.
     * @param accessRequest The access request being handled. Contains the user info
     * @param env The environment object used by the ssm document for client creation
     * @param instances the instances that the user is being created on
     * @param platform - used to determine document, should always be Windows on this call
     * @throws GatekeeperException
     */
    private List<GatekeeperWindowsNotification> createWindowsUser(AccessRequest accessRequest, AWSEnvironment env, List<String> instances, String platform) throws GatekeeperException{
        Map<String, Boolean> userStatus = new HashMap<>();
        
        Map<User, List<String>> cancellations = new HashMap<>();
        for (User user : accessRequest.getUsers()) {
            // Call SSM to create account (one call does all instances)
            Map<String, String> createStatus =  ssmService.createUserAccount(env, instances, user, accessRequest, platform);
            userStatus.put(user.getName(), createStatus.containsValue(CommandStatus.Success.toString()));
            if(createStatus.containsValue(CommandStatus.Cancelled.toString())){
                List<String> cancelledInstances = new ArrayList<>();
                for(String instance : createStatus.keySet()){
                    if(createStatus.get(instance).equals(CommandStatus.Cancelled.toString())){
                        cancelledInstances.add(instance);
                    }
                }    
                cancellations.put(user,cancelledInstances);
            }
        }

        if(!userStatus.containsValue(true)){
            throw new GatekeeperException("Could not create user account on one or more "+ platform +" instances");
        }

        return cancellations.keySet()
                .stream()
                .map(user -> new GatekeeperWindowsNotification()
                    .setAccessRequest(accessRequest)
                    .setUser(user)
                    .setCancelledInstances(cancellations.get(user)))
                .collect(Collectors.toList());

    }
}
