package org.finra.gatekeeper.services.aws;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.PublishResult;
import org.finra.gatekeeper.configuration.GatekeeperRdsProperties;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the Gatekeeper RDS Access Request Service
 */
@RunWith(MockitoJUnitRunner.class)
public class SnsServiceTest {

    @InjectMocks
    private SnsService snsService;

    @Mock
    private AwsSessionService awsSessionService;

    @Mock
    private GatekeeperRdsProperties gatekeeperRdsProperties;

    @Mock
    private AmazonSNS mockSnsClient;

    private AccessRequest accessRequest;

    private String MOCK_SNS_TOPIC_ARN = "arn:aws:sns:us-east-1:000000000000:TEST-GATEKEEPER-TPM-EMAIL-dev";
    private int RETRY_COUNT = 3;
    private int RETRY_INTERVAL_IN_MILLIS = 1000;
    private int RETRY_INTERVAL_MULTIPLIER = 1;

    @Before
    public void init() {
        accessRequest = new AccessRequest(
                1,
                "TOOLS-DEV",
                "us-east-1",
                "dev",
                "00000",
                "Test Name",
                "Test.Email@finra.org",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                "Every reason in the world",
                "Sure",
                "00001",
                "Test Approver"
        );
        Mockito.when(gatekeeperRdsProperties.getSnsApprovalTopic()).thenReturn(MOCK_SNS_TOPIC_ARN);
        Mockito.when(gatekeeperRdsProperties.getRetryCount()).thenReturn(RETRY_COUNT);
        Mockito.when(gatekeeperRdsProperties.getRetryIntervalMillis()).thenReturn(RETRY_INTERVAL_IN_MILLIS);
        Mockito.when(gatekeeperRdsProperties.getRetryIntervalMultiplier()).thenReturn(RETRY_INTERVAL_MULTIPLIER);
        Mockito.when(awsSessionService.getSNSSession()).thenReturn(mockSnsClient);
    }

    @Test
    public void testPushToSNSTopic() throws Exception {
        Mockito.when(mockSnsClient.publish(Mockito.any())).thenReturn(new PublishResult().withMessageId("successid"));
        boolean result = snsService.pushToSNSTopic(accessRequest);
        Assert.assertTrue(result);
        verify(mockSnsClient, times(1)).publish(Mockito.any());
    }

    @Test
    public void testSNSTopicNotSet() throws Exception {
        Mockito.when(gatekeeperRdsProperties.getSnsApprovalTopic()).thenReturn(null);
        boolean result = snsService.pushToSNSTopic(accessRequest);
        Assert.assertFalse(result);
        verify(mockSnsClient, times(0)).publish(Mockito.any());
    }

    @Test(expected=GatekeeperException.class)
    public void testSNSTopicPublishException() throws Exception {
        Mockito.when(mockSnsClient.publish(Mockito.any())).thenThrow(AmazonSNSException.class);
        boolean result = snsService.pushToSNSTopic(accessRequest);
    }

    @Test(expected=GatekeeperException.class)
    public void testSNSSessionBuildException() throws Exception {
        Mockito.when(awsSessionService.getSNSSession()).thenThrow(Exception.class);
        boolean result = snsService.pushToSNSTopic(accessRequest);
    }
}
