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
package org.finra.gatekeeper.services.group.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.search.GatekeeperLdapLookupService;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.services.group.interfaces.IGatekeeperGroupLookupService;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
public class GatekeeperLdapGroupLookupService implements IGatekeeperGroupLookupService {

    private LoadingCache<String,Optional<Map<String, Set<GatekeeperADGroupEntry>>>> ldapGroupDbaApplicationCache = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .concurrencyLevel(10)
            .expireAfterWrite(60L, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<Map<String, Set<GatekeeperADGroupEntry>>>>() {
                public Optional<Map<String, Set<GatekeeperADGroupEntry>>> load(String groups) throws Exception {
                    return Optional.ofNullable(loadGroups());
                }
            });


    private  final LdapTemplate ldapTemplate;
    private final GatekeeperProperties.AuthenticationProperties.GatekeeperLdapProperties ldapProperties;
    private final GatekeeperRdsAuthProperties rdsAuthProperties;
    private final GatekeeperLdapParseService gatekeeperLdapParseService;
    private final Logger logger = LoggerFactory.getLogger(GatekeeperLdapLookupService.class);

    @Autowired
    public GatekeeperLdapGroupLookupService(LdapTemplate ldapTemplate, GatekeeperProperties gatekeeperProperties, GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties, GatekeeperLdapParseService gatekeeperLdapParseService) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = gatekeeperProperties.getAuth().getLdap();
        this.rdsAuthProperties = gatekeeperRdsAuthProperties;
        this.gatekeeperLdapParseService = gatekeeperLdapParseService;
    }

    @Override
    public Map<String, Set<GatekeeperADGroupEntry>> loadGroups() {
        logger.info("Loading AD Groups");
        Map<String, Set<GatekeeperADGroupEntry>> groupMap = new HashMap<String, Set<GatekeeperADGroupEntry>>();
        Set<GatekeeperADGroupEntry> groupSet = new HashSet<GatekeeperADGroupEntry>(
        ldapTemplate.search(
                LdapQueryBuilder.query()
                .base(ldapProperties.getRestrictedGroupsBase())
                        .countLimit(1000)
                        .where("name")
                        .like(rdsAuthProperties.getRestrictedPrefix() + "*"), getAttributesMapper()
        ));


        for(GatekeeperADGroupEntry g : groupSet){
            char sdlc = g.getSdlc().toCharArray()[0];

            //If the SDLC tag is marked as unrestricted we don't need to store it
            boolean enabled = true;
            for (char sdlcFilter : rdsAuthProperties.getUnrestrictedSDLC()){
                if(sdlc == sdlcFilter){
                    enabled = false;
                }
            }
            if(enabled) {
                String application = g.getApplication();
                if (groupMap.get(application) == null) {
                    Set<GatekeeperADGroupEntry> input = new HashSet<GatekeeperADGroupEntry>();
                    input.add(g);
                    groupMap.put(application, input);
                } else {
                    groupMap.get(application).add(g);
                }
            }
        }
        return groupMap;
    }


    public Map<String, Set<GatekeeperADGroupEntry>> getLdapAdGroups(){
        Optional<Map<String, Set<GatekeeperADGroupEntry>>> cache = ldapGroupDbaApplicationCache.getUnchecked("groups");
        if(cache.isPresent()){
            return cache.get();
        }
        return null;
    }

    protected AttributesMapper<GatekeeperADGroupEntry> getAttributesMapper(){
        return new GroupAttributeMapper();
    }

    private class GroupAttributeMapper implements AttributesMapper<GatekeeperADGroupEntry> {
        @Override
        public GatekeeperADGroupEntry mapFromAttributes(Attributes attributes) throws NamingException {
            Attribute nameAttr = attributes.get("name");
            String name = nameAttr != null ? ((String) nameAttr.get()).toUpperCase() : null;
            String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups(name);

            return new GatekeeperADGroupEntry(parsedAttributes[0], parsedAttributes[1], parsedAttributes[2], name);
        }

    }
}
