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

package org.finra.gatekeeper.services.db.factory;

import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.rds.interfaces.DBConnection;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for getting a Database Connection based on a provided Engine String
 */
@Component
public class DatabaseConnectionFactory implements ApplicationContextAware {

    private ApplicationContext context;
    private Map<String, String> supportedDbs;

    @Autowired
    public DatabaseConnectionFactory(GatekeeperProperties gatekeeperProperties){
        this.supportedDbs = gatekeeperProperties.getDb().getSupportedDbs();
    }

    public void setApplicationContext(ApplicationContext applicationContext){
        this.context = applicationContext;
    }

    public DBConnection getConnection(String dbEngine) throws GKUnsupportedDBException{
        String bean = this.supportedDbs.get(dbEngine.toLowerCase());
        if(bean == null){
            throw new GKUnsupportedDBException(dbEngine);
        }
        return (DBConnection) context.getBean(bean);
    }
}
