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
package org.finra.gatekeeper.common.services.user.auth;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperUserProfile;
import org.finra.gatekeeper.common.services.user.interfaces.IGatekeeperUserAuthorizationService;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class GatekeeperAuthorizationService implements IGatekeeperUserAuthorizationService {
    final Supplier<IGatekeeperUserProfile> gatekeeperUserProfileSupplier;

    /* User Cache */
    final LoadingCache<String, Optional<GatekeeperUserEntry>> userCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .concurrencyLevel(10)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, Optional<GatekeeperUserEntry>>() {
                @Override
                public Optional<GatekeeperUserEntry> load(String userName) throws Exception {
                    return Optional.ofNullable(loadUser(userName));
                }
            });

    /* User -> Application Cache */
    private final LoadingCache<String, Optional<Set<String>>> userMembershipCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .concurrencyLevel(10)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<Set<String>>>() {
                @Override
                public Optional<Set<String>> load(String userName) throws Exception {
                    return Optional.ofNullable(loadUserMemberships(userName));
                }
            });

    GatekeeperAuthorizationService(Supplier<IGatekeeperUserProfile> gatekeeperUserProfileSupplier) {
        this.gatekeeperUserProfileSupplier = gatekeeperUserProfileSupplier;
    }

    public Set<String> getMemberships(){
        return userMembershipCache.getUnchecked(gatekeeperUserProfileSupplier.get().getName()).get();
    }

    public boolean isMember(String applicationName) {
        return getMemberships().contains(applicationName);
    }

    public GatekeeperUserEntry getUser(){
        return userCache.getUnchecked(gatekeeperUserProfileSupplier.get().getName()).get();
    }

    abstract GatekeeperUserEntry loadUser(String userName);

    abstract Set<String> loadUserMemberships(String userName);
}
