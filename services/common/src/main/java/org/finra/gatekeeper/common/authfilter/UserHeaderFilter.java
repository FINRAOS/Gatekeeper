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

package org.finra.gatekeeper.common.authfilter;


import org.finra.gatekeeper.common.authfilter.parser.CompositeParser;
import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperUserProfile;
import org.finra.gatekeeper.common.authfilter.parser.UserParser;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

public class UserHeaderFilter implements Filter {

    private UserParser userProfileParser;

    private class UserProfileRequestWrapper extends HttpServletRequestWrapper {

        private IGatekeeperUserProfile userProfile;

        public UserProfileRequestWrapper(HttpServletRequest req, IGatekeeperUserProfile userProfile) {
            super(req);
            this.userProfile = userProfile;
        }

        @Override
        public Principal getUserPrincipal() {
            return userProfile;
        }
    }

    public UserHeaderFilter(UserParser userProfileParser) {
        this.userProfileParser = userProfileParser;
    }

    public UserHeaderFilter(UserParser... userProfileParsers) {
        this.userProfileParser = new CompositeParser(userProfileParsers);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        Optional<IGatekeeperUserProfile> userProfile = userProfileParser.parse(httpReq);
        if (userProfile.isPresent()) {
            filterChain.doFilter(new UserProfileRequestWrapper(httpReq, userProfile.get()), res);
        } else {
            filterChain.doFilter(httpReq, res);
        }
    }

    @Override
    public void destroy() {

    }
}