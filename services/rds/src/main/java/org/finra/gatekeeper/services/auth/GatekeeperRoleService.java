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

package org.finra.gatekeeper.services.auth;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.auth.GatekeeperAuthorizationService;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.stereotype.Component;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gatekeeper modifications for the Ldap Service
 */
@Component
public class GatekeeperRoleService {

    private final GatekeeperAuthorizationService gatekeeperAuthorizationService;
    private final GatekeeperAuthProperties gatekeeperAuthProperties;

    private static final String DEFAULT_DN = "distinguishedName";
    private static final String DEFAULT_CN = "cn";
    private static final String DEFAULT_PATTERN = "none";

    private final Pattern dbaPattern;
    private final Pattern opsPattern;
    private final Pattern devPattern;

    private LoadingCache<String, Optional<Set<String>>> ldapUserDbaApplicationCache = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .concurrencyLevel(10)
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<Set<String>>>() {
                public Optional<Set<String>> load(String userName) throws Exception {
                    return Optional.ofNullable(loadLdapUserDbaMemberships());
                }
            });

    private LoadingCache<String, Optional<Set<String>>> ldapUserOpsApplicationCache = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .concurrencyLevel(10)
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<Set<String>>>() {
                public Optional<Set<String>> load(String userName) throws Exception {
                    return Optional.ofNullable(loadLdapUserOpsMemberships());
                }
            });
    
    private LoadingCache<String, Optional<Map<String, Set<String>>>> ldapUserDevApplicationCache = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .concurrencyLevel(10)
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<Map<String, Set<String>>>>() {
                public Optional<Map<String, Set<String>>> load(String userName) throws Exception {
                    return Optional.ofNullable(loadLdapUserDevMemberships());
                }
            });
    

    @Autowired
    public GatekeeperRoleService(GatekeeperAuthorizationService gatekeeperAuthorizationService,
                                 GatekeeperAuthProperties gatekeeperAuthProperties,
                                 GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties){
        this.gatekeeperAuthProperties = gatekeeperAuthProperties;
        this.gatekeeperAuthorizationService = gatekeeperAuthorizationService;
        this.dbaPattern = Pattern.compile(gatekeeperRdsAuthProperties.getDbaGroupsPattern());
        this.opsPattern = Pattern.compile(gatekeeperRdsAuthProperties.getOpsGroupsPattern());
        this.devPattern = Pattern.compile(gatekeeperRdsAuthProperties.getDevGroupsPattern());
    }

     public GatekeeperUserEntry getUserProfile(){
        return gatekeeperAuthorizationService.getUser();
    }

    private Set<String> loadLdapUserDbaMemberships(){
        return loadLdapUserMemberships(dbaPattern);
    }

    private Set<String> loadLdapUserOpsMemberships(){
        return loadLdapUserMemberships(opsPattern);
    }

    private Set<String> loadLdapUserMemberships(Pattern pattern){
        Set<String> memberships = new HashSet<>();
        gatekeeperAuthorizationService.getMemberships().forEach((membership) -> {
            Matcher m = pattern.matcher(membership);
            if(m.find()) {
                memberships.add(m.group(1).toUpperCase());
            }
        });

        return memberships;
    }
    
    private Map<String, Set<String>> loadLdapUserDevMemberships(){
        Map<String, Set<String>> memberships = new HashMap<>();
        gatekeeperAuthorizationService.getMemberships().forEach((membership) -> {
            Matcher m = devPattern.matcher(membership);
            if(m.find()) {
                String application = m.group(1);
                String sdlc = m.group(2);
                if(!memberships.containsKey(application)){
                    memberships.put(application, new HashSet<>());
                }
                memberships.get(application).add(sdlc.equals("QC") ? "QA" : sdlc);
            }
        });

        return memberships;
    }

    public Map<String, Set<String>> getDevMemberships(String requestorId){
        return ldapUserDevApplicationCache.getUnchecked(requestorId).get();
    }

    public Map<String, Set<String>> getDevMemberships(){
        return getDevMemberships(getUserProfile().getUserId());
    }

    public Set<String> getDbaMemberships(String requestorId){
        return ldapUserDbaApplicationCache.getUnchecked(requestorId).get();
    }

    public Set<String> getDbaMemberships(){
        return getDbaMemberships(getUserProfile().getUserId());
    }


    public Set<String> getOpsMemberships(String requestorId){
        return ldapUserOpsApplicationCache.getUnchecked(requestorId).get();
    }

    public Set<String> getOpsMemberships(){
        return getOpsMemberships(getUserProfile().getUserId());
    }


    public GatekeeperRdsRole getRole(){
        return checkGatekeeperRdsRole();
    }

    public boolean isApprover(){
        return getRole() == GatekeeperRdsRole.APPROVER;
    }

    private GatekeeperRdsRole checkGatekeeperRdsRole() {
        if(gatekeeperAuthorizationService.getMemberships().contains(gatekeeperAuthProperties.getApproverGroup())) {
            return GatekeeperRdsRole.APPROVER;
        }else if(!getDbaMemberships().isEmpty()){
            return GatekeeperRdsRole.DBA;
        }else if(!getDevMemberships().isEmpty()) {
            return GatekeeperRdsRole.DEV;
        }else if(!getOpsMemberships().isEmpty()) {
            return GatekeeperRdsRole.OPS;
        } else{
            return GatekeeperRdsRole.UNAUTHORIZED;
        }
    }
}
