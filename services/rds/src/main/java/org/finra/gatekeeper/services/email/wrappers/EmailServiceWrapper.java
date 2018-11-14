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

package org.finra.gatekeeper.services.email.wrappers;

import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.model.RoleType;
import org.finra.gatekeeper.services.accessrequest.model.AWSRdsDatabase;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.email.EmailService;
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

    private EmailService emailService;

    private String approverEmails;
    private String opsEmails;
    private String teamEmail;
    private String mailFrom;

    @Autowired
    public EmailServiceWrapper(EmailService emailService, GatekeeperProperties gatekeeperProperties){
        this.emailService = emailService;
        this.approverEmails = gatekeeperProperties.getEmail().getApproverEmails();
        this.opsEmails = gatekeeperProperties.getEmail().getOpsEmails();
        this.teamEmail = gatekeeperProperties.getEmail().getTeam();
        this.mailFrom = gatekeeperProperties.getEmail().getFrom();
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
            if(other != null){
                other.forEach((k, v) -> params.put(k.toString(), v));
            }
            emailService.sendEmail(email, mailFrom, cc, subject, templateName, params);
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
        emailHelper(approverEmails, null, "GATEKEEPER: Access Requested", "accessRequested", request);
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
        notifyOps(request, request.getAwsRdsInstances());
    }

    /**
     * Targeted notification for Ops team, this will pass a targeted list of instances for them to investigate, this will be called in cases where some
     * instances successfully had access revoked but some didn't.
     *
     * @param request
     * @param offlineInstances
     */
    public void notifyOps(AccessRequest request, List<AWSRdsDatabase> offlineInstances){
        logger.info("Some instances were not properly revoked, notifying the ops team ("+offlineInstances+")");
        Map<String, List<AWSRdsDatabase>> custom = new HashMap<>();
        custom.put("offlineInstances", offlineInstances);
        emailHelper(opsEmails, teamEmail, "GATEKEEPER: Manual revoke access for expired request", "manualRemoval", request, custom);
    }

    public void notifyCanceled(AccessRequest request){
        logger.info("Notify user and approvers that request was canceled");
        emailHelper(approverEmails, request.getRequestorEmail(), "Access Request " + request.getId() + " was canceled", "requestCanceled", request);
    }

    public void notifyRejected(AccessRequest request){
        logger.info("Notify the submitter that the request was rejected");
        emailHelper(request.getRequestorEmail(), null, "Access Request " + request.getId() + " was denied", "accessDenied", request);
    }

    public void notifyApproved(AccessRequest request){
        logger.info("Notify the submitter that the request was approved");
        emailHelper(request.getRequestorEmail(), null, "Access Request " + request.getId() + " was granted", "accessGranted", request);
    }

    public void notifyOfCredentials(AccessRequest request, User user, RoleType roleType, String password, Map<String, Map<RoleType, List<String>>> schemaTables){
        logger.info("Send user their fresh credentials: " +  request);
        try {

            Map<String, Object> contentMap = new HashMap<String,Object>();
            contentMap.put("request", request);
            contentMap.put("user", user);
            contentMap.put("userName", user.getUserId() + "_" + roleType.getShortSuffix());
            contentMap.put("password", password);
            contentMap.put("role", roleType);

            //Freemarker hates the enum map, so convert the enums to strings
            Map<String, Map<String, List<String>>> convertedSchemaTables = new HashMap<>();

            schemaTables.entrySet().forEach(entry -> {
                Map<String, List<String>> converted = new HashMap<>();
                entry.getValue().entrySet().forEach(schema -> {
                    converted.put(schema.getKey().toString(), schema.getValue());
                });
                convertedSchemaTables.put(entry.getKey(), converted);
            });

            contentMap.put("schemaTables", convertedSchemaTables);

            emailService.sendEmail(user.getEmail(), mailFrom, null, "Gatekeeper: ["+request.getId()+"] You have been granted " + roleType.getDbRole() + " access", "userGrant", contentMap);
            emailService.sendEmail(user.getEmail(), mailFrom, null, "Gatekeeper: ["+request.getId()+"] Your temporary credentials for role " + roleType.getDbRole(), "credentials", contentMap);

        }catch(Exception e){
            logger.error("Error sending the team an email",e);
        }

    }

    public void notifyAdminsOfFailure(AccessRequest request, Throwable exception){
        logger.info("Notify Gatekeeper Admins that an exception was tossed trying to grant Access for " +  request);
        try {
            String stacktrace = constructStackTrace(exception);
            Map<String,Object> param = new HashMap<>();
            param.put("stacktrace", stacktrace);
            Map<String, Object> contentMap = new HashMap<String,Object>();
            contentMap.put("request", request);
            emailService.sendEmailWithAttachment(teamEmail, mailFrom,null, "Failure executing process", "failure",contentMap, "Exception.txt","exception",  param, "text/plain");
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
