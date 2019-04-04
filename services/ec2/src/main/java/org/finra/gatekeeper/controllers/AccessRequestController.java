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

package org.finra.gatekeeper.controllers;

import org.finra.gatekeeper.common.services.properties.GatekeeperPropertiesService;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.ActiveAccessRequestWrapper;
import org.finra.gatekeeper.controllers.wrappers.CompletedAccessRequestWrapper;

import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.services.accessrequest.AccessRequestService;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequest;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller providing basic gatekeeper services
 */

@RestController
public class AccessRequestController {

    private static final Logger logger = LoggerFactory.getLogger(AccessRequestController.class);

    private final AccessRequestService accessRequestService;
    private final AccountInformationService accountsService;
    private final GatekeeperPropertiesService gatekeeperPropertiesService;

    @Autowired
    public AccessRequestController(AccessRequestService accessRequestService, AccountInformationService accountsService, GatekeeperPropertiesService gatekeeperPropertiesService){
        this.accessRequestService = accessRequestService;
        this.accountsService = accountsService;
        this.gatekeeperPropertiesService = gatekeeperPropertiesService;
    }

    @RequestMapping(value = "/getAccounts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Account> getAccounts() throws Exception {
        return accountsService.getAccounts();
    }

    @RequestMapping(value = "/grantAccess", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public AccessRequest createRequest(@RequestBody AccessRequestWrapper request) throws GatekeeperException {
        //todo sam accountname for the requestor
        return accessRequestService.storeAccessRequest(request);
    }

    @RequestMapping(value = "/getActiveRequests", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ActiveAccessRequestWrapper> getActiveRequests() throws Exception {
        return accessRequestService.getActiveRequests();
    }

    @RequestMapping(value = "/getCompletedRequests", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CompletedAccessRequestWrapper> getCompletedRequests() {
        return accessRequestService.getCompletedRequests();
    }

    @RequestMapping(value = "/approveRequest", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object approveRequest(@RequestBody ActiveAccessRequestWrapper request) {
        return accessRequestService.approveRequest(request.getTaskId(), request.getId(), request.getApproverComments());
    }

    @RequestMapping(value = "/rejectRequest", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object rejectRequest(@RequestBody ActiveAccessRequestWrapper request) {
        return accessRequestService.rejectRequest(request.getTaskId(), request.getId(), request.getApproverComments());
    }

    @RequestMapping(value = "/cancelRequest", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object cancelRequest(@RequestBody ActiveAccessRequestWrapper request) {
        return accessRequestService.cancelRequest(request.getTaskId(), request.getId());
    }

    @RequestMapping(value="/getConfig", method= RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getConfig() {
        return gatekeeperPropertiesService.getJustificationConfig();
    }

}
