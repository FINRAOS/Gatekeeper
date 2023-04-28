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
import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperLDAPUserProfile;
import org.finra.gatekeeper.common.services.health.interfaces.DeepHealthCheckItem;
import org.finra.gatekeeper.common.services.health.model.DeepHealthCheckTargetDTO;
import org.finra.gatekeeper.common.services.health.model.enums.DeepHealthCheckTargetStatus;
import org.finra.gatekeeper.common.services.health.model.enums.DependencyCriticality;
import org.finra.gatekeeper.common.services.user.interfaces.IGatekeeperUserAuthorizationService;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

 public abstract class GatekeeperAuthorizationService implements IGatekeeperUserAuthorizationService, DeepHealthCheckItem {

    @Value("${gatekeeper.auth.ldap.server}")
    private String endpoint;
    @Value("${gatekeeper.health.ldapTagValue}")
    private String ldapTag;
    @Value("${gatekeeper.health.ldapAccountCheck}")
    private String ldapAccountCheck;

    final Supplier<IGatekeeperLDAPUserProfile> gatekeeperUserProfileSupplier;

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
    final LoadingCache<String, Optional<Set<String>>> userMembershipCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .concurrencyLevel(10)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<Set<String>>>() {
                @Override
                public Optional<Set<String>> load(String userName) throws Exception {
                    return Optional.ofNullable(loadUserMemberships(userName));
                }
            });

    GatekeeperAuthorizationService(Supplier<IGatekeeperLDAPUserProfile> gatekeeperUserProfileSupplier) {
        this.gatekeeperUserProfileSupplier = gatekeeperUserProfileSupplier;
    }

    public Set<String> getMemberships(){
        return  userMembershipCache.getUnchecked(gatekeeperUserProfileSupplier.get().getUserId()).get();
    }

    public boolean isMember(String applicationName) {
        return getMemberships().contains(applicationName);
    }

    public GatekeeperUserEntry getUser(){
        return userCache.getUnchecked(gatekeeperUserProfileSupplier.get().getUserId()).get();
    }

    public DeepHealthCheckTargetDTO doHealthCheck(){
        DeepHealthCheckTargetDTO deepHealthCheckTargetDTO = new DeepHealthCheckTargetDTO()
                .setApplication(ldapTag)
                .setUri(endpoint)
                .setCategory("User and Group Lookup")
                .setDependencyType(DependencyCriticality.REQUIRED)
                .setDescription("This checks whether Gatekeeper can perform an LDAP lookup.")
                .setComponent("LDAP Connection")
                .setStartTimestamp(LocalDateTime.now().toString());
        try {
            GatekeeperUserEntry user = loadUser(ldapAccountCheck);
            deepHealthCheckTargetDTO.setStatus(DeepHealthCheckTargetStatus.SUCCESS);
        } catch (Exception e) {
            deepHealthCheckTargetDTO.setExceptionMessage(e.getMessage());
            deepHealthCheckTargetDTO.setStatus(DeepHealthCheckTargetStatus.FAILURE);
        }
        return deepHealthCheckTargetDTO
                .setEndTimestamp(LocalDateTime.now().toString());
    }

    abstract GatekeeperUserEntry loadUser(String userName);

    abstract Set<String> loadUserMemberships(String userName);
}
