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

import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperLDAPUserProfile;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class GatekeeperActiveDirectoryLDAPAuthorizationService extends GatekeeperOpenLDAPAuthorizationService {
    //active directory specific filter for finding nested group membership
    private static final String LDAP_MATCHING_RULE_IN_CHAIN = "1.2.840.113556.1.4.1941";

    public GatekeeperActiveDirectoryLDAPAuthorizationService(LdapTemplate ldapTemplate,
                                                             Supplier<IGatekeeperLDAPUserProfile> gatekeeperUserProfileSupplier,
                                                             GatekeeperAuthProperties gatekeeperAuthProperties) {
        super(ldapTemplate,gatekeeperUserProfileSupplier, gatekeeperAuthProperties);
    }

    @Override
    protected Set<String> loadUserMemberships(String userName){
        {
            Optional<GatekeeperUserEntry> user = userCache.getUnchecked(userName);
            String userDn = user.get().getDn();

            LdapQuery memberOfApplication = LdapQueryBuilder.query()
                    .base(ldapUserGroupsBase)
                    .searchScope(SearchScope.SUBTREE)
                    .attributes(ldapUserCn, ldapUserDn)
                    .filter("(member:" + LDAP_MATCHING_RULE_IN_CHAIN + ":=" + userDn + ")");

            return new HashSet<>(ldapTemplate.search(memberOfApplication, getStringAttributesMapper(ldapUserCn)));
        }
    }
}
