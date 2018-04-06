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

package org.finra.gatekeeper.services.accessrequest.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The job of this handler is to set the accessRequest variable for the UserTask for better UI tracking.
 */
public class GatekeeperAccessRequestHandler implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(GatekeeperAccessRequestHandler.class);

    /**
     * @param delegateTask
     */
    public void notify(DelegateTask delegateTask){
        logger.info("User Task Created");
        AccessRequest obj = (AccessRequest)delegateTask.getExecution().getVariable("accessRequest");
        delegateTask.setOwner(obj.getRequestorId());
    }
}
