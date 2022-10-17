/*
 * Copyright 2022. Gatekeeper Contributors
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.common.services.eventlogging.*;
import org.finra.gatekeeper.configuration.GatekeeperApprovalProperties;
import org.finra.gatekeeper.configuration.GatekeeperOverrideProperties;
import org.finra.gatekeeper.controllers.AccessRequestController;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.ActiveAccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.CompletedAccessRequestWrapper;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.rds.model.RoleType;
import org.finra.gatekeeper.services.accessrequest.model.*;
import org.finra.gatekeeper.services.accessrequest.model.response.AccessRequestCreationOutcome;
import org.finra.gatekeeper.services.accessrequest.model.response.AccessRequestCreationResponse;
import org.finra.gatekeeper.services.auth.model.AppApprovalThreshold;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.finra.gatekeeper.services.aws.SnsService;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.db.DatabaseConnectionService;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.finra.gatekeeper.services.email.EmailServiceWrapper;
import org.finra.gatekeeper.services.group.service.GatekeeperGroupAuthService;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.persistence.*;
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
    private final EmailServiceWrapper emailServiceWrapper;
    private final SnsService snsService;
    private final EntityManager entityManager;
    private final GatekeeperGroupAuthService gatekeeperGroupAuthService;
    private final String REJECTED = "REJECTED";
    private final String APPROVED = "APPROVED";
    private final String CANCELED = "CANCELED";

    protected static final String REQUESTS_QUERY = new StringBuilder()
            .append("SELECT access_request.account_sdlc,\n")
            .append("       access_request.request_reason,\n")
            .append("       access_request.region,\n")
            .append("       access_request.approver_comments,\n")
            .append("       access_request.actioned_by_user_name,\n")
            .append("       access_request.actioned_by_user_id,\n")
            .append("       access_request.ticket_id,\n")
            .append("       access_request.id,\n")
            .append("       access_request.account,\n")
            .append("       access_request.requestor_name,\n")
            .append("       access_request.requestor_email,\n")
            .append("       access_request.requestor_id,\n")
            .append("       access_request.days,\n")
            .append("       created, \n")
            .append("       updated, \n")
            .append("       user_count, \n")
            .append("       status from\n")
            .append("                        gatekeeper_rds.access_request access_request,\n")
            .append("                        -- This gets the access request id and their created time, their updated time and their request status from activiti tables\n")
            .append("                        (select cast(text2_ as numeric) access_request_id, create_time_ as created, last_updated_time_ as updated, status\n")
            .append("                         from (select a.proc_inst_id_, a.text2_\n")
            .append("                               from gatekeeper_rds.act_hi_varinst a\n")
            .append("                               where name_ = 'accessRequest') accessRequestId,\n")
            .append("                              -- This gets the request status\n")
            .append("                              (select a.proc_inst_id_,\n")
            .append("                                      a.create_time_,\n")
            .append("                                      a.last_updated_time_,\n")
            .append("                                      substring(encode(b.bytes_, 'escape'), '\\w+$') as status\n")
            .append("                               from gatekeeper_rds.act_hi_varinst a\n")
            .append("                                      join gatekeeper_rds.act_ge_bytearray b on a.bytearray_id_ = b.id_) accessRequestStatus\n")
            .append("                         where accessRequestId.proc_inst_id_ = accessRequestStatus.proc_inst_id_\n")
            .append("                        ) gk_activiti,\n")
            .append("                        -- This counts the users oer request\n")
            .append("                        (select a.id, count(*) as user_count from gatekeeper_rds.access_request a, gatekeeper_rds.access_request_users b\n")
            .append("                         where a.id = b.access_request_id\n")
            .append("                         group by a.id) users,\n")
            .append("                        -- This counts the dbs per request\n")
            .append("                        (select a.id, count(*) as databases from gatekeeper_rds.access_request a, gatekeeper_rds.access_request_aws_rds_instances b\n")
            .append("                         where a.id = b.access_request_id\n")
            .append("                         group by a.id) databases\n")
            .append("where access_request.id = gk_activiti.access_request_id\n")
            .append("  and access_request.id = users.id\n")
            .append("  and access_request.id = databases.id\n")
            .toString();

    protected static final String ROLE_QUERY = "SELECT id, role\n" +
            "FROM gatekeeper_rds.access_request_roles a, gatekeeper_rds.request_role r\n" +
            "WHERE a.access_request_id = :request_id\n" +
            "AND a.roles_id = r.id;";
    protected static final String INSTANCE_QUERY = "SELECT id, name, application, instance_id, db_name, engine, endpoint, status, database_type, arn\n" +
            "FROM gatekeeper_rds.access_request_aws_rds_instances w, gatekeeper_rds.request_database c\n" +
            "WHERE w.access_request_id = :request_id \n" +
            "AND w.aws_rds_instances_id = c.id;";
    protected static final String USER_QUERY = "SELECT id, name, user_id, email\n" +
            "FROM gatekeeper_rds.access_request_users a, gatekeeper_rds.request_user r\n" +
            "WHERE a.access_request_id = :request_id \n" +
            "AND a.users_id = r.id;";





    @Autowired
    public AccessRequestService(TaskService taskService,
                                AccessRequestRepository accessRequestRepository,
                                GatekeeperRoleService gatekeeperRoleService,
                                RuntimeService runtimeService,
                                HistoryService historyService,
                                GatekeeperApprovalProperties gatekeeperApprovalProperties,
                                AccountInformationService accountInformationService,
                                GatekeeperOverrideProperties overridePolicy,
                                DatabaseConnectionService databaseConnectionService,
                                EmailServiceWrapper emailServiceWrapper,
                                SnsService snsService,
                                EntityManager entityManager,
                                GatekeeperGroupAuthService groupAuthService
    ){
        this.taskService = taskService;
        this.accessRequestRepository = accessRequestRepository;
        this.gatekeeperRoleService = gatekeeperRoleService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.approvalThreshold = gatekeeperApprovalProperties;
        this.accountInformationService = accountInformationService;
        this.overridePolicy = overridePolicy;
        this.databaseConnectionService = databaseConnectionService;
        this.emailServiceWrapper = emailServiceWrapper;
        this.snsService = snsService;
        this.entityManager = entityManager;
        this.gatekeeperGroupAuthService = groupAuthService;
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

        //Approvers override elevated requirements
        if(!gatekeeperRoleService.isApprover()){
            //Fails if they do not have authorization for an elevated request
            String response = gatekeeperGroupAuthService.hasGroupAuth(request, requestor);
            if(!response.equals("Allowed")){
                logger.error("User is not authorized for this request.");
                return new AccessRequestCreationResponse(AccessRequestCreationOutcome.USER_NOT_AUTHORIZED,response);
            }
        }


        Integer maxDays = gatekeeperRoleService.isApprover() ? overridePolicy.getMaxDays() : overridePolicy.getMaxDaysForRequest(gatekeeperRoleService.getRoleMemberships(), request.getRoles(), request.getAccountSdlc());
        logger.info("Maximum days allowed for user " + requestor.getUserId() + ": " + maxDays);

        if(request.getDays() > maxDays){
            throw new GatekeeperException("Days requested (" + request.getDays() + ") exceeded the maximum of " + maxDays + " for roles " + request.getRoles() + " on account with SDLC " + request.getAccountSdlc());
        }


        //throw gk in front of all the user id's
        request.getUsers().forEach(u -> u.setUserId("gk_" + u.getUserId()));

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

        List<String> checkResult;
        AWSRdsDatabase database = accessRequest.getAwsRdsInstances().get(0);
        try {
            checkResult = databaseConnectionService.checkUsersAndDbs(request.getRoles(), request.getUsers(), request.getInstances().get(0),
                    new AWSEnvironment(accessRequest.getAccount(), accessRequest.getRegion(), accessRequest.getAccountSdlc()));
        }catch(Exception e){
            throw new GatekeeperException("Unable to verify the Users for the provided databases", e);
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

        try {
            boolean approvalNeeded = isApprovalNeeded(accessRequest);
            boolean topicSet = snsService.isTopicSet();
            if (approvalNeeded && topicSet) {
                snsService.pushToSNSTopic(accessRequest);
            } else if (!approvalNeeded){
                logger.info("Approval is not required for this request (" + accessRequest.getId() + "). Skipping publishing of access request to SNS topic.");
            } else {
                logger.info("SNS topic ARN not provided. Skipping publishing of access request to SNS topic.");
            }
        } catch (Exception e) {
            Long accessRequestId = accessRequest.getId();
            emailServiceWrapper.notifyAdminsOfFailure(accessRequest, e);
            logger.error("Unable to push access request (" + accessRequestId + ") to SNS topic.");
        }
        RequestEventLogger.logEventToJson(EventType.AccessRequested, accessRequest);
        return new AccessRequestCreationResponse(AccessRequestCreationOutcome.CREATED, accessRequest);
    }

    public boolean isApprovalNeeded(AccessRequest request) throws Exception{
        logger.info("Checking whether user " + request.getRequestorId() + " requires approval for request " + request.getId());

        //Approvers never require approval
        if(gatekeeperRoleService.isApprover()) {
            logger.info("User " + request.getRequestorId() + " is an approver. No approval required.");
            return false;
        }
        //We have to associate the policy to the SDLC of the requested account. The name of the account provided by the ui will not always be "dev" "qa" or "prod", but they will need to associate with those SDLC's
        Account theAccount = accountInformationService.getAccountByAlias(request.getAccount());

        Map<String, RoleMembership> roleMemberships = gatekeeperRoleService.getRoleMemberships();
        logger.info("Retrieving approval policy for user: " + request.getRequestorId());
        Map<String, AppApprovalThreshold> approvalThresholds = approvalThreshold.getApprovalPolicy(roleMemberships);
        logger.info("Approval thresholds for request " + request.getId() + ": " + approvalThresholds);

        String application = request.getAwsRdsInstances().get(0).getApplication();
        String accountSdlc = theAccount.getSdlc().toLowerCase();
        Integer approvalRequiredThreshold = overridePolicy.getMaxDays();
        if(approvalThresholds.containsKey(application)) {
            Map<RoleType, Map<String, Integer>> appApprovalThresholds = approvalThresholds.get(application).getAppApprovalThresholds();
            for(UserRole userRole : request.getRoles()){
                if (appApprovalThresholds.containsKey(RoleType.valueOf(userRole.getRole().toUpperCase())) &&
                        appApprovalThresholds.get(RoleType.valueOf(userRole.getRole().toUpperCase())).containsKey(accountSdlc) &&
                        appApprovalThresholds.get(RoleType.valueOf(userRole.getRole().toUpperCase())).get(accountSdlc) < approvalRequiredThreshold) {
                    approvalRequiredThreshold = appApprovalThresholds.get(RoleType.valueOf(userRole.getRole().toUpperCase())).get(accountSdlc);
                }
            }
            if(request.getDays() <= approvalRequiredThreshold) {
                logger.info("User " + request.getRequestorId() + " does not require approval for request " + request.getId() + ". Request duration: " + request.getDays() + ". Threshold: " + approvalRequiredThreshold);
                return false;
            } else {
                logger.info("User " + request.getRequestorId() + " requires approval for request " + request.getId() + ". Request duration: " + request.getDays() + ". Threshold: " + approvalRequiredThreshold);
                return true;
            }
        }

        logger.info("User " + request.getRequestorId() + " is not a member of any " + application + " AD groups.");
        //Always require approval if the user is not a member of the application's AD groups
        return true;
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
            AccessRequest theRequest = accessRequestRepository.getAccessRequestById(Long.valueOf(
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

    public List<CompletedAccessRequestWrapper> getRequest(Long id) {
        final ObjectMapper mapper = new ObjectMapper();
        List<CompletedAccessRequestWrapper> results = new ArrayList<>();

        String query = new StringBuilder(REQUESTS_QUERY)
                .append("and access_request.id = :request_id \n")
                .append("order by updated desc;")
                .toString();

        NativeQueryImpl q = (NativeQueryImpl) entityManager.createNativeQuery(query);
        q.setParameter("request_id", id);
        q.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

        NativeQueryImpl roleQuery = (NativeQueryImpl) entityManager.createNativeQuery(ROLE_QUERY);
        roleQuery.setParameter("request_id", id);
        roleQuery.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

        NativeQueryImpl instanceQuery = (NativeQueryImpl) entityManager.createNativeQuery(INSTANCE_QUERY);
        instanceQuery.setParameter("request_id", id);
        instanceQuery.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

        NativeQueryImpl userQuery = (NativeQueryImpl) entityManager.createNativeQuery(USER_QUERY);
        userQuery.setParameter("request_id", id);
        userQuery.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

        List < Map < String, AccessRequestWrapper >> result = q.getResultList();
        List < Map < String, User >> userResult = userQuery.getResultList();
        List < Map < String, UserRole >> roleResult = roleQuery.getResultList();
        List < Map < String, AWSRdsDatabase >> instanceResult = instanceQuery.getResultList();

        for (Map<String, AccessRequestWrapper> map: result) {
            CompletedAccessRequestWrapper requestWrapper = mapper.convertValue(map, CompletedAccessRequestWrapper.class);

            for (Map<String, UserRole> roleMap: roleResult) {
                UserRole role = mapper.convertValue(roleMap, UserRole.class);
                requestWrapper.getRoles().add(role);
            }

            for (Map<String, AWSRdsDatabase> instanceeMap: instanceResult) {
                AWSRdsDatabase instance = mapper.convertValue(instanceeMap, AWSRdsDatabase.class);
                requestWrapper.getInstances().add(instance);
            }

            for (Map<String, User> userMap: userResult) {
                User user = mapper.convertValue(userMap, User.class);
                requestWrapper.getUsers().add(user);
            }

            results.add(requestWrapper);
        }

        return (List<CompletedAccessRequestWrapper>)filterResults(results);
    }

    public List<CompletedAccessRequestWrapper> getCompletedRequests() {
    /*  This object is all of the Activiti Variables associated with the request
        This map will contain the following:
        When the request was opened
        When the request was actioned by an approver (or canceled by a user/approver)
        How many attempts it took to approve the user
    */
        final ObjectMapper mapper = new ObjectMapper();
        List<CompletedAccessRequestWrapper> results = new ArrayList<>();

        String query = new StringBuilder(REQUESTS_QUERY)
                .append("order by updated desc;")
                .toString();

        NativeQueryImpl q = (NativeQueryImpl) entityManager.createNativeQuery(query);
        q.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

        List < Map < String, AccessRequestWrapper >> result = q.getResultList();
        for (Map<String, AccessRequestWrapper> map: result) {
            CompletedAccessRequestWrapper requestWrapper = mapper.convertValue(map, CompletedAccessRequestWrapper.class);
            results.add(requestWrapper);
        }

        return (List<CompletedAccessRequestWrapper>)filterResults(results);
    }

    /**
     * Gets all live requests currently active within the gatekeeper system
     * @return - all live AccessRequest objects in the Gatekeeper System
     */
    public List<CompletedAccessRequestWrapper> getLiveRequests() {
        List<AccessRequest> liveRequests = accessRequestRepository.getLiveAccessRequests();
        Map<String, Map<String, Object>> liveRequestExpirationData = accessRequestRepository.getLiveAccessRequestExpirations()
                .stream()
                .collect(Collectors.toMap(item -> item.get("id").toString(), item -> item));

        return liveRequests.stream().map(
                item -> new CompletedAccessRequestWrapper(item)
                    .setUpdated((Date)liveRequestExpirationData.get(String.valueOf(item.getId())).get("granted_on"))
                    .setExpirationDate((Date)liveRequestExpirationData.get(String.valueOf(item.getId())).get("expire_time"))
        ).collect(Collectors.toList());
    }

    /**
     * This checks to see if there is any live requests still active for the user for the given account and database
     * @param userId - the user id for the user
     * @param accountName - the name of the account
     * @param database - the name of the database
     * @return a list of access requests that the user still has active on that database.
     */
    public List<AccessRequest> getLiveRequestsForUserOnDatabase(String userId, String accountName, String database, UserRole role) {
        return accessRequestRepository.getLiveAccessRequestsForUserAccountDbNameAndRole(userId, accountName, database, role.getRole());
    }

    /**
     * Helper function to update the request comments / actionedBy fields for the access request
     *
     * @param requestId - the access request ID
     * @param approverComments - the comments from the approver
     * @param action - the action taken on the request
     */
    private void updateRequestDetails(Long requestId, String approverComments, String action){
        AccessRequest accessRequest = accessRequestRepository.getAccessRequestById(requestId);
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
                || gatekeeperRoleService.getRole().equals(GatekeeperRdsRole.AUDITOR)
                || gatekeeperRoleService.getUserProfile().getUserId().equalsIgnoreCase(AccessRequestWrapper.getRequestorId()))
                .collect(Collectors.toList());
    }
}
