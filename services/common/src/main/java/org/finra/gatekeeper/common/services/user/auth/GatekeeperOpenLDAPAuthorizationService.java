/*
 *
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

package org.finra.gatekeeper.common.services.user.auth;

import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperUserProfile;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatekeeperOpenLDAPAuthorizationService extends GatekeeperAuthorizationService {
    private final Logger logger = LoggerFactory.getLogger(GatekeeperOpenLDAPAuthorizationService.class);
    final GatekeeperAuthProperties.GatekeeperLdapProperties ldapProperties;
    final LdapTemplate ldapTemplate;
    final String ldapUserCn;
    final String ldapUserId;
    final String ldapUserDn;
    final String ldapUserEmail;
    final String ldapUserName;
    final String ldapUserGroupsBase;
    final String ldapObjectClass;

    public GatekeeperOpenLDAPAuthorizationService(LdapTemplate ldapTemplate,
                                                             Supplier<IGatekeeperUserProfile> gatekeeperUserProfileSupplier,
                                                             GatekeeperAuthProperties gatekeeperAuthProperties) {
        super(gatekeeperUserProfileSupplier);
        this.ldapProperties = gatekeeperAuthProperties.getLdap();
        this.ldapTemplate = ldapTemplate;
        this.ldapUserCn = ldapProperties.getUsersCnAttribute();
        this.ldapUserId = ldapProperties.getUsersIdAttribute();
        this.ldapUserDn = ldapProperties.getUsersDnAttribute();
        this.ldapObjectClass = ldapProperties.getObjectClass();
        this.ldapUserEmail = ldapProperties.getUsersEmailAttribute();
        this.ldapUserName = ldapProperties.getUsersNameAttribute();
        this.ldapUserGroupsBase = ldapProperties.getAwsGroupsBase() != null ? ldapProperties.getAwsGroupsBase() : ldapProperties.getGroupsBase();

        logger.info("Initialized GatekeeperOpenLDAPAuthorizationService with cn=" + this.ldapUserCn + " id=" + ldapUserId
        + " dn=" + ldapUserDn + " email=" + ldapUserEmail + " name=" + ldapUserName);
    }

    protected Set<String> loadUserMemberships(String userName){
        Pattern cnPattern = Pattern.compile("cn=([- _A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);

        logger.info("Checking Memberships for " +userName );
        Set<String> memberships = new HashSet<>();
        String memberof = "memberOf";
        LdapQuery query = LdapQueryBuilder.query()
                .base(ldapProperties.getUsersBase()).countLimit(1000)
                .searchScope(SearchScope.SUBTREE)
                .attributes(memberof)
                .where("objectClass")
                .is(ldapObjectClass)
                .and(ldapUserId)
                .is(userName);


        LinkedList<String[]> subjects = (LinkedList<String[]>)ldapTemplate.search(query, new OpenLdapMembershipsMapper());

        if (subjects == null || subjects.size() == 0) {
            if(ldapProperties.getTestUsersBase() != null) {
                query = LdapQueryBuilder.query()
                        .base(ldapProperties.getTestUsersBase()).countLimit(1000)
                        .searchScope(SearchScope.SUBTREE)
                        .attributes("memberOf")
                        .where("objectClass")
                        .is(ldapObjectClass)
                        .and(ldapUserId)
                        .is(userName);
                subjects = (LinkedList<String[]>) ldapTemplate.search(query, new OpenLdapMembershipsMapper());
            }
        }

        HashSet<String> extracted = new HashSet<>();

        Arrays.asList(subjects.getFirst()).forEach(item -> {
            Matcher m = cnPattern.matcher(item);
            if(m.find()) {
                extracted.add(m.group(1));
            }
        });

        return extracted;
    }

    protected GatekeeperUserEntry loadUser(String userName){
        logger.info("Loading info for " + userName);
        LdapQuery query = LdapQueryBuilder.query()
                .base(ldapProperties.getUsersBase()).countLimit(1)
                .searchScope(SearchScope.SUBTREE)
                .attributes(ldapUserId, ldapUserDn, ldapUserEmail, ldapUserName)
                .where("objectClass")
                .is(ldapObjectClass)
                .and(ldapUserId)
                .is(userName);
        List<GatekeeperUserEntry> subjects = ldapTemplate.search(query, getAttributesMapper());

        if (subjects != null && subjects.size() > 0) {
            return subjects.get(0);
            //check to see if account is test account (only if testUsersBase is provided)
        } else if(ldapProperties.getTestUsersBase() != null) {
            query = LdapQueryBuilder.query()
                    .base(ldapProperties.getTestUsersBase()).countLimit(1)
                    .searchScope(SearchScope.SUBTREE)
                    .attributes(ldapUserId, ldapUserDn, ldapUserEmail, ldapUserName)
                    .where("objectCategory")
                    .is(ldapObjectClass)
                    .and(ldapUserId)
                    .is(userName);
            subjects = ldapTemplate.search(query, getAttributesMapper());
            //return null;
            if (subjects != null && subjects.size() > 0) {
                return subjects.get(0);
            }
        }
        return null;
    }

    AttributesMapper<GatekeeperUserEntry> getAttributesMapper(){
        return new UserAttributeMapper();
    }

    AttributesMapper<String> getStringAttributesMapper(String attr){
        return new StringAttributeMapper(attr);
    }

    private class UserAttributeMapper implements AttributesMapper<GatekeeperUserEntry> {
        @Override
        public GatekeeperUserEntry mapFromAttributes(Attributes attributes) throws NamingException {
            Attribute idAttr    =   attributes.get(ldapUserId);
            Attribute dnAttr    =   attributes.get(ldapUserDn);
            Attribute mailAttr  =   attributes.get(ldapUserEmail);
            Attribute nameAttr  =   attributes.get(ldapUserName);

            String id   =   idAttr      != null ? ((String) idAttr.get()).toLowerCase() : null;
            String dn   =   dnAttr      != null ? (String) dnAttr.get() : null;
            String mail =   mailAttr    != null ? (String) mailAttr.get() : null;
            String name =   nameAttr    != null ? (String) nameAttr.get() : null;


            return new GatekeeperUserEntry(id, dn, mail, name);
        }
    }

    private class StringAttributeMapper implements AttributesMapper<String> {
        private String attr;
        public StringAttributeMapper(String attr){
            this.attr = attr;
        }
        @Override
        public String mapFromAttributes(Attributes attributes) throws NamingException {
            return (String) attributes.get(attr).get();
        }
    }

    private class OpenLdapMembershipsMapper extends AbstractContextMapper {
        @Override
        protected Object doMapFromContext(DirContextOperations ctx) {
            return ctx.getStringAttributes("memberOf");
        }
    }
}
