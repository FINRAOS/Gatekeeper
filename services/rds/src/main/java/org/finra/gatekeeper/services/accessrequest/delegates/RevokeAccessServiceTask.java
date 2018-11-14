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

package org.finra.gatekeeper.services.accessrequest.delegates;

import org.activiti.engine.ManagementService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.Job;
import org.finra.gatekeeper.rds.model.RoleType;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.accessrequest.model.UserRole;
import org.finra.gatekeeper.services.db.DatabaseConnectionService;
import org.finra.gatekeeper.services.email.wrappers.EmailServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Service task to grant access using the details from an AccessRequest
 */
@Component
public class RevokeAccessServiceTask implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(RevokeAccessServiceTask.class);

    private final EmailServiceWrapper emailServiceWrapper;
    private final DatabaseConnectionService databaseConnectionService;
    private final ManagementService managementService;

    @Autowired
    public RevokeAccessServiceTask(EmailServiceWrapper emailServiceWrapper,
                                   DatabaseConnectionService databaseConnectionService,
                                   ManagementService managementService) {
        this.emailServiceWrapper = emailServiceWrapper;
        this.databaseConnectionService = databaseConnectionService;
        this.managementService = managementService;
    }

    /***
     * @param execution - the request to execute on
     * @throws Exception - if the revocation fails
     */
    public void execute(DelegateExecution execution) throws Exception{
        Job job = managementService.createJobQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
        AccessRequest accessRequest = (AccessRequest)execution.getVariable("accessRequest");
        try {
            logger.info("Revoking access for Users, Attempts remaining: " + job.getRetries());
            for(User user : accessRequest.getUsers()){
                for(UserRole role : accessRequest.getRoles()) {
                    databaseConnectionService.revokeAccess(accessRequest.getAwsRdsInstances(), RoleType.valueOf(role.getRole().toUpperCase()), user.getUserId());
                }
            }

        }catch(Exception e){
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
