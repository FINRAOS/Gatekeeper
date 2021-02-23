package org.finra.gatekeeper.services.group.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.search.GatekeeperLdapLookupService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final Logger logger = LoggerFactory.getLogger(GatekeeperLdapLookupService.class);

    @Autowired
    public GatekeeperLdapGroupLookupService(LdapTemplate ldapTemplate, GatekeeperAuthProperties gatekeeperAuthProperties) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = gatekeeperAuthProperties.getLdap();
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
            String ags = g.getAGS();
            if(groupMap.get(ags) == null){
                Set<GatekeeperADGroupEntry> input = new HashSet<GatekeeperADGroupEntry>();
                input.add(g);
                groupMap.put(ags, input);
            }
            else{
                groupMap.get(ags).add(g);
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
    /**
     * @Param takes in the name of an AD group of the following form: APP_GK_<AGS>_<GK_ROLE>_<SDLC>
     * @return returns an array of 3 strings, the AGS, the GK Role, and the SDLC
     */
    public String[] parseADGroups(String ADgroup){
        if(ADgroup == null){
            return null;
        }
        String regex = "APP_GK_([A-Z]{2,8})_(RO|DF|DBA|ROC|DBAC)_(Q|D|P)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ADgroup);

        if(!matcher.find()){
            return null;
        }

        return new String[] {matcher.group(1),matcher.group(2),matcher.group(3)};
    }

    private class GroupAttributeMapper implements AttributesMapper<GatekeeperADGroupEntry> {
        @Override
        public GatekeeperADGroupEntry mapFromAttributes(Attributes attributes) throws NamingException {
            //TODO: make this an env variable
            Attribute nameAttr = attributes.get("name");

            String name = nameAttr != null ? ((String) nameAttr.get()).toUpperCase() : null;

            String[] parsedAttributes = parseADGroups(name);

            return new GatekeeperADGroupEntry(parsedAttributes[0], parsedAttributes[1], parsedAttributes[2], name);
        }

    }
}
