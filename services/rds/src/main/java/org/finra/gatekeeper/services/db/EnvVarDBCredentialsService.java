/*
 * Copyright 2019. Gatekeeper Contributors
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
package org.finra.gatekeeper.services.db;

import com.google.common.base.MoreObjects;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.interfaces.GKUserCredentialsProvider;
import org.finra.gatekeeper.rds.model.RdsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class EnvVarDBCredentialsService implements GKUserCredentialsProvider {

    private final Logger logger = LoggerFactory.getLogger(EnvVarDBCredentialsService.class);
    private String secret;

    @Autowired
    public EnvVarDBCredentialsService(GatekeeperProperties gatekeeperProperties){
        this.secret = gatekeeperProperties.getDb().getGkPass();
    }

    @Override
    public String getGatekeeperSecret(RdsQuery rdsQuery) {
        logger.info("Getting Environment Variable based secret" );
        logger.info(MoreObjects.toStringHelper(this)
                .add("account", rdsQuery.getAccount())
                .add("region", rdsQuery.getRegion())
                .add("sdlc", rdsQuery.getSdlc())
                .add("database", rdsQuery.getDbInstanceName()).toString());
        return secret;
    }
}
