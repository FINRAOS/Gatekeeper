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

package org.finra.gatekeeper.common.authfilter.parser;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SSOParser implements UserParser {

    private final String userIdHeader;
    private final String groupsHeader;

    public SSOParser(String userIdHeader, String groupsHeader){
        this.userIdHeader = userIdHeader;
        this.groupsHeader = groupsHeader;

    }

    public static final String SOURCE_NAME = "SSO";

    @Override
    public Optional<IGatekeeperUserProfile> parse(HttpServletRequest req) {
        Optional<IGatekeeperUserProfile> userProfile = Optional.empty();
        String name = req.getHeader(userIdHeader);
        if (name != null) {
            if(groupsHeader != null) {
                Set<String> groups = new HashSet<>(Collections.list(req.getHeaders(groupsHeader)));
                userProfile = Optional.of(new GatekeeperUserProfile(name, SOURCE_NAME, groups));

            } else{
                userProfile = Optional.of(new GatekeeperUserProfile(name, SOURCE_NAME));
            }
        }

        return userProfile;
    }
}
