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

package org.finra.gatekeeper.services.auth;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.services.user.auth.GatekeeperAuthorizationService;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.properties.GatekeeperProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LDAP Auth Service for Gatekeeper. This thing just checks that you are a member of the DL set at
 * app.adminGroup in the YAML
 */
@Component
public class GatekeeperRoleService {

    private static final String DEFAULT_PATTERN = "none";

    private final GatekeeperProperties.AuthenticationProperties authenticationProperties;
    private final GatekeeperProperties.AuthenticationProperties.GatekeeperLdapProperties ldapProperties;
    private final GatekeeperAuthorizationService gatekeeperAuthorizationService;
    private final Pattern pattern;

    /* User Application Cache */
    private final LoadingCache<String, Optional<Set<String>>> ldapUserApplicationCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .concurrencyLevel(10)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<Set<String>>>() {
                @Override
                public Optional<Set<String>> load(String userName) throws Exception {
                    return Optional.ofNullable(loadUserMemberships(userName));
                }
            });

    @Autowired
    public GatekeeperRoleService(GatekeeperAuthorizationService gatekeeperAuthorizationService, GatekeeperProperties gatekeeperProperties) {
        this.gatekeeperAuthorizationService = gatekeeperAuthorizationService;
        this.authenticationProperties = gatekeeperProperties.getAuth();
        this.ldapProperties = authenticationProperties.getLdap();
        this.pattern = Pattern.compile(ldapProperties.getPattern() != null ? ldapProperties.getPattern() : DEFAULT_PATTERN );
    }

    public GatekeeperUserEntry getUserProfile() {
        return gatekeeperAuthorizationService.getUser();
    }

    public GatekeeperRole getRole() {
        return checkGatekeeperRole();
    }

    public Set<String> getMemberships(){
        return ldapUserApplicationCache.getUnchecked(gatekeeperAuthorizationService.getUser().getUserId()).get();
    }

    public boolean isApprover(){
        return checkGatekeeperRole() == GatekeeperRole.APPROVER;
    }

    /**
     * Check the role for the given user
     *
     * @return the gatekeeper role for the associated user
     */
    private GatekeeperRole checkGatekeeperRole() {
        if(gatekeeperAuthorizationService.getMemberships().contains(authenticationProperties.getApproverGroup())) {
            return GatekeeperRole.APPROVER;
        }else if(gatekeeperAuthorizationService.getMemberships().contains(authenticationProperties.getSupportGroup())){
            return GatekeeperRole.SUPPORT;
        }else{
            //Assume Dev for now.
            return GatekeeperRole.DEV;
        }
    }

    private Set<String> loadUserMemberships(String userName){

        Set<String> memberships = new HashSet<>();
        gatekeeperAuthorizationService.getMemberships().forEach(membership ->{
            Matcher matcher = pattern.matcher(membership);
            if(matcher.find()) {
                memberships.add(matcher.group(1));
            }
        });

        return memberships;
    }
}
