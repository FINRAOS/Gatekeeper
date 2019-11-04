package org.finra.gatekeeper.services.aws;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.finra.gatekeeper.configuration.GatekeeperRdsProperties;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SnsService {

    private final Logger logger = LoggerFactory.getLogger(SnsService.class);

    private final AwsSessionService awsSessionService;
    private final GatekeeperRdsProperties gatekeeperRdsProperties;

    @Autowired
    public SnsService(AwsSessionService awsSessionService,
                      GatekeeperRdsProperties gatekeeperRdsProperties){

        this.awsSessionService = awsSessionService;
        this.gatekeeperRdsProperties = gatekeeperRdsProperties;
    }

    public boolean isTopicSet() {
        return gatekeeperRdsProperties.getSnsApprovalTopic() != null;
    }

    public boolean pushToSNSTopic(AccessRequest accessRequest) throws Exception {
        if(isTopicSet()) {
            pushToSNSTopic(accessRequest, gatekeeperRdsProperties.getSnsApprovalTopic());
            return true;
        }
        return false;
    }

    private void pushToSNSTopic(Object accessRequest, String topicARN) throws Exception {
        int attempts = gatekeeperRdsProperties.getRetryCount() == -1 ? 5 : gatekeeperRdsProperties.getRetryCount();
        int retryInterval = gatekeeperRdsProperties.getRetryIntervalMillis() == -1 ? 1000 : gatekeeperRdsProperties.getRetryIntervalMillis();
        int retryMultiplier = gatekeeperRdsProperties.getRetryIntervalMultiplier() == -1 ? 1 : gatekeeperRdsProperties.getRetryIntervalMultiplier();

        ObjectWriter jsonWriter = new ObjectMapper().writer();
        logger.info("Pushing " + jsonWriter.withDefaultPrettyPrinter().writeValueAsString(accessRequest) + " to " + topicARN);
        String messageId = "";
        do {
            try {
                AmazonSNS snsClient = awsSessionService.getSNSSession();

                messageId = snsClient.publish(new PublishRequest()
                        .withTopicArn(topicARN)
                        .withMessage(jsonWriter.writeValueAsString(accessRequest))).getMessageId();
                logger.info("SNS Transaction ID: " + messageId);
            } catch (Exception e) {
                logger.error("Error occurred trying to push to SNS topic. (there are " + attempts + " attempts(s) remaining, next attempt in " + retryInterval +" ms)", e);
                Thread.sleep(retryInterval);
                retryInterval *= retryMultiplier;
            }
        } while ( --attempts > 0 && messageId.isEmpty());

        if(attempts == 0) {
            throw new GatekeeperException("Could not publish the request data to SNS topic. \n" + jsonWriter.withDefaultPrettyPrinter().writeValueAsString(accessRequest));
        }
    }
}
