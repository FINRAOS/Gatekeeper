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

package org.finra.gatekeeper.services.aws;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.finra.gatekeeper.configuration.properties.GatekeeperSnsProperties;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.accessrequest.model.messaging.dto.RequestEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that handles the AWS connection and does lookups
 */

@Component
public class SnsService {

    private final Logger logger = LoggerFactory.getLogger(SnsService.class);

    private final AwsSessionService awsSessionService;
    private final GatekeeperSnsProperties gatekeeperSnsProperties;

    @Autowired
    public SnsService(AwsSessionService awsSessionService,
                      GatekeeperSnsProperties gatekeeperSnsProperties){

        this.awsSessionService = awsSessionService;
        this.gatekeeperSnsProperties = gatekeeperSnsProperties;
    }

    public boolean isTopicSet(){
        return gatekeeperSnsProperties.getTopicARN() != null;
    }

    public void pushToSNSTopic(RequestEventDTO message) throws Exception {
        if(gatekeeperSnsProperties.getTopicARN() != null){
            pushToSNSTopic(message, gatekeeperSnsProperties.getTopicARN());
        } else {
            logger.info("SNS Topic was not provided, skipping");
        }
    }

    private void pushToSNSTopic(RequestEventDTO message, String topicARN) throws Exception {
        int attempts = gatekeeperSnsProperties.getRetryCount() == -1 ? 5 : gatekeeperSnsProperties.getRetryCount();
        int retryInterval = gatekeeperSnsProperties.getRetryIntervalMillis() == -1 ? 1000 : gatekeeperSnsProperties.getRetryIntervalMillis();
        int retryMultiplier = gatekeeperSnsProperties.getRetryIntervalMultiplier() == -1 ? 1 : gatekeeperSnsProperties.getRetryIntervalMultiplier();

        ObjectWriter jsonWriter = new ObjectMapper().writer();
        logger.info("Pushing " + jsonWriter.withDefaultPrettyPrinter().writeValueAsString(message) + " to " + topicARN);
        String messageId = "";
        do {
            try {
                AmazonSNS snsClient = awsSessionService.getSnsSession();

                Map<String, RequestEventDTO> messagePayload = new HashMap<>();
                messagePayload.put("default", message);
                messageId = snsClient.publish(new PublishRequest()
                        .withTopicArn(topicARN)
                        .withMessage(jsonWriter.writeValueAsString(message))).getMessageId();
                logger.info("SNS Transaction ID: " + messageId);
            } catch (Exception e ) {
                logger.error("Error occurred trying to push to SNS topic. (there are " + attempts + " attempts(s) remaining, next attempt in " + retryInterval +" ms)", e);
                Thread.sleep(retryInterval);
                retryInterval *= retryMultiplier;
            }
        } while ( --attempts > 0 && messageId.isEmpty());

        if(attempts == 0) {
            throw new GatekeeperException("Could not publish the request data to SNS topic. \n" + jsonWriter.withDefaultPrettyPrinter().writeValueAsString(message));
        }
    }
}
