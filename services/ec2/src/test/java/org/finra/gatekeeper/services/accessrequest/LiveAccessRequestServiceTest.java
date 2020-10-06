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

package org.finra.gatekeeper.services.accessrequest;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.NativeHistoricVariableInstanceQuery;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.finra.gatekeeper.services.accessrequest.model.*;
import org.finra.gatekeeper.services.accessrequest.model.messaging.dto.ActiveAccessRequestDTO;
import org.finra.gatekeeper.services.accessrequest.model.messaging.dto.ActiveRequestUserDTO;
import org.finra.gatekeeper.services.accessrequest.model.messaging.dto.RequestEventDTO;
import org.finra.gatekeeper.services.accessrequest.model.messaging.enums.EventType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the Access Request Service
 */

@ActiveProfiles("unit-test")
@RunWith(MockitoJUnitRunner.class)
public class LiveAccessRequestServiceTest {

    @InjectMocks
    private AccessRequestService accessRequestService;
    @Mock
    private AccessRequestRepository accessRequestRepository;
    @Mock
    private HistoryService historyService;
    @Mock
    private AccessRequest ownerRequest;
    @Mock
    private ActiveRequestUserDTO activeRequestUser;
    @Mock
    private HistoricVariableInstanceEntity activitiRequest1;
    @Mock
    private HistoricVariableInstanceEntity activitiRequest2;
    @Mock
    private HistoricVariableInstanceEntity activitiRequest3;
    @Mock
    private NativeHistoricVariableInstanceQuery nativeHistoricVariableInstanceQuery;


    private final String LINUX = "Linux";
    private final String WINDOWS = "Windows";

    private Date testDate;
    private User user1;
    private User user2;
    private AWSInstance instance1;
    private AWSInstance instance2;
    private AWSInstance instance3;
    private AccessRequest request1;
    private AccessRequest request2;
    private AccessRequest request3;

    private List<ActiveAccessRequestDTO> liveLinuxAccessRequests;
    private List<ActiveAccessRequestDTO> liveWindowsAccessRequests;
    private List<ActiveAccessRequestDTO> expiringLinuxAccessRequests;
    private List<ActiveAccessRequestDTO> expiringWindowsAccessRequests;



    @Before
    public void initMocks() {
        setTestVars();
        setMocks();

    }

    @Test
    public void testGetLiveLinuxRequests() {
        List<ActiveAccessRequestDTO> expectedActiveUserOne = Collections.singletonList(new ActiveAccessRequestDTO(request3.getId().toString(), instance3.getName(), instance3.getIp()));

        List<ActiveAccessRequestDTO> expectedActiveUserTwo = Arrays.asList(
                new ActiveAccessRequestDTO(request1.getId().toString(), instance1.getName(), instance1.getIp()),
                new ActiveAccessRequestDTO(request1.getId().toString(), instance2.getName(), instance2.getIp()));

        List<ActiveAccessRequestDTO> expectedExpired = Collections.singletonList(new ActiveAccessRequestDTO(request2.getId().toString(), request2.getInstances().get(0).getName(), request2.getInstances().get(0).getIp()));

        RequestEventDTO liveRequests = accessRequestService.getLiveRequestsForUsersInRequest(EventType.EXPIRATION, request2);
        Assert.assertEquals(liveRequests.getUsers().size(), 2);
        ActiveRequestUserDTO liveRequestUserOne = liveRequests.getUsers().get(0); //testman
        ActiveRequestUserDTO liveRequestUserTwo = liveRequests.getUsers().get(1); //testlady
        Assert.assertEquals(liveRequests.getEventType(), EventType.EXPIRATION.getValue());
        Assert.assertEquals(liveRequests.getRequestId(), request2.getId());

        // testlady
        Assert.assertEquals(expectedActiveUserOne, liveRequestUserOne.getActiveAccess().getLinux());
        Assert.assertEquals(Collections.emptyList(), liveRequestUserOne.getActiveAccess().getWindows());
        Assert.assertEquals(expectedExpired, liveRequestUserOne.getExpiredAccess().getLinux());
        Assert.assertEquals(Collections.emptyList(), liveRequestUserOne.getExpiredAccess().getWindows());
        Assert.assertEquals(user2.getUserId().substring(3), liveRequestUserOne.getUserId());
        Assert.assertEquals(user2.getUserId(), liveRequestUserOne.getGkUserId());
        Assert.assertEquals(user2.getEmail(), liveRequestUserOne.getEmail());

        // testman
        Assert.assertEquals(expectedActiveUserTwo, liveRequestUserTwo.getActiveAccess().getLinux());
        Assert.assertEquals(Collections.emptyList(), liveRequestUserTwo.getActiveAccess().getWindows());
        Assert.assertEquals(expectedExpired, liveRequestUserTwo.getExpiredAccess().getLinux());
        Assert.assertEquals(Collections.emptyList(), liveRequestUserTwo.getExpiredAccess().getWindows());
        Assert.assertEquals(user1.getUserId().substring(3), liveRequestUserTwo.getUserId());
        Assert.assertEquals(user1.getUserId(), liveRequestUserTwo.getGkUserId());
        Assert.assertEquals(user1.getEmail(), liveRequestUserTwo.getEmail());

    }


    @Test
    public void testGetLiveWindowsRequests() {
        Arrays.asList(request1, request2, request3)
                .forEach(item -> item.setPlatform(WINDOWS));
        Arrays.asList(instance1, instance2, instance3)
                .forEach(item -> item.setPlatform(WINDOWS));

        List<ActiveAccessRequestDTO> expectedActiveUserOne = Collections.singletonList(new ActiveAccessRequestDTO(request3.getId().toString(), instance3.getName(), instance3.getIp()));

        List<ActiveAccessRequestDTO> expectedActiveUserTwo = Arrays.asList(
                new ActiveAccessRequestDTO(request1.getId().toString(), instance1.getName(), instance1.getIp()),
                new ActiveAccessRequestDTO(request1.getId().toString(), instance2.getName(), instance2.getIp()));

        List<ActiveAccessRequestDTO> expectedExpired = Collections.singletonList(new ActiveAccessRequestDTO(request2.getId().toString(), request2.getInstances().get(0).getName(), request2.getInstances().get(0).getIp()));

        RequestEventDTO liveRequests = accessRequestService.getLiveRequestsForUsersInRequest(EventType.EXPIRATION, request2);
        Assert.assertEquals(liveRequests.getUsers().size(), 2);
        ActiveRequestUserDTO liveRequestUserOne = liveRequests.getUsers().get(0); //testman
        ActiveRequestUserDTO liveRequestUserTwo = liveRequests.getUsers().get(1); //testlady
        Assert.assertEquals(liveRequests.getEventType(), EventType.EXPIRATION.getValue());
        Assert.assertEquals(liveRequests.getRequestId(), request2.getId());

        // testlady
        Assert.assertEquals(expectedActiveUserOne, liveRequestUserOne.getActiveAccess().getWindows());
        Assert.assertEquals(Collections.emptyList(), liveRequestUserOne.getActiveAccess().getLinux());
        Assert.assertEquals(expectedExpired, liveRequestUserOne.getExpiredAccess().getWindows());
        Assert.assertEquals(Collections.emptyList(), liveRequestUserOne.getExpiredAccess().getLinux());
        Assert.assertEquals(user2.getUserId().substring(3), liveRequestUserOne.getUserId());
        Assert.assertEquals(user2.getUserId(), liveRequestUserOne.getGkUserId());
        Assert.assertEquals(user2.getEmail(), liveRequestUserOne.getEmail());

        // testman
        Assert.assertEquals(expectedActiveUserTwo, liveRequestUserTwo.getActiveAccess().getWindows());
        Assert.assertEquals(Collections.emptyList(), liveRequestUserTwo.getActiveAccess().getLinux());
        Assert.assertEquals(expectedExpired, liveRequestUserTwo.getExpiredAccess().getWindows());
        Assert.assertEquals(Collections.emptyList(), liveRequestUserTwo.getExpiredAccess().getLinux());
        Assert.assertEquals(user1.getUserId().substring(3), liveRequestUserTwo.getUserId());
        Assert.assertEquals(user1.getUserId(), liveRequestUserTwo.getGkUserId());
        Assert.assertEquals(user1.getEmail(), liveRequestUserTwo.getEmail());
    }

    private void setTestVars(){
        testDate = new Date();
        List<HistoricVariableInstance> taskVars = new ArrayList<>();

        user1 = new User().setUserId("gk-testman").setName("testman").setEmail("testman@email.com");
        user2 = new User().setUserId("gk-testlady").setName("testlady").setEmail("testlady@email.com");

        instance1 = new AWSInstance().setInstanceId("12345").setIp("1.2.3.4").setApplication("HELLO").setName("test").setPlatform(LINUX).setStatus("ONLINE");
        instance2 = new AWSInstance().setInstanceId("54321").setIp("111.222.333.444").setApplication("Goodbye").setName("test2").setPlatform(LINUX).setStatus("ONLINE");
        instance3 = new AWSInstance().setInstanceId("9999").setIp("999.888.777.666").setApplication("Bonjour").setName("test3").setPlatform(LINUX).setStatus("ONLINE");

        request1 = new AccessRequest().setId(1L).setHours(20).setInstances(Arrays.asList(instance1, instance2)).setUsers(Arrays.asList(user1));
        request2 = new AccessRequest().setId(2L).setHours(5).setInstances(Arrays.asList(instance2)).setUsers(Arrays.asList(user1, user2));
        request3 = new AccessRequest().setId(3L).setHours(2).setInstances(Arrays.asList(instance3)).setUsers(Arrays.asList(user2));
    }

    private void setMocks(){
        when(activitiRequest1.getTextValue2()).thenReturn("1");
        when(activitiRequest2.getTextValue2()).thenReturn("2");
        when(activitiRequest3.getTextValue2()).thenReturn("3");
        when(activitiRequest1.getTextValue()).thenReturn("APPROVAL_GRANTED");
        when(activitiRequest2.getTextValue()).thenReturn("GRANTED");
        when(activitiRequest3.getTextValue()).thenReturn("GRANTED");
        Calendar expireTime = Calendar.getInstance();
        expireTime.setTime(testDate);
        expireTime.add(Calendar.HOUR, -200);
        when(activitiRequest1.getLastUpdatedTime()).thenReturn(testDate);
        when(activitiRequest2.getLastUpdatedTime()).thenReturn(expireTime.getTime());
        when(activitiRequest3.getLastUpdatedTime()).thenReturn(testDate);

        when(historyService.createNativeHistoricVariableInstanceQuery()).thenReturn(nativeHistoricVariableInstanceQuery);
        when(nativeHistoricVariableInstanceQuery.sql(Mockito.any())).thenReturn(nativeHistoricVariableInstanceQuery);
        when(historyService.createNativeHistoricVariableInstanceQuery()
                .list())
                .thenReturn(Arrays.asList(activitiRequest1, activitiRequest2, activitiRequest3));

        when(accessRequestRepository.getAccessRequestsByIdIn(Mockito.anyCollection()))
                .thenReturn(Arrays.asList(request1, request2, request3));
    }
}
