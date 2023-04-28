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

package org.finra.gatekeeper.common.authfilter;


import org.finra.gatekeeper.common.authfilter.parser.CompositeHeaderParser;
import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperHeaderUserProfile;
import org.finra.gatekeeper.common.authfilter.parser.UserHeaderParser;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

public class UserHeaderHeadersFilter implements Filter {

    private UserHeaderParser userProfileParser;

    private String contentSecurityPolicy;

    private class UserProfileRequestWrapper extends HttpServletRequestWrapper {

        private IGatekeeperHeaderUserProfile userProfile;

        public UserProfileRequestWrapper(HttpServletRequest req, IGatekeeperHeaderUserProfile userProfile) {
            super(req);
            this.userProfile = userProfile;
        }


        @Override
        public Principal getUserPrincipal() {
            return userProfile;
        }
    }

    public UserHeaderHeadersFilter(UserHeaderParser userProfileParser, String contentSecurityPolicy) {
        this.userProfileParser = userProfileParser;
        this.contentSecurityPolicy = contentSecurityPolicy;
    }

    public UserHeaderHeadersFilter(UserHeaderParser userProfileParser) {
        this.userProfileParser = userProfileParser;
    }

    public UserHeaderHeadersFilter(UserHeaderParser... userProfileParsers) {
        this.userProfileParser = new CompositeHeaderParser(userProfileParsers);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;
        if(contentSecurityPolicy != null && !contentSecurityPolicy.equals("")) {
            httpRes.setHeader("Content-Security-Policy",
                    "default-src 'self' 'unsafe-inline' 'unsafe-eval'; " + contentSecurityPolicy);
        } else {
            httpRes.setHeader("Content-Security-Policy",
                    "default-src 'self' 'unsafe-inline' 'unsafe-eval'");
        }
        httpRes.setHeader("X-XSS-Protection",
                "1; mode=block");
        httpRes.setHeader("X-Frame-Options",
                "DENY");
        Optional<IGatekeeperHeaderUserProfile> userProfile = userProfileParser.parse(httpReq);
        if (userProfile.isPresent()) {
            filterChain.doFilter(new UserProfileRequestWrapper(httpReq, userProfile.get()), httpRes);
        } else {
            filterChain.doFilter(httpReq, httpRes);
        }
    }

    @Override
    public void destroy() {

    }
}
