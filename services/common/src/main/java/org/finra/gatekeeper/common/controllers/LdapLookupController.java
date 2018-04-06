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

package org.finra.gatekeeper.common.controllers;

import org.finra.gatekeeper.common.services.user.model.GatekeeperSearchUserEntry;
import org.finra.gatekeeper.common.services.user.search.GatekeeperLdapLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class LdapLookupController {

    private static final Logger logger = LoggerFactory.getLogger(LdapLookupController.class);

    private final GatekeeperLdapLookupService gatekeeperLdapService;


    @Autowired
    public LdapLookupController(GatekeeperLdapLookupService gatekeeperLdapService){
        this.gatekeeperLdapService = gatekeeperLdapService;
    }

    @RequestMapping(value = "/searchAD", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GatekeeperSearchUserEntry> searchAD(@RequestParam("searchStr") String searchString) {
        if(searchString.isEmpty()){
            logger.error("No Search String provided");
            return new ArrayList<>();
        }else{
            return gatekeeperLdapService.searchForUsers(searchString);
        }
    }

}
