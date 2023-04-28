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

package org.finra.gatekeeper.common.services.user.model;

import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperLDAPUserProfile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

/**
 * Capable of supplying a UserProfile instance representing the currently active user.
 */

@Component
public class LDAPUserSupplier implements Supplier<IGatekeeperLDAPUserProfile> {

    /**
     * Supplies a UserProfile instance representing the currently active user.
     *
     * Currently checks if an active HttpServletRequest is available and if so looks up the
     * UserPrincipal contained there.  If the active request does not contain a UserProfile
     * then an IllegalStateException is thrown.
     *
     * If there's no ServletRequestAttribute than throw an IllegalStateException
     *
     * @return the currently active UserProfile
     */
    @Override
    public IGatekeeperLDAPUserProfile get() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            if (request.getUserPrincipal() == null) {
                throw new IllegalStateException("Could not determine user on request");
            }
            return (IGatekeeperLDAPUserProfile) request.getUserPrincipal();
        }

        throw new IllegalStateException("Could not determine user on request");
    }
}
