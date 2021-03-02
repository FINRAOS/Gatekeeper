package org.finra.gatekeeper.services.group.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.search.GatekeeperLdapLookupService;
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
    private final GatekeeperAuthProperties.GatekeeperLdapProperties ldapProperties;
    private final GatekeeperRdsAuthProperties rdsAuthProperties;
    private final  GatekeeperLdapParseService gatekeeperLdapParseService;
    private final Logger logger = LoggerFactory.getLogger(GatekeeperLdapLookupService.class);

    @Autowired
    public GatekeeperLdapGroupLookupService(LdapTemplate ldapTemplate, GatekeeperAuthProperties gatekeeperAuthProperties, GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties, GatekeeperLdapParseService gatekeeperLdapParseService) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = gatekeeperAuthProperties.getLdap();
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
                .base("OU=Application_Groups,OU=EmpowerID Managed Groups,OU=Groups")
                        .countLimit(1000)
                        .where("name")
                        .like("APP_GK_*")
                        , getAttributesMapper()
        )
        );


        for(GatekeeperADGroupEntry g : groupSet){
            char sdlc = g.getSDLC().toCharArray()[0];

            //If the SDLC tag is marked as unrestricted we don't need to store it
            boolean enabled = true;
            for (char sdlcFilter : rdsAuthProperties.getUnrestrictedSDLC()){
                if(sdlc == sdlcFilter){
                    enabled = false;
                }
            }
            if(enabled) {
                String ags = g.getAGS();
                if (groupMap.get(ags) == null) {
                    Set<GatekeeperADGroupEntry> input = new HashSet<GatekeeperADGroupEntry>();
                    input.add(g);
                    groupMap.put(ags, input);
                } else {
                    groupMap.get(ags).add(g);
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
