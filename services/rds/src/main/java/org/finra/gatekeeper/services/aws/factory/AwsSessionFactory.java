package org.finra.gatekeeper.services.aws.factory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.rds.AmazonRDSClient;
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
    public AwsSessionFactory(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    public AmazonRDSClient createRdsSession(BasicSessionCredentials basicSessionCredentials){
        return new AmazonRDSClient(basicSessionCredentials, clientConfiguration);
    }

    public AmazonEC2Client createEC2Session(BasicSessionCredentials basicSessionCredentials){
        return new AmazonEC2Client(basicSessionCredentials, clientConfiguration);
    }

    public AmazonSNS createSNSSession(){
        return AmazonSNSClientBuilder
                .standard()
                .build();
    }
    public AWSLambda createLambdaSession(String region){
        return AWSLambdaClientBuilder
                .standard().withRegion(region)
                .build();
    }

}
