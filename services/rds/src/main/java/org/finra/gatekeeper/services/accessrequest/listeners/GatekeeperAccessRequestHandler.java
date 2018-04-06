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
