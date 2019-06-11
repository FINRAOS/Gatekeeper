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

package org.finra.gatekeeper.services.aws.factory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for creating amazon clients
 */
@Component
public class AwsSessionFactory {

    private final ClientConfiguration clientConfiguration;

    @Autowired
    public AwsSessionFactory(ClientConfiguration clientConfiguration){
        this.clientConfiguration = clientConfiguration;
    }

    public AmazonEC2 createEc2Session(BasicSessionCredentials basicSessionCredentials, String region){
        return AmazonEC2ClientBuilder
                .standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(setCredentials(basicSessionCredentials))
                .build();
    }

    public AWSSimpleSystemsManagement createSsmSession(BasicSessionCredentials basicSessionCredentials, String region){
        return AWSSimpleSystemsManagementClientBuilder
                .standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(setCredentials(basicSessionCredentials))
                .build();
    }

    public AmazonSNS createSnsSession(){
         return AmazonSNSClientBuilder
                 .standard()
                 .build();
    }

    private AWSStaticCredentialsProvider setCredentials(BasicSessionCredentials basicSessionCredentials){
        return new AWSStaticCredentialsProvider(basicSessionCredentials);
    }

}
