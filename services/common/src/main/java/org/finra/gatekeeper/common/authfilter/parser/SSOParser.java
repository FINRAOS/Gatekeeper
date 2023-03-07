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
    private final String emailHeader;
    private final String userNameHeader;

    public SSOParser(String userIdHeader, String groupsHeader, String emailHeader, String userNameHeader){
        this.userIdHeader = userIdHeader;
        this.userNameHeader = userNameHeader;
        this.emailHeader = emailHeader;
        this.groupsHeader = groupsHeader;

    }

    public static final String SOURCE_NAME = "SSO";

    @Override
    public Optional<IGatekeeperUserProfile> parse(HttpServletRequest req) {
        Optional<IGatekeeperUserProfile> userProfile = Optional.empty();
        String userId = req.getHeader(userIdHeader);
        if (userId != null) {
            if(groupsHeader != null && userNameHeader != null && emailHeader != null) {
                String name = req.getHeader(userNameHeader);
                String email = req.getHeader(emailHeader);
                Set<String> groups = new HashSet<>(Collections.list(req.getHeaders(groupsHeader)));

                userProfile = Optional.of(new GatekeeperUserProfile(userId, name, email, groups, SOURCE_NAME));

            } else{
                userProfile = Optional.of(new GatekeeperUserProfile(userId, SOURCE_NAME));
            }
        }

        return userProfile;
    }
}
