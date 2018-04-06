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
import java.util.Optional;

public class SSOParser implements UserParser {

    private final String userIdHeader;

    public SSOParser(String userIdHeader){
        this.userIdHeader = userIdHeader;
    }

    public static final String SOURCE_NAME = "SSO";

    @Override
    public Optional<IGatekeeperUserProfile> parse(HttpServletRequest req) {
        Optional<IGatekeeperUserProfile> userProfile = Optional.empty();
        String name = req.getHeader(userIdHeader);
        if (name != null) {
            userProfile = Optional.of(new GatekeeperUserProfile(name, SOURCE_NAME));
        }
        return userProfile;
    }
}