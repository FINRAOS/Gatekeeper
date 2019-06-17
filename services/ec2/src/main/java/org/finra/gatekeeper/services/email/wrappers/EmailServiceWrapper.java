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

package org.finra.gatekeeper.services.email.wrappers;

import org.finra.gatekeeper.configuration.properties.GatekeeperEmailProperties;
import org.finra.gatekeeper.services.accessrequest.model.AWSInstance;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.email.EmailService;
import org.finra.gatekeeper.services.email.model.GatekeeperLinuxNotification;
import org.finra.gatekeeper.services.email.model.GatekeeperWindowsNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for email message related activity
 */
@Component
public class EmailServiceWrapper {

    private final Logger logger  = LoggerFactory.getLogger(EmailServiceWrapper.class);

    private final GatekeeperEmailProperties emailProperties;

    private final EmailService emailService;

    @Autowired
    public EmailServiceWrapper(EmailService emailService, GatekeeperEmailProperties gatekeeperEmailProperties){
        this.emailService = emailService;
        this.emailProperties = gatekeeperEmailProperties;
    }

    /*
     * Overloaded emailHelper methods
     */

    private void emailHelper(String email, String cc, String subject, String templateName, AccessRequest request){
        emailHelper(email, cc, subject, templateName, request,null, null);
    }

    private void emailHelper(String email, String cc, String subject, String templateName, AccessRequest request, Map other){
        emailHelper(email, cc, subject, templateName, request, null, other);
    }

    private void emailHelper(String email, String cc, String subject, String templateName, AccessRequest request, User user){
        emailHelper(email, cc, subject, templateName, request, user, null);
    }

    private void emailHelper(String email, String cc, String subject, String templateName, AccessRequest request, User user, Map other){
        try{
            Map<String, Object> params = new HashMap<>();
            params.put("request", request);
            params.put("user", user);
            params.put("approverDL", emailProperties.getApproverEmails());
            if(other != null){
                other.forEach((k, v) -> params.put(k.toString(), v));
            }
            emailService.sendEmail(email, emailProperties.getFrom(), cc, subject, templateName, params);
        }catch(Exception ex){
            logger.info("Unable to push emails out to recipients " + email + " with cc " + cc + " using subject: " + subject
                    + " with template: " + templateName + "\n Access Request: " + request.toString());
            notifyAdminsOfFailure(request,ex);
        }
    }

    /**
     * Notifies the gatekeeper admins (the approvers) that there's a new access request in their bucket.
     *
     * @param request - The request the email is for
     */
    public void notifyAdmins(AccessRequest request){
        logger.info("Notify the admins of: " + request);
        emailHelper(emailProperties.getApproverEmails(), null, String.format("GATEKEEPER: Access Requested (%s)", request.getId()), "accessRequested", request);
    }

    public void notifyExpired(AccessRequest request){
        logger.info("notify users that their time is up " + request.getUsers());
        request.getUsers().forEach(user -> {
            emailHelper(user.getEmail(), null, "Your Access has expired", "accessExpired", request);
        });
    }

    /**
     * Default notification for Ops team, this will just pass all instances with the request, this will be called in cases of absolute failure
     * @param request
     */
    public void notifyOps(AccessRequest request){
        notifyOps(request, request.getInstances());
    }

    /**
     * Targeted notification for Ops team, this will pass a targeted list of instances for them to investigate, this will be called in cases where some
     * instances successfully had access revoked but some didn't.
     *
     * @param request
     * @param offlineInstances
     */
    public void notifyOps(AccessRequest request, List<AWSInstance> offlineInstances){
        logger.info("Some instances were not properly revoked, notifying the ops team ("+offlineInstances+")");
        Map<String, List<AWSInstance>> custom = new HashMap<>();
        custom.put("offlineInstances", offlineInstances);
        emailHelper(emailProperties.getOpsEmails(), emailProperties.getTeam(), "GATEKEEPER: Manual revoke access for expired request", "manualRemoval", request, custom);
    }

    public void notifyCanceled(AccessRequest request){
        logger.info("Notify user and approvers that request was canceled");
        emailHelper(emailProperties.getApproverEmails(), request.getRequestorEmail(), "Access Request " + request.getId() + " was canceled", "requestCanceled", request);
    }

    public void notifyRejected(AccessRequest request){
        logger.info("Notify the submitter that the request was rejected");
        emailHelper(request.getRequestorEmail(), null, "Access Request " + request.getId() + " was denied", "accessDenied", request);
    }

    public void notifyApproved(AccessRequest request){
        logger.info("Notify the submitter that the request was approved");
        emailHelper(request.getRequestorEmail(), null, "Access Request " + request.getId() + " was granted", "accessGranted", request);
    }

    public void notifyOfCredentials(AccessRequest request, GatekeeperLinuxNotification notification){
        logger.info("Send user their fresh credentials: " +  request);
        try {

            Map<String,Object> param = new HashMap<>();
            param.put("privatekey", notification.getKey());
            Map<String, Object> contentMap = new HashMap<String,Object>();
            contentMap.put("request", request);
            contentMap.put("user", notification.getUser());
            contentMap.put("instanceStatus", notification.getCreateStatus());

            //Send out just the username
            emailHelper(notification.getUser().getEmail(), null, "Access Request " + request.getId() + " - Your temporary username", "username", request, contentMap);
            
            //Send out just the pem
            emailService.sendEmailWithAttachment(notification.getUser().getEmail(), emailProperties.getFrom(), null,"Access Request " + request.getId() + " - Your temporary credential", "credentials", contentMap, "credential.pem", "privatekey", param, "application/x-pem-file");

        }catch(Exception e){
            logger.error("Error sending the team an email",e);
        }

    }

    public void notifyOfCancellation(AccessRequest request, GatekeeperWindowsNotification notification){
        logger.info("Notify user that adding temporary user was cancelled: " +  request);
        try {
            Map<String, Object> contentMap = new HashMap<String,Object>();
            contentMap.put("instances", notification.getCancelledInstances());

            emailHelper(notification.getUser().getEmail(),  null, "Could not add user", "processCancelled", request, notification.getUser(), contentMap);

        }catch(Exception e){
            logger.error("Error sending the team an email",e);
        }

    }

    public void notifyAdminsOfFailure(AccessRequest request, Throwable exception){
        logger.info("Notify The Admins that an exception was tossed trying to grant Access for " +  request);
        try {
            String stacktrace = constructStackTrace(exception);
            Map<String,Object> param = new HashMap<>();
            param.put("stacktrace", stacktrace);
            Map<String, Object> contentMap = new HashMap<String,Object>();
            contentMap.put("request", request);
            emailService.sendEmailWithAttachment(emailProperties.getTeam(), emailProperties.getFrom(),null, "Failure executing process", "failure",contentMap, "Exception.txt","exception",  param, "text/plain");
        }catch(Exception e){
            logger.error("Error sending the team an email",e);
        }

    }

    /**
     * Helper method for taking stack trace and making it printable
     * because Java doesn't have that built in for some reason. Used
     * for failure emails
     * @param exception The exception that is to be printed
     * @return The stacktrace as a String
     */
    private String constructStackTrace(Throwable exception){
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

}
