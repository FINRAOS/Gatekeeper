/*
 *
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
 */

package org.finra.gatekeeper.common.services.user.auth;

import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperHeaderUserProfile;
import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperLDAPUserProfile;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.function.Supplier;


public class GatekeeperHeaderAuthorizationService extends GatekeeperAuthorizationService {
    private final Logger logger = LoggerFactory.getLogger(GatekeeperHeaderAuthorizationService.class);
    final Supplier<IGatekeeperHeaderUserProfile> gatekeeperUserProfileSupplier;

    public GatekeeperHeaderAuthorizationService(Supplier<IGatekeeperHeaderUserProfile> gatekeeperHeaderUserProfileSupplier) {
        super(null);
        this.gatekeeperUserProfileSupplier = gatekeeperHeaderUserProfileSupplier;
        logger.info("Initialized GatekeeperHeaderAuthorizationService");
    }

    protected Set<String> loadUserMemberships(String userName){
        return gatekeeperUserProfileSupplier.get().getMemberships();
    }

    protected GatekeeperUserEntry loadUser(String userName){
        String name = gatekeeperUserProfileSupplier.get().getName();
        String userId = gatekeeperUserProfileSupplier.get().getUserId();
        String email = gatekeeperUserProfileSupplier.get().getEmail();
        return new GatekeeperUserEntry(userId, null, email, name);
    }

    @Override
    public Set<String> getMemberships(){
        return  userMembershipCache.getUnchecked(gatekeeperUserProfileSupplier.get().getUserId()).get();
    }
    @Override
    public GatekeeperUserEntry getUser(){
        return userCache.getUnchecked(gatekeeperUserProfileSupplier.get().getUserId()).get();
    }
}
