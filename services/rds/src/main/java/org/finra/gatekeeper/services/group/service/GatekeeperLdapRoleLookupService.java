package org.finra.gatekeeper.services.group.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.auth.GatekeeperAuthorizationService;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.common.services.user.search.GatekeeperLdapLookupService;
import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.group.interfaces.IGatekeeperRoleLookupService;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GatekeeperLdapRoleLookupService implements IGatekeeperRoleLookupService {
    //active directory specific filter for finding nested group membership
    private static final String LDAP_MATCHING_RULE_IN_CHAIN = "1.2.840.113556.1.4.1941";


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
    private final String ldapUserName;
    private final String ldapUserId;
    private final String ldapObjectClass;

    private final GatekeeperAuthorizationService gatekeeperAuthorizationService;


    private final Logger logger = LoggerFactory.getLogger(GatekeeperLdapLookupService.class);

    @Autowired
    public GatekeeperLdapRoleLookupService(LdapTemplate ldapTemplate, GatekeeperAuthProperties gatekeeperAuthProperties, GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties, GatekeeperAuthorizationService gatekeeperAuthorizationService) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = gatekeeperAuthProperties.getLdap();
        this.rdsAuthProperties = gatekeeperRdsAuthProperties;
        this.gatekeeperAuthorizationService = gatekeeperAuthorizationService;
        this.ldapUserId = ldapProperties.getUsersIdAttribute();
        this.ldapUserName = ldapProperties.getUsersNameAttribute();
        this.ldapObjectClass = ldapProperties.getObjectClass();
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
    /**
     * @Param takes in the name of an AD group of the following form: APP_GK_<AGS>_<GK_ROLE>_<SDLC>
     * @return returns an array of 3 strings, the AGS, the GK Role, and the SDLC
     */
    public String[] parseADGroups(String ADgroup){
        if(ADgroup == null){
            return null;
        }
        //TODO: Make ENV Variable
        String regex = "APP_GK_([A-Z]{2,8})_(RO|DF|DBA|ROC|DBAC)_(Q|D|P)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ADgroup);

        if(!matcher.find()){
            return new String[] {"","",""};
        }

        return new String[] {matcher.group(1),matcher.group(2),matcher.group(3)};
    }

    private class GroupAttributeMapper implements AttributesMapper<Set<GatekeeperADGroupEntry>> {
        @Override
        public Set<GatekeeperADGroupEntry> mapFromAttributes(Attributes attributes) throws NamingException {
            //TODO: make this an env variable
            Attribute nameAttr = attributes.get("memberOf");
            Set<GatekeeperADGroupEntry> gkEntries = new HashSet<>();
            while(nameAttr.size() > 0){
                String name = nameAttr != null ? ((String) nameAttr.get()).toUpperCase() : null;
                String[] parsedAttributes = parseADGroups(name);
                nameAttr.remove(0);
                if(!parsedAttributes[0].equals("")){
                    gkEntries.add(new GatekeeperADGroupEntry(parsedAttributes[0], parsedAttributes[1], parsedAttributes[2], name));
                }
            }



            return gkEntries;
        }

    }

}
