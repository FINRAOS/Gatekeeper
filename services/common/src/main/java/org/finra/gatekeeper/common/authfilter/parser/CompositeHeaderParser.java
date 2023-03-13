/*
 * Copyright 2023. Gatekeeper Contributors
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class CompositeHeaderParser implements UserHeaderParser {

    private static final Logger logger = LoggerFactory.getLogger(UserHeaderParser.class);
    public UserHeaderParser[] userProfileParsers;

    public CompositeHeaderParser(UserHeaderParser... userProfileParsers) {
        if (userProfileParsers == null) {
            throw new NullPointerException(
                    "CompositeLDAPParser must accept at least 1 UserProfileParser");
        } else if (userProfileParsers.length == 0) {
            logger.warn("No UserProfileParser's were found");
        }
        this.userProfileParsers = userProfileParsers;
    }

    @Override
    public Optional<IGatekeeperHeaderUserProfile> parse(HttpServletRequest req) {
        Optional<IGatekeeperHeaderUserProfile> userProfile = Optional.empty();
        for (UserHeaderParser parser : userProfileParsers) {
            userProfile = parser.parse(req);
            if (userProfile.isPresent()) {
                return userProfile;
            }
        }
        return userProfile;
    }
}
