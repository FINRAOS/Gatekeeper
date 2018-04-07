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

package org.finra.gatekeeper.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties used for authentication
 */

@Component
@ConfigurationProperties(prefix="gatekeeper.auth")
public class GatekeeperAuthProperties {
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
     * The header containing the authenticated users email
     */
    private String userEmailHeader;

    /**
     * The header containing the authenticated users full name
     */
    private String userFullNameHeader;

    /**
     * The header to look for the user's LDAP groups (should be supplied by SSO mechanism
     */
    private String userMembershipsHeader;

    /**
     * This is a regex to extract user memberships from the header
     */
    private String userMembershipsPattern;

    /**
     * Ldap Specific settings go here
     */
    private GatekeeperLdapProperties ldap;

    public String getApproverGroup() {
        return approverGroup;
    }

    public GatekeeperAuthProperties setApproverGroup(String approverGroup) {
        this.approverGroup = approverGroup;
        return this;
    }

    public String getSupportGroup() {
        return supportGroup;
    }

    public GatekeeperAuthProperties setSupportGroup(String supportGroup) {
        this.supportGroup = supportGroup;
        return this;
    }

    public String getTestAdminAccount() {
        return testAdminAccount;
    }

    public GatekeeperAuthProperties setTestAdminAccount(String testAdminAccount) {
        this.testAdminAccount = testAdminAccount;
        return this;
    }

    public String getUserIdHeader() {
        return userIdHeader;
    }

    public GatekeeperAuthProperties setUserIdHeader(String userIdHeader) {
        this.userIdHeader = userIdHeader;
        return this;
    }

    public GatekeeperLdapProperties getLdap() {
        return ldap;
    }

    public GatekeeperAuthProperties setLdap(GatekeeperLdapProperties ldap) {
        this.ldap = ldap;
        return this;
    }

    public String getUserEmailHeader() {
        return userEmailHeader;
    }

    public GatekeeperAuthProperties setUserEmailHeader(String userEmailHeader) {
        this.userEmailHeader = userEmailHeader;
        return this;
    }

    public String getUserFullNameHeader() {
        return userFullNameHeader;
    }

    public GatekeeperAuthProperties setUserFullNameHeader(String userFullNameHeader) {
        this.userFullNameHeader = userFullNameHeader;
        return this;
    }

    public String getUserMembershipsHeader() {
        return userMembershipsHeader;
    }

    public GatekeeperAuthProperties setUserMembershipsHeader(String userMembershipsHeader) {
        this.userMembershipsHeader = userMembershipsHeader;
        return this;
    }

    public String getUserMembershipsPattern() {
        return userMembershipsPattern;
    }

    public GatekeeperAuthProperties setUserMembershipsPattern(String userMembershipsPattern) {
        this.userMembershipsPattern = userMembershipsPattern;
        return this;
    }

    public static class GatekeeperLdapProperties {
        /**
         * Is this LDAP configuration Active Directory-based?
         */
        private Boolean isActiveDirectory;

        /**
         * The ObjectClass value to in which to search will pull from
         */
        private String objectClass;
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
         * The base location for ldap
         */
        private String base;

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
        private String usersDnAttribute;

        public Boolean getIsActiveDirectory() {
            return isActiveDirectory;
        }

        public GatekeeperLdapProperties setIsActiveDirectory(Boolean activeDirectory) {
            isActiveDirectory = activeDirectory;
            return this;
        }

        public String getPattern() {
            return pattern;
        }

        public GatekeeperLdapProperties setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public String getObjectClass() {
            return objectClass;
        }

        public GatekeeperLdapProperties setObjectClass(String objectClass) {
            this.objectClass = objectClass;
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

        public String getBase() {
            return base;
        }

        public GatekeeperLdapProperties setBase(String base) {
            this.base = base;
            return this;
        }

        public String getUsersBase() {
            return usersBase;
        }

        public GatekeeperLdapProperties setUsersBase(String usersBase) {
            this.usersBase = usersBase;
            return this;
        }

        public String getTestUsersBase() {
            return testUsersBase;
        }

        public GatekeeperLdapProperties setTestUsersBase(String testUsersBase) {
            this.testUsersBase = testUsersBase;
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

        public String getUsersDnAttribute() {
            return usersDnAttribute;
        }

        public GatekeeperLdapProperties setUsersDnAttribute(String usersDnAttribute) {
            this.usersDnAttribute = usersDnAttribute;
            return this;
        }
    }
}