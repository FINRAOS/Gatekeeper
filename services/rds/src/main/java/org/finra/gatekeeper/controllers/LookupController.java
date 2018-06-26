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

package org.finra.gatekeeper.controllers;


import org.finra.gatekeeper.services.accessrequest.model.RoleType;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperRDSInstance;
import org.finra.gatekeeper.services.aws.RdsLookupService;
import org.finra.gatekeeper.services.db.model.DbUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Basic controller used to handle requests using
 * a simple service.
 */
@RestController
class LookupController {

    private static final Logger logger = LoggerFactory.getLogger(LookupController.class);

    private final RdsLookupService rdsLookupService;

    @Autowired
    public LookupController(RdsLookupService rdsLookupService) {
        this.rdsLookupService = rdsLookupService;
    }

    @RequestMapping(value = "/searchDBInstances", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GatekeeperRDSInstance> searchRDSInstances(@RequestParam("account") String account, @RequestParam("region") String region,
                                                          @RequestParam("searchText") String searchString){
        return rdsLookupService.getInstances(new AWSEnvironment(account.toUpperCase(), region), searchString.toLowerCase());
    }

    @RequestMapping(value = "/getAvailableSchemas", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<RoleType, List<String>> getAvailableSchemas(@RequestParam("account") String account, @RequestParam("region") String region,
                                                           @RequestParam("instanceId") String instanceId) throws Exception{
        return rdsLookupService.getSchemasForInstance(new AWSEnvironment(account.toUpperCase(), region), instanceId);
    }

    @RequestMapping(value="/getUsers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DbUser> getUsersForInstance(@RequestParam("account") String account,
                                            @RequestParam("region") String region,
                                            @RequestParam("instanceName") String instanceName) throws Exception{
        return rdsLookupService.getUsersForInstance(new AWSEnvironment(account.toUpperCase(), region), instanceName);

    }
}
