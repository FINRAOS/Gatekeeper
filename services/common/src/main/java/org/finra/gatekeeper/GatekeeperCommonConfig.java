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

package org.finra.gatekeeper;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import org.apache.commons.lang3.StringUtils;
import org.finra.gatekeeper.common.authfilter.parser.IGatekeeperUserProfile;
import org.finra.gatekeeper.common.authfilter.parser.SSOParser;
import org.finra.gatekeeper.common.authfilter.UserHeaderFilter;
import org.finra.gatekeeper.common.properties.GatekeeperAuthProperties;
import org.finra.gatekeeper.common.properties.GatekeeperAwsProperties;
import org.finra.gatekeeper.common.services.user.auth.GatekeeperActiveDirectoryLDAPAuthorizationService;
import org.finra.gatekeeper.common.services.user.auth.GatekeeperAuthorizationService;
import org.finra.gatekeeper.common.services.user.auth.GatekeeperOpenLDAPAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.function.Supplier;

@Component
public class GatekeeperCommonConfig {

    private final Logger logger = LoggerFactory.getLogger(GatekeeperCommonConfig.class);

    private final String userIdHeader;
    private final String userFullNameHeader;
    private final String userEmailHeader;
    private final String userMembershipsHeader;
    private final String userMembershipsPattern;
    private final String userBase;
    private final String base;
    private final String proxyHost;
    private final Integer proxyPort;

    private final GatekeeperAuthProperties gatekeeperAuthProperties;

    @Autowired
    public GatekeeperCommonConfig(GatekeeperAwsProperties gatekeeperAwsProperties,
                                  GatekeeperAuthProperties gatekeeperAuthProperties){
        //AWS
        if(gatekeeperAwsProperties.getProxyHost().equalsIgnoreCase("unset") && !gatekeeperAwsProperties.getProxyHost().equals("${PROXY_HOST}")) {
            logger.info("Setting Proxy Host to " + gatekeeperAwsProperties.getProxyHost());
            this.proxyHost = gatekeeperAwsProperties.getProxyHost();
        }else{
            logger.info("Proxy Host was not provided. So not setting proxyHost");
            this.proxyHost = null;
        }
        //Check to see if a valid integer got passed off to the port (check for mis-configuration/missing configuration)
        //if it is properly configured then set this.proxyPort to the value contained within otheriwse set it to  null.
        if(gatekeeperAwsProperties.getProxyHost().equalsIgnoreCase("unset") && StringUtils.isNumeric(gatekeeperAwsProperties.getProxyPort())) {
            logger.info("Setting Proxy Port to " + gatekeeperAwsProperties.getProxyPort());
            this.proxyPort = Integer.valueOf(gatekeeperAwsProperties.getProxyPort());
        }else{
            logger.info("No valid proxy port configuration found. (was " + gatekeeperAwsProperties.getProxyPort() + "), so not setting proxyPort");
            this.proxyPort = null;
        }

        //LDAP
        this.userIdHeader = gatekeeperAuthProperties.getUserIdHeader();
        this.userFullNameHeader = gatekeeperAuthProperties.getUserFullNameHeader();
        this.userEmailHeader = gatekeeperAuthProperties.getUserEmailHeader();
        this.userMembershipsHeader = gatekeeperAuthProperties.getUserMembershipsHeader();
        this.userMembershipsPattern = gatekeeperAuthProperties.getUserMembershipsPattern();
        this.base = gatekeeperAuthProperties.getLdap().getBase();
        this.userBase = gatekeeperAuthProperties.getLdap().getUsersBase();
        this.gatekeeperAuthProperties = gatekeeperAuthProperties;
    }

    @Bean
    public ClientConfiguration clientConfiguration() {
        final ClientConfiguration clientConfiguration = new ClientConfiguration();
        logger.info("Configuring AWS SDK Client");
        if( this.proxyHost != null && this.proxyPort != null) {
            logger.info("Discovered proxy configurations, setting the proxy inside clientConfiguration");
            clientConfiguration.setProxyHost(this.proxyHost);
            clientConfiguration.setProxyPort(this.proxyPort);
            logger.info("Proxy Host set to: " + this.proxyHost);
            logger.info("Proxy Port set to: " + this.proxyPort);
        }
        return clientConfiguration;

    }

    @Bean
    public AWSSecurityTokenServiceClient awsSecurityTokenServiceClient() {
        return new AWSSecurityTokenServiceClient(clientConfiguration());
    }

    /* Creating UserProfileFilter with order of 0 to ensure it happens first */
    @Bean
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    public FilterRegistrationBean userProfileFilterRegistration() {
        FilterRegistrationBean userProfileFilterRegistration = new FilterRegistrationBean();
        userProfileFilterRegistration.setFilter(new UserHeaderFilter(new SSOParser(userIdHeader)));
        userProfileFilterRegistration.setOrder(0);
        return userProfileFilterRegistration;
    }

    /* Request scoped bean to create autowireable UserProfile object */
    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public IGatekeeperUserProfile userProfile() {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Principal p = req.getUserPrincipal();
        return (IGatekeeperUserProfile) p;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.ldap.context-source")
    public LdapContextSource authContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setBase(userBase);
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    @Bean
    public GatekeeperAuthorizationService gatekeeperLDAPAuthorizationService(LdapTemplate ldapTemplate,
                                                                             Supplier<IGatekeeperUserProfile> gatekeeperUserProfileSupplier){
        //Sets to AD if true
        if(gatekeeperAuthProperties.getLdap().getIsActiveDirectory()) {
            logger.info("Setting Authorization to work with Active Directory");
            return new GatekeeperActiveDirectoryLDAPAuthorizationService(ldapTemplate,
                    gatekeeperUserProfileSupplier,
                    gatekeeperAuthProperties);
        }

        logger.info("Setting Authorization to work with OpenLDAP");
        //Defaults to OpenLDAP otherwise
        return new GatekeeperOpenLDAPAuthorizationService(ldapTemplate,
                    gatekeeperUserProfileSupplier,
                    gatekeeperAuthProperties);
    }

}
