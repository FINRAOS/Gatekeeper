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

package org.finra.gatekeeper.services.accessrequest;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.task.Task;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperApprovalProperties;
import org.finra.gatekeeper.configuration.GatekeeperOverrideProperties;
import org.finra.gatekeeper.controllers.AccessRequestController;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.ActiveAccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.CompletedAccessRequestWrapper;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.accessrequest.model.*;
import org.finra.gatekeeper.services.accessrequest.model.response.AccessRequestCreationOutcome;
import org.finra.gatekeeper.services.accessrequest.model.response.AccessRequestCreationResponse;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.db.DatabaseConnectionService;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.account.model.Account;
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
    private final GatekeeperApprovalProperties approvalThreshold;
    private final AccountInformationService accountInformationService;
    private final GatekeeperOverrideProperties overridePolicy;
    private final DatabaseConnectionService databaseConnectionService;
    private final String REJECTED = "REJECTED";
    private final String APPROVED = "APPROVED";
    private final String CANCELED = "CANCELED";

    @Autowired
    public AccessRequestService(TaskService taskService,
                                AccessRequestRepository accessRequestRepository,
                                GatekeeperRoleService gatekeeperRoleService,
                                RuntimeService runtimeService,
                                HistoryService historyService,
                                GatekeeperApprovalProperties gatekeeperApprovalProperties,
                                AccountInformationService accountInformationService,
                                GatekeeperOverrideProperties overridePolicy,
                                DatabaseConnectionService databaseConnectionService){
        this.taskService = taskService;
        this.accessRequestRepository = accessRequestRepository;
        this.gatekeeperRoleService = gatekeeperRoleService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.approvalThreshold = gatekeeperApprovalProperties;
        this.accountInformationService = accountInformationService;
        this.overridePolicy = overridePolicy;
        this.databaseConnectionService = databaseConnectionService;
    }

    /**
     * Store the Access Request and either grant or require approval. Before the access request is written to the database the users
     * provided will be checked against each DB to make sure that the users can be successfully created.
     *
     * @param request
     * @return AccessRequest - if the user/db check succeeds, Map - if theres any
     * @throws GatekeeperException
     */
    public AccessRequestCreationResponse storeAccessRequest(AccessRequestWrapper request) throws GatekeeperException{
        GatekeeperUserEntry requestor = gatekeeperRoleService.getUserProfile();

        Integer maxDays = overridePolicy.getMaxDaysForRequest(gatekeeperRoleService.getRoleMemberships().get(request.getApplication()), request.getRoles(), request.getAccountSdlc());

        if(request.getDays() > maxDays){
            throw new GatekeeperException("Days requested (" + request.getDays() + ") exceeded the maximum of " + maxDays + " for roles " + request.getRoles() + " on account with SDLC " + request.getAccountSdlc());
        }


        //throw gk in front of all the user id's
        request.getUsers().forEach(u -> u.setUserId("gk_" + u.getUserId()));
        Account theAccount = accountInformationService.getAccountByAlias(request.getAccount());

        AWSEnvironment environment = new AWSEnvironment(theAccount.getAlias().toUpperCase(), request.getRegion());

        AccessRequest accessRequest = new AccessRequest()
                .setAccount(request.getAccount().toUpperCase())
                .setAccountSdlc(request.getAccountSdlc())
                .setRegion(request.getRegion())
                .setDays(request.getDays())
                .setRequestorId(requestor.getUserId())
                .setRequestorName(requestor.getName())
                .setRequestorEmail(requestor.getEmail())
                .setUsers(request.getUsers())
                .setAwsRdsInstances(request.getInstances())
                .setTicketId(request.getTicketId())
                .setRequestReason(request.getRequestReason())
                .setRoles(request.getRoles());

        logger.info("Checking Users associated with this access request");

        Map<String, List<String>> checkResult;
        try {
            checkResult = databaseConnectionService.checkUsersAndDbs(request.getRoles(), request.getUsers(), request.getInstances());
        }catch(Exception e){
            throw new GatekeeperException("Unable to verify the Users for the provided databases");
        }

        if(!checkResult.isEmpty()){
            return new AccessRequestCreationResponse(AccessRequestCreationOutcome.NOT_CREATED_USER_ISSUE,checkResult);
        }

        logger.info("Storing Access Request");
        accessRequestRepository.save(accessRequest);
        logger.info("Access Request stored with ID: " + accessRequest.getId());

        //Kick off the activiti workflow

        Map<String, Object> variables = new HashMap<>();
        variables.put("accessRequest", accessRequest);
        runtimeService.startProcessInstanceByKey("gatekeeperAccessRequest", variables);

        // Verify that we started a new process instance
        logger.info("Number of process instances: " + runtimeService.createProcessInstanceQuery().count());
        return new AccessRequestCreationResponse(AccessRequestCreationOutcome.CREATED, accessRequest);
    }

    /**
     * Make sure the DBA has the Application Membership that the DB is assigned to in its tags
     * @param request - the AccessRequest
     * @return
     */
    private boolean isDBAOwnerOfInstances(AccessRequest request) {
        Set<String> memberships = gatekeeperRoleService.getDbaMemberships(request.getRequestorId());
        return request.getAwsRdsInstances().stream().allMatch(
                instance -> memberships.contains(instance.getApplication())
        );
    }

    /**
     * Make sure the DEV has the Application Membership + the proper SDLC role on the account for the DB they are trying to access
     * @param request -  The Access Request
     * @return
     */
    private boolean isDevOwnerOfInstances(AccessRequest request, String sdlc) {
        Map<String, Set<String>> memberships = gatekeeperRoleService.getDevMemberships(request.getRequestorId());
        return request.getAwsRdsInstances().stream().allMatch(
                instance -> memberships.containsKey(instance.getApplication()) && memberships.get(instance.getApplication()).contains(sdlc.toUpperCase())
        );
    }

    public boolean isApprovalNeeded(AccessRequest request) throws Exception{
        //We have to associate the policy to the SDLC of the requested account. The name of the account provided by the ui will not always be "dev" "qa" or "prod", but they will need to associate with those SDLC's
        Account theAccount = accountInformationService.getAccountByAlias(request.getAccount());

        switch(gatekeeperRoleService.getRole()){
            //Approvers can do whatever they want.
            case APPROVER:
                return false;
            case DBA:
                return !isRequestedDaysWithinPolicy(request.getRoles(), request.getAccountSdlc(), request.getDays()) || !isDBAOwnerOfInstances(request);
            case DEV:
                return !isRequestedDaysWithinPolicy(request.getRoles(), request.getAccountSdlc(), request.getDays()) || !isDevOwnerOfInstances(request, theAccount.getSdlc());
            case OPS:
                return !isRequestedDaysWithinPolicy(request.getRoles(), request.getAccountSdlc(), request.getDays());
            default:
                //should NEVER happen.
                throw new GatekeeperException("Could not determine Role");
        }
    }

    //returns true if days is lower than the threshold
    //returns false if one of the thresholds arent met
    private boolean isRequestedDaysWithinPolicy(List<UserRole> roles, String sdlc, Integer days){
        //Getting the crazy approval policy
        Map<String, Map<String, Integer>> crazyApprovalPolicy = approvalThreshold.getApprovalPolicy(gatekeeperRoleService.getRole());

        //if one requirement is not
        return roles.stream().allMatch(role -> days <= crazyApprovalPolicy.get(role.getRole().toLowerCase()).get(sdlc.toLowerCase()));
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
            AccessRequest theRequest = accessRequestRepository.findOne(Long.valueOf(
                    runtimeService.getVariableInstance(task.getExecutionId(), "accessRequest").getTextValue2())
            );

            response.add(new ActiveAccessRequestWrapper(theRequest)
                    .setCreated(task.getCreateTime())
                    .setTaskId(task.getId())
                    .setInstanceCount(theRequest.getAwsRdsInstances().size())
                    .setUserCount(theRequest.getUsers().size()));
        });

        return (List<ActiveAccessRequestWrapper>)filterResults(response);
    }

    public List<CompletedAccessRequestWrapper> getCompletedRequests() {
    /*  This object is all of the Activiti Variables associated with the request
        This map will contain the following:
        When the request was opened
        When the request was actioned by an approver (or canceled by a user/approver)
        How many attempts it took to approve the user*/
        Map<String, Map<String, Object>> historicData = new HashMap<>();

        //we have to map the activiti hitory items to the gatekeeper requests map activiti objects to the access requests
        Map<String, Long> activitiAccessRequestMap = new HashMap<>();
        //map gatekeeper access request ids
        Map<Long, AccessRequest> gkAccessRequestMap = new HashMap<>();

    /*this gets all of the access requests history items. We exclude variable initialization
        because activiti will break if we ever change the access request object so we'll just fetch the items
        ourselves */
        List<HistoricVariableInstance> accessRequests = historyService.createHistoricVariableInstanceQuery()
                .excludeVariableInitialization()
                .variableName("accessRequest")
                .list();

        //we'll get all the access requests and store the created date into the historicData map
        //as well as map the activiti item to the access request to which it corresponds
        accessRequests.forEach(taskVar -> {
            Map<String, Object> data = new HashMap<>();
            data.put("created", taskVar.getCreateTime());
            historicData.put(taskVar.getProcessInstanceId(), data);
            activitiAccessRequestMap.put(taskVar.getProcessInstanceId(), Long.valueOf(((HistoricVariableInstanceEntity) taskVar).getTextValue2()));
        });

        //run a query to grab all of the access requests that we found in activiti (this pretty much returns all
        //for now, until we implement it to use a time window)
        accessRequestRepository.findAll(activitiAccessRequestMap.values()).forEach(accessRequest -> {
            gkAccessRequestMap.put(accessRequest.getId(), accessRequest);
        });

    /*  this one's kind of a hack but this query gets the RequestStatus value for a request
        we do it like this because once again if the package name changes it won't update the database and will
        activiti will try to instantiate the object using Reflection. to work around this we just pull out the value
        for the enum and instantiate it ourselves

        we also put the updated time into the historicData map here as this is when the request was updated
     */
        historyService.createNativeHistoricVariableInstanceQuery()
                .sql("select a.*, substring(encode(b.bytes_, 'escape'), '\\w+$') as textValue\n" +
                        "  from act_hi_varinst a join act_ge_bytearray b on a.bytearray_id_ = b.id_;\n")
                .list()
                .forEach(item -> {
                    historicData.get(item.getProcessInstanceId()).put(item.getVariableName(), ((HistoricVariableInstanceEntity)item).getTextValue());
                    historicData.get(item.getProcessInstanceId()).put("updated", item.getLastUpdatedTime());
                });

        //This gets all the attempts data for the requests and inserts it into the historicData map
        historyService.createHistoricVariableInstanceQuery()
                .excludeVariableInitialization()
                .variableName("attempts")
                .list()
                .forEach(item -> {
                    historicData.get(item.getProcessInstanceId()).put(item.getVariableName(), item.getValue());
                });


        List<CompletedAccessRequestWrapper> results = new ArrayList<>();

        //this section compiles the historicData into the response object for the UI to consume
        for (String k : historicData.keySet()) {
            Map<String, Object> varMap = historicData.get(k);
            Long requestId = activitiAccessRequestMap.get(k);
            AccessRequest request = gkAccessRequestMap.get(requestId);
            if(request != null) {
                //we instantiate the RequestStatus value here.
                RequestStatus status = varMap.get("requestStatus") != null
                        ? RequestStatus.valueOf((String) varMap.get("requestStatus"))
                        : RequestStatus.APPROVAL_PENDING;

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
            }else{
                logger.warn("Could not get request details for AccessRequest with ID: " + requestId + ". This request will not be returned ");
            }
        }

        return (List<CompletedAccessRequestWrapper>)filterResults(results);
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
        updateRequestDetails(requestId, "This request was canceled", CANCELED);
        handleRequest(gatekeeperRoleService.getUserProfile().getUserId(), taskId, RequestStatus.CANCELED);
        return getActiveRequests();
    }

    private List<? extends AccessRequestWrapper> filterResults(List<? extends AccessRequestWrapper> results) {
        return results.stream().filter(AccessRequestWrapper -> gatekeeperRoleService.getRole().equals(GatekeeperRdsRole.APPROVER)
                || gatekeeperRoleService.getUserProfile().getUserId().equalsIgnoreCase(AccessRequestWrapper.getRequestorId()))
                .collect(Collectors.toList());
    }
}
