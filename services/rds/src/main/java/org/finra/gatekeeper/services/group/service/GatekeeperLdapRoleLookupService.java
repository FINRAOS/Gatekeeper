package org.finra.gatekeeper.services.group.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.search.GatekeeperLdapLookupService;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.services.group.interfaces.IGatekeeperRoleLookupService;
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
public class GatekeeperLdapRoleLookupService implements IGatekeeperRoleLookupService {

    private LoadingCache<String,Optional<Map<String, Set<GatekeeperADGroupEntry>>>> ldapRoleApplicationCache = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .concurrencyLevel(10)
            .expireAfterWrite(60L, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<Map<String, Set<GatekeeperADGroupEntry>>>>() {
                public Optional<Map<String, Set<GatekeeperADGroupEntry>>> load(String userId) throws Exception {
                    return Optional.ofNullable(loadRoles(userId));
                }
            });


    private  final LdapTemplate ldapTemplate;
    private final GatekeeperAuthProperties.GatekeeperLdapProperties ldapProperties;
    private final GatekeeperRdsAuthProperties rdsAuthProperties;
    private final GatekeeperLdapParseService gatekeeperLdapParseService;
    private final String ldapUserId;



    private final Logger logger = LoggerFactory.getLogger(GatekeeperLdapLookupService.class);

    @Autowired
    public GatekeeperLdapRoleLookupService(LdapTemplate ldapTemplate, GatekeeperAuthProperties gatekeeperAuthProperties, GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties, GatekeeperLdapParseService gatekeeperLdapParseService) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = gatekeeperAuthProperties.getLdap();
        this.rdsAuthProperties = gatekeeperRdsAuthProperties;
        this.gatekeeperLdapParseService = gatekeeperLdapParseService;
        this.ldapUserId = ldapProperties.getUsersIdAttribute();
    }

    @Override
    public Map<String, Set<GatekeeperADGroupEntry>> loadRoles(String userId) {
        logger.info("Loading AD User Roles for " + userId);
        Map<String, Set<GatekeeperADGroupEntry>> groupMap = new HashMap<String, Set<GatekeeperADGroupEntry>>();
        List<Set<GatekeeperADGroupEntry>> listofGroupSets =
            ldapTemplate.search(
                LdapQueryBuilder.query()
                    .base("OU=Users,OU=Locations")
                    .countLimit(1000)
                    .where(ldapUserId)
                    .like(userId)
                    , getAttributesMapper()
            );

        if(listofGroupSets.size() > 1){
            logger.warn("More than 1 user returned for ID: " + userId);
        }
        Set<GatekeeperADGroupEntry> groupSet = listofGroupSets.get(0);

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


    public Map<String, Set<GatekeeperADGroupEntry>> getLdapAdRoles(String userId){
        Optional<Map<String, Set<GatekeeperADGroupEntry>>> cache = ldapRoleApplicationCache.getUnchecked(userId);
        if(cache.isPresent()){
            return cache.get();
        }
        return null;
    }

    protected AttributesMapper<Set<GatekeeperADGroupEntry>> getAttributesMapper(){
        return new GroupAttributeMapper();
    }

    private class GroupAttributeMapper implements AttributesMapper<Set<GatekeeperADGroupEntry>> {
        @Override
        public Set<GatekeeperADGroupEntry> mapFromAttributes(Attributes attributes) throws NamingException {
            Attribute nameAttr = attributes.get("memberOf");
            Set<GatekeeperADGroupEntry> gkEntries = new HashSet<>();
            while(nameAttr.size() > 0){
                String name = nameAttr != null ? ((String) nameAttr.get()).toUpperCase() : null;
                String[] parsedAttributes = gatekeeperLdapParseService.parseADGroups(name);
                nameAttr.remove(0);
                if(!parsedAttributes[0].equals("")){
                    gkEntries.add(new GatekeeperADGroupEntry(parsedAttributes[0], parsedAttributes[1], parsedAttributes[2], parsedAttributes[3]));
                }
            }



            return gkEntries;
        }

    }

}
