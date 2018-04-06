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

package org.finra.gatekeeper.common.services.user.search;

import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.interfaces.IGatekeeperUserLookupService;
import org.finra.gatekeeper.common.services.user.model.GatekeeperSearchUserEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;


/**
 * An Ldap service primarily used to just determine whether or not the user is in the groups
 * that give them read/write access to the DynamoDB table. Used by custom mapper to add
 * authorities.
 */
@Component
public class GatekeeperLdapLookupService implements IGatekeeperUserLookupService {

    private final LdapTemplate ldapTemplate;

    private final GatekeeperAuthProperties.GatekeeperLdapProperties ldapProperties;
    private final String ldapObjectClass;
    private final String ldapUserId;
    private final String ldapUserDn;
    private final String ldapUserEmail;
    private final String ldapUserName;

    private final Logger logger = LoggerFactory.getLogger(GatekeeperLdapLookupService.class);

    @Autowired
    public GatekeeperLdapLookupService(LdapTemplate ldapTemplate,
                                       GatekeeperAuthProperties gatekeeperAuthProperties) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = gatekeeperAuthProperties.getLdap();
        this.ldapObjectClass = ldapProperties.getObjectClass();
        this.ldapUserId = ldapProperties.getUsersIdAttribute();
        this.ldapUserDn = ldapProperties.getUsersDnAttribute();
        this.ldapUserEmail = ldapProperties.getUsersEmailAttribute();
        this.ldapUserName = ldapProperties.getUsersNameAttribute();
    }

    @Override
    public List<GatekeeperSearchUserEntry> searchForUsers(String queryStr){
        logger.info("Searching for users matching "+queryStr);
        return ldapTemplate.search(
                LdapQueryBuilder.query()
                        .base(ldapProperties.getUsersBase())
                        .countLimit(10)
                        .searchScope(SearchScope.SUBTREE)
                        .where("objectClass")
                        .is(ldapObjectClass)
                        .and(LdapQueryBuilder.query()
                                .where(ldapUserId)
                                .like("*"+queryStr+"*")
                                .or(ldapUserName)
                                .like("*"+queryStr+"*")), getAttributesMapper());
    }

    protected AttributesMapper<GatekeeperSearchUserEntry> getAttributesMapper(){
        return new UserAttributeMapper();
    }

    private class UserAttributeMapper implements AttributesMapper<GatekeeperSearchUserEntry> {
        @Override
        public GatekeeperSearchUserEntry mapFromAttributes(Attributes attributes) throws NamingException {
            Attribute idAttr    =   attributes.get(ldapUserId);
            Attribute mailAttr  =   attributes.get(ldapUserEmail);
            Attribute nameAttr  =   attributes.get(ldapUserName);

            String id   =   idAttr      != null ? ((String) idAttr.get()).toLowerCase() : null;
            String mail =   mailAttr    != null ? (String) mailAttr.get() : null;
            String name =   nameAttr    != null ? (String) nameAttr.get() : null;


            return new GatekeeperSearchUserEntry(id, mail, name);
        }
    }
}
