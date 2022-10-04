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

package org.finra.gatekeeper.services.aws;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.sns.AmazonSNS;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.properties.GatekeeperAwsProperties;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.finra.gatekeeper.services.aws.factory.AwsSessionFactory;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Service that provides out AWS Sessions to other gatekeeper Services
 */
@Component
public class AwsSessionService {


    private static final Logger logger = LoggerFactory.getLogger(AwsSessionService.class);

    private final AccountInformationService accountInformationService;
    private final AWSSecurityTokenServiceClient awsSecurityTokenServiceClient;
    private final AwsSessionFactory awsSessionFactory;

    private final Integer sessionTimeout;
    private final Integer sessionTimeoutPad;
    private final String roleToAssume;

    @Autowired
    public AwsSessionService(AccountInformationService accountInformationService,
                             AWSSecurityTokenServiceClient awsSecurityTokenServiceClient,
                             AwsSessionFactory awsSessionFactory,
                             GatekeeperAwsProperties awsProperties) {

        this.accountInformationService = accountInformationService;
        this.awsSecurityTokenServiceClient = awsSecurityTokenServiceClient;
        this.awsSessionFactory = awsSessionFactory;
        this.sessionTimeout = awsProperties.getSessionTimeout();
        this.sessionTimeoutPad = awsProperties.getSessionTimeoutPad();
        this.roleToAssume = awsProperties.getRoleToAssume();

    }

    /* Session Caches */
    private LoadingCache<AWSEnvironment, BasicSessionCredentials> credentialCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .concurrencyLevel(10)
            .refreshAfterWrite(360 * 1000,TimeUnit.MILLISECONDS)
            .build(new CacheLoader<AWSEnvironment,  BasicSessionCredentials>() {
                @Override
                public BasicSessionCredentials load(AWSEnvironment environment) throws Exception {
                    return getFreshCredentials(environment);
                }
            });

    private BasicSessionCredentials getFreshCredentials(AWSEnvironment environment) throws GatekeeperException{

        logger.info("Assuming role for environment " + environment.getAccount() + " on region " + environment.getRegion()
                + " with timeout of " + (sessionTimeout / 1000) + " seconds (with " + (sessionTimeoutPad / 1000) + " padding.)");

        AssumeRoleRequest assumeRequest = new AssumeRoleRequest()
                .withRoleArn(getRoleArn(environment.getAccount()))
                .withDurationSeconds((sessionTimeout + sessionTimeoutPad) / 1000)
                .withRoleSessionName("GATEKEEPER_APP");

        AssumeRoleResult assumeResult = awsSecurityTokenServiceClient.assumeRole(assumeRequest);

        return new BasicSessionCredentials(
                assumeResult.getCredentials().getAccessKeyId(),
                assumeResult.getCredentials().getSecretAccessKey(),
                assumeResult.getCredentials().getSessionToken());

    }

    private String getRoleArn(String alias) throws GatekeeperException {
        Account account = accountInformationService.getAccountByAlias(alias);

        if (account == null) {
            logger.error("No account found with alias: " + alias);
            throw new GatekeeperException("No account found with alias: " + alias);
        }


        account.getAccountId();
        StringBuffer sb = new StringBuffer();
        sb.append("arn:aws:iam::");
        sb.append(account.getAccountId());
        sb.append(":role/");
        sb.append(roleToAssume);
        return sb.toString();
    }

    public AmazonRDSClient getRDSSession(AWSEnvironment environment){
        BasicSessionCredentials creds = credentialCache.getUnchecked(environment);
        AmazonRDSClient rds = awsSessionFactory.createRdsSession(creds);
        rds.setRegion(Region.getRegion(Regions.fromName(environment.getRegion())));
        return rds;
    }

    public AmazonEC2Client getEC2Session(AWSEnvironment environment){
        BasicSessionCredentials creds = credentialCache.getUnchecked(environment);
        AmazonEC2Client ec2 = awsSessionFactory.createEC2Session(creds);
        ec2.setRegion(Region.getRegion(Regions.fromName(environment.getRegion())));
        return ec2;
    }

    public AmazonSNS getSNSSession(){
        return awsSessionFactory.createSNSSession();
    }
    public AWSLambda getAwsLambda(String region){
        return awsSessionFactory.createLambdaSession(region);
    }
    public AmazonSimpleEmailService getSimpleEmailServiceSession(){ return awsSessionFactory.createSimpleEmailServiceSession(); }
}
