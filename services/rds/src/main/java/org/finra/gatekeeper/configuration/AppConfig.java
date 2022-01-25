/*
 *
 *  Copyright 2022. Gatekeeper Contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.finra.gatekeeper.configuration;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.finra.gatekeeper.rds.interfaces.GKUserCredentialsProvider;
import org.finra.gatekeeper.services.db.EnvVarDBCredentialsService;
import org.finra.gatekeeper.services.email.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Configuration file used to set up beans like ClientConfiguration
 * or any other AWS clients such as S3, SQS, etc.
 */
@Component
public class AppConfig implements ApplicationContextAware {
    private final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private ApplicationContext context;

    @Bean
    public freemarker.template.Configuration freemarkerConfig() {
        Configuration configuration = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_29);
        configuration.setClassForTemplateLoading(EmailService.class, "/emails");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return configuration;
    }

    @Bean(name="credentialsProvider")
    public GKUserCredentialsProvider credentialsProviderConfig(GatekeeperProperties gatekeeperProperties){
        logger.info("************** CREDENTIALS PROVIDER SETUP **************");
        String credentialProvider = gatekeeperProperties.getDb().getGkCredentialProvider();
        if(credentialProvider == null){
            logger.info("No credentials provider was provided, using default environmental credentials provider.");
            return new EnvVarDBCredentialsService(gatekeeperProperties);
        }
        logger.info("Using provided credential provider: " + credentialProvider);
        return (GKUserCredentialsProvider) context.getBean(credentialProvider);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
