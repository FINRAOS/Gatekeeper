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

import org.finra.gatekeeper.services.aws.Ec2LookupService;
import org.finra.gatekeeper.services.aws.model.AWSEnvironment;
import org.finra.gatekeeper.services.aws.model.GatekeeperAWSInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller providing basic gatekeeper services
 */

@RestController
public class LookupController {

    private static final Logger logger = LoggerFactory.getLogger(LookupController.class);

    private final Ec2LookupService ec2LookupService;

    @Autowired
    public LookupController(Ec2LookupService ec2LookupService){
        this.ec2LookupService = ec2LookupService;
    }

    @RequestMapping(value = "/searchAWSInstances", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GatekeeperAWSInstance> searchAWSInstances(@RequestParam("account") String account, @RequestParam("region") String region,
                                                          @RequestParam("searchTag") String searchTag, @RequestParam("platform") String platform, @RequestParam("searchStr") String searchString){
        return ec2LookupService.getInstances(new AWSEnvironment(account.toUpperCase(), region), platform, searchTag.toUpperCase(), searchString.toUpperCase());
    }

}
