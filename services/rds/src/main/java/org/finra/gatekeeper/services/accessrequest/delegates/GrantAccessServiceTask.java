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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.rds.model.RoleType;
import org.finra.gatekeeper.services.accessrequest.model.*;
import org.finra.gatekeeper.services.db.DatabaseConnectionService;
import org.finra.gatekeeper.services.email.wrappers.EmailServiceWrapper;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.passwords.PasswordGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Service task to grant access using the details from an AccessRequest
 */
@Component
public class GrantAccessServiceTask implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(GrantAccessServiceTask.class);

    private final DatabaseConnectionService databaseConnectionService;
    private final PasswordGenerationService passwordGenerationService;
    private final EmailServiceWrapper emailServiceWrapper;

    @Autowired
    public GrantAccessServiceTask(DatabaseConnectionService databaseConnectionService,
                                  PasswordGenerationService passwordGenerationService,
                                  EmailServiceWrapper emailServiceWrapper) {
        this.databaseConnectionService = databaseConnectionService;
        this.passwordGenerationService = passwordGenerationService;
        this.emailServiceWrapper = emailServiceWrapper;
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

        AccessRequest accessRequest = (AccessRequest) execution.getVariable("accessRequest");
        logger.info("Granting Access to " + accessRequest);
        try {

            // Prepare parameters
            AWSEnvironment env = new AWSEnvironment(accessRequest.getAccount(), accessRequest.getRegion());
            logger.info("Environment for this access request is " + env.getAccount() + " ( " + env.getRegion() + " )");

            //bundle up the role -> db -> schema/table offerings
            Map<String, Map<RoleType, List<String>>> schemasForRequest = new HashMap<>();
            for(AWSRdsDatabase db : accessRequest.getAwsRdsInstances()){
                schemasForRequest.put(db.getName(), databaseConnectionService.getAvailableSchemasForDb(db));
            }

            // Do all of this for each user in the request
            for (User u : accessRequest.getUsers()) {
                //have to apply the roles to each user in the request
                for(UserRole role : accessRequest.getRoles()){
                    // Generate keypair
                    String password = passwordGenerationService.generatePassword();
                    if (password == null) {
                        throw new GatekeeperException("Could not generate Password");
                    }

                    RoleType roleType = RoleType.valueOf(role.getRole().toUpperCase());
                    Map<String, Boolean> createStatus = databaseConnectionService.grantAccess(accessRequest.getAwsRdsInstances(),  u.getUserId(), roleType, password, accessRequest.getDays());
                    if (createStatus.values().stream().allMatch(item -> item == Boolean.FALSE)) {
                        throw new GatekeeperException("Could not create user account on any DB instances");
                    }

                    // Send email with private key
                    emailServiceWrapper.notifyOfCredentials(accessRequest,u, roleType, password, schemasForRequest);
                }
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
}
