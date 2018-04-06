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

package org.finra.gatekeeper.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="gatekeeper")
public class GatekeeperProperties {

    public AuthenticationProperties getAuth() {
        return auth;
    }

    public GatekeeperProperties setAuth(AuthenticationProperties auth) {
        this.auth = auth;
        return this;
    }

    /**
     * Properties used for authentication
     */
    private AuthenticationProperties auth;

    public static class AuthenticationProperties {
        /**
         * The Group name for the Approvers
         */
        private String approverGroup;
        /**
         * The Group name for the Support team
         */
        private String supportGroup;

        /**
         * Test account name, leave blank for production environments
         */
        private String testAdminAccount;

        /**
         * The header to look for the user's ID (authentication mechanism should inject this header)
         */
        private String userIdHeader;

        /**
         * Ldap Specific settings
         */
        private GatekeeperLdapProperties ldap;

        public String getApproverGroup() {
            return approverGroup;
        }

        public AuthenticationProperties setApproverGroup(String approverGroup) {
            this.approverGroup = approverGroup;
            return this;
        }

        public String getSupportGroup() {
            return supportGroup;
        }

        public AuthenticationProperties setSupportGroup(String supportGroup) {
            this.supportGroup = supportGroup;
            return this;
        }

        public String getTestAdminAccount() {
            return testAdminAccount;
        }

        public AuthenticationProperties setTestAdminAccount(String testAdminAccount) {
            this.testAdminAccount = testAdminAccount;
            return this;
        }

        public GatekeeperLdapProperties getLdap() {
            return ldap;
        }

        public AuthenticationProperties setLdap(GatekeeperLdapProperties ldap) {
            this.ldap = ldap;
            return this;
        }

        public String getUserIdHeader() {
            return userIdHeader;
        }

        public AuthenticationProperties setUserIdHeader(String userIdHeader) {
            this.userIdHeader = userIdHeader;
            return this;
        }

        public static class GatekeeperLdapProperties {
            /**
             * The regex pattern at which to capture the name of a particular group
             */
            private String pattern;

            /**
             * The base where all the application groups are definied
             */
            private String groupsBase;

            /**
             * If AWS enabled groups are different from the base groups then set this, otherwise the groupsBase will be used.
             */
            private String awsGroupsBase;

            /**
             * The base where the system will search for users
             */
            private String usersBase;

            /**
             * The base where to search for test users (could be the same as the users base, but sometimes it could be different)
             */
            private String testUsersBase;

            /**
             * The LDAP Attribute for the user's cn
             */
            private String usersCnAttribute;

            /**
             * The LDAP Attribute to get the user's login id
             */

            private String usersIdAttribute;

            /**
             * The LDAP Attribute to get the user's name
             */
            private String usersNameAttribute;

            /**
             * The LDAP Attribute to get the user's email
             */
            private String usersEmailAttribute;

            /**
             * The Distinguished Name for the user
             */
            private String distinguishedName;


            public String getPattern() {
                return pattern;
            }

            public GatekeeperLdapProperties setPattern(String pattern) {
                this.pattern = pattern;
                return this;
            }

            public String getGroupsBase() {
                return groupsBase;
            }

            public GatekeeperLdapProperties setGroupsBase(String groupsBase) {
                this.groupsBase = groupsBase;
                return this;
            }

            public String getAwsGroupsBase() {
                return awsGroupsBase;
            }

            public GatekeeperLdapProperties setAwsGroupsBase(String awsGroupsBase) {
                this.awsGroupsBase = awsGroupsBase;
                return this;
            }

            public String getTestUsersBase() {
                return testUsersBase;
            }

            public GatekeeperLdapProperties setTestUsersBase(String testUsersBase) {
                this.testUsersBase = testUsersBase;
                return this;
            }

            public String getUsersBase() {
                return usersBase;
            }

            public GatekeeperLdapProperties setUsersBase(String usersBase) {
                this.usersBase = usersBase;
                return this;
            }

            public String getUsersCnAttribute() {
                return usersCnAttribute;
            }

            public GatekeeperLdapProperties setUsersCnAttribute(String usersCnAttribute) {
                this.usersCnAttribute = usersCnAttribute;
                return this;
            }

            public String getUsersIdAttribute() {
                return usersIdAttribute;
            }

            public GatekeeperLdapProperties setUsersIdAttribute(String usersIdAttribute) {
                this.usersIdAttribute = usersIdAttribute;
                return this;
            }

            public String getUsersNameAttribute() {
                return usersNameAttribute;
            }

            public GatekeeperLdapProperties setUsersNameAttribute(String usersNameAttribute) {
                this.usersNameAttribute = usersNameAttribute;
                return this;
            }

            public String getUsersEmailAttribute() {
                return usersEmailAttribute;
            }

            public GatekeeperLdapProperties setUsersEmailAttribute(String usersEmailAttribute) {
                this.usersEmailAttribute = usersEmailAttribute;
                return this;
            }

            public String getDistinguishedName() {
                return distinguishedName;
            }

            public GatekeeperLdapProperties setDistinguishedName(String distinguishedName) {
                this.distinguishedName = distinguishedName;
                return this;
            }
        }
    }

    public String getAccountInfoEndpoint() {
        return accountInfoEndpoint;
    }

    public GatekeeperProperties setAccountInfoEndpoint(String accountInfoEndpoint) {
        this.accountInfoEndpoint = accountInfoEndpoint;
        return this;
    }

    /**
     * API where AWS account info is provided
     */
    private String accountInfoEndpoint;
}
