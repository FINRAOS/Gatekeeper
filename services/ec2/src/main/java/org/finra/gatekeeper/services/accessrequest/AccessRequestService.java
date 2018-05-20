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

package org.finra.gatekeeper.services.accessrequest;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.Task;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.properties.GatekeeperApprovalProperties;
import org.finra.gatekeeper.controllers.AccessRequestController;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.ActiveAccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.CompletedAccessRequestWrapper;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.accessrequest.model.AWSInstance;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequestRepository;
import org.finra.gatekeeper.services.accessrequest.model.RequestStatus;
import org.finra.gatekeeper.services.auth.GatekeeperRole;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.aws.SsmService;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that is used for various activities around the AccessRequest Object
 */

@Component
public class AccessRequestService {

    private static final Logger logger = LoggerFactory.getLogger(AccessRequestController.class);

    private final TaskService taskService;
    private final AccessRequestRepository accessRequestRepository;
    private final GatekeeperRoleService gatekeeperRoleService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final GatekeeperApprovalProperties approvalPolicy;
    private final AccountInformationService accountInformationService;
    private final SsmService ssmService;
    private final String REJECTED = "REJECTED";
    private final String APPROVED = "APPROVED";
    private final String CANCELED = "CANCELED";


    @Autowired
    public AccessRequestService(TaskService taskService,
                                AccessRequestRepository accessRequestRepository,
                                GatekeeperRoleService gatekeeperRoleService,
                                RuntimeService runtimeService,
                                HistoryService historyService,
                                AccountInformationService accountInformationService,
                                SsmService ssmService,
                                GatekeeperApprovalProperties gatekeeperApprovalProperties){

        this.taskService = taskService;
        this.accessRequestRepository = accessRequestRepository;
        this.gatekeeperRoleService = gatekeeperRoleService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.accountInformationService = accountInformationService;
        this.ssmService = ssmService;
        this.approvalPolicy = gatekeeperApprovalProperties;
    }

    public AccessRequest storeAccessRequest(AccessRequestWrapper request) throws GatekeeperException {
        GatekeeperUserEntry requestor = gatekeeperRoleService.getUserProfile();

        //Validating that all instances in the request are the same as the requested platform
        //This also means that all instances have the same platform.
        for(AWSInstance instance : request.getInstances()){
            if(!instance.getPlatform().equals(request.getPlatform())){
                throw new GatekeeperException("Instance platform doesn't match requested platform. Instance: "
                        + instance.getPlatform() + " Requested: " +request.getPlatform());
            }
        }

        //throw gk in front of all the user id's
        request.getUsers().forEach(u -> u.setUserId("gk-" + u.getUserId()));

        AccessRequest accessRequest = new AccessRequest()
                .setAccount(request.getAccount().toUpperCase())
                .setRegion(request.getRegion())
                .setHours(request.getHours())
                .setRequestorId(requestor.getUserId())
                .setRequestorName(requestor.getName())
                .setRequestorEmail(requestor.getEmail())
                .setUsers(request.getUsers())
                .setInstances(request.getInstances())
                .setRequestReason(request.getRequestReason())
                .setPlatform(request.getPlatform());

        logger.info("Storing Access Request");
        accessRequestRepository.save(accessRequest);
        logger.info("Access Request stored with ID: " + accessRequest.getId());

        //Kick off the activiti workflow

        Map<String, Object> variables = new HashMap<>();
        variables.put("accessRequest", accessRequest);
        runtimeService.startProcessInstanceByKey("gatekeeperAccessRequest", variables);

        // Verify that we started a new process instance
        logger.info("Number of process instances: " + runtimeService.createProcessInstanceQuery().count());
        return accessRequest;
    }

    private boolean isRequestorOwnerOfInstances(AccessRequest request) {
        Set<String> memberships = gatekeeperRoleService.getMemberships();
        for (AWSInstance instance : request.getInstances()) {
            if (!memberships.contains(instance.getApplication())) {
                //return false because there exists an instance the requestor doesn't "own"
                return false;
            }
        }
        //if we didn't find any instances the requestor didn't have an Application for we return true
        return true;
    }

    public boolean isApprovalNeeded(AccessRequest request) throws Exception{
        Map<String, Integer> policy = approvalPolicy.getApprovalPolicy(gatekeeperRoleService.getRole());

        //We have to associate the policy to the SDLC of the requested account. The name of the account provided by the ui will not always be "dev" "qa" or "prod", but they will need to associate with those SDLC's
        Account theAccount = accountInformationService.getAccountByAlias(request.getAccount());

        switch(gatekeeperRoleService.getRole()){
            case APPROVER:
                return false;
            case SUPPORT:
                return request.getHours() > policy.get(theAccount.getSdlc().toLowerCase());
            case DEV:
            case OPS:
                return request.getHours() > policy.get(theAccount.getSdlc().toLowerCase()) || !isRequestorOwnerOfInstances(request);
            default:
                //should NEVER happen.
                throw new Exception("Could not determine Role");
        }
    }


    private void handleRequest(String user, String taskId, RequestStatus status) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("requestStatus", status);

        //todo: samAccountName is the approver
        taskService.setAssignee(taskId, user);
        taskService.complete(taskId, vars);
    }



    public List<ActiveAccessRequestWrapper> getActiveRequests() {
        List<Task> tasks = taskService.createTaskQuery().active().list();
        List<ActiveAccessRequestWrapper> response = new ArrayList<>();
        tasks.forEach(task -> {
            AccessRequest theRequest = updateInstanceStatus((AccessRequest) runtimeService.getVariable(task.getExecutionId(), "accessRequest"));

            response.add(new ActiveAccessRequestWrapper(theRequest)
                    .setCreated(task.getCreateTime())
                    .setTaskId(task.getId())
                    .setInstanceCount(theRequest.getInstances().size())
                    .setUserCount(theRequest.getUsers().size()));
        });

        return (List<ActiveAccessRequestWrapper>)filterResults(response);
    }

    public List<CompletedAccessRequestWrapper> getCompletedRequests() {
        //We can use the variables stored on the activiti requests to build out our request history.
        List<HistoricVariableInstance> taskVars = historyService.createHistoricVariableInstanceQuery().list();

        Map<String, Map<String, Object>> historicData = new HashMap<>();

        List<CompletedAccessRequestWrapper> results = new ArrayList<>();

        taskVars.forEach(taskVar -> {
            String key = taskVar.getVariableName();
            Object value = taskVar.getValue();

            if (historicData.containsKey(taskVar.getProcessInstanceId())) {
                historicData.get(taskVar.getProcessInstanceId()).put(taskVar.getVariableName(), taskVar.getValue());
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("created", taskVar.getCreateTime());
                data.put(taskVar.getVariableName(), taskVar.getValue());
                historicData.put(taskVar.getProcessInstanceId(), data);
            }

            if (key.equals("requestStatus")) {
                historicData.get(taskVar.getProcessInstanceId()).put("updated", taskVar.getLastUpdatedTime());
            }
        });

        for (String k : historicData.keySet()) {
            Map<String, Object> varMap = historicData.get(k);
            RequestStatus status = (RequestStatus) varMap.get("requestStatus") != null
                    ? (RequestStatus) varMap.get("requestStatus") : RequestStatus.APPROVAL_PENDING;

            AccessRequest request = (AccessRequest) varMap.get("accessRequest");
            if(status.equals(RequestStatus.APPROVAL_PENDING)){
                request=updateInstanceStatus(request);
            }
            Date created = (Date) varMap.get("created");
            Date updated = (Date) varMap.get("updated");
            CompletedAccessRequestWrapper wrapper = new CompletedAccessRequestWrapper(request)
                    .setUpdated(updated)
                    .setAttempts((Integer) varMap.get("attempts"))
                    .setStatus(status)
                    .setActionedByUserId(request.getActionedByUserId())
                    .setActionedByUserName(request.getActionedByUserName());

            wrapper.setCreated(created);

            results.add(wrapper);
        }

        return (List<CompletedAccessRequestWrapper>)filterResults(results);
    }

    public AccessRequest updateInstanceStatus(AccessRequest accessRequest){
        AWSEnvironment environment = new AWSEnvironment(accessRequest.getAccount(),accessRequest.getRegion());
        List<AWSInstance> requestedInstances = accessRequest.getInstances();
        List<String> instanceIds = requestedInstances.stream().map(instance -> instance.getInstanceId()).collect(Collectors.toList());
        Map<String,String> instances = ssmService.checkInstancesWithSsm(environment, instanceIds);
        requestedInstances.forEach(instance ->
                instance.setStatus(instances.get(instance.getInstanceId()) != null ? instances.get(instance.getInstanceId()) : "Unknown")
        );
        accessRequest.setInstances(requestedInstances);
        accessRequestRepository.save(accessRequest);

        return accessRequest;

    }


    /**
     * Helper function to update the request comments / actionedBy fields for the access request
     *
     * @param requestId - the access request ID
     * @param approverComments - the comments from the approver
     * @param action - the action taken on the request
     */
    private void updateRequestDetails(Long requestId, String approverComments, String action){
        AccessRequest accessRequest = accessRequestRepository.findOne(requestId);
        accessRequest.setApproverComments(approverComments);
        GatekeeperUserEntry user = gatekeeperRoleService.getUserProfile();
        accessRequest.setActionedByUserId(user.getUserId());
        accessRequest.setActionedByUserName(user.getName());
        accessRequestRepository.save(accessRequest);
        logger.info("Access Request " + accessRequest.getId() + " was " + action +" by " + user.getName() + " (" + user.getUserId() +"). ");

    }

    /**
     * Approves the Request
     * @param taskId - the activiti task id
     * @param requestId - the AccessRequest object id
     * @param approverComments - The comments from the approver
     * @return - The updated list of Active Access Requests
     */
    @PreAuthorize("@gatekeeperRoleService.isApprover()")
    public List<ActiveAccessRequestWrapper> approveRequest(String taskId, Long requestId, String approverComments ) {
        updateRequestDetails(requestId, approverComments, APPROVED);
        handleRequest(gatekeeperRoleService.getUserProfile().getUserId(), taskId, RequestStatus.APPROVAL_GRANTED);
        return getActiveRequests();
    }

    /**
     * Rejects the request
     * @param taskId - the activiti task id
     * @param requestId - the AccessRequest object id
     * @param approverComments - The comments from the approver
     * @return - The updated list of Active Access Requests
     */
    @PreAuthorize("@gatekeeperRoleService.isApprover()")
    public List<ActiveAccessRequestWrapper> rejectRequest(String taskId, Long requestId, String approverComments) {
        updateRequestDetails(requestId, approverComments, REJECTED);
        handleRequest(gatekeeperRoleService.getUserProfile().getUserId(), taskId, RequestStatus.APPROVAL_REJECTED);
        return getActiveRequests();
    }

    /**
     * Cancels the request
     * @param taskId - the activiti task id
     * @param requestId - the AccessRequest object ID
     * @return - The list of active access requests
     */
    public List<ActiveAccessRequestWrapper> cancelRequest(String taskId, Long requestId){
        updateRequestDetails(requestId, "The Request was canceled", CANCELED);
        handleRequest(gatekeeperRoleService.getUserProfile().getUserId(), taskId, RequestStatus.CANCELED);
        return getActiveRequests();
    }

    private List<? extends AccessRequestWrapper> filterResults(List<? extends AccessRequestWrapper> results) {
        return results.stream().filter(AccessRequestWrapper -> gatekeeperRoleService.getRole().equals(GatekeeperRole.APPROVER)
                || gatekeeperRoleService.getUserProfile().getUserId().equalsIgnoreCase(AccessRequestWrapper.getRequestorId()))
                .collect(Collectors.toList());
    }
}
