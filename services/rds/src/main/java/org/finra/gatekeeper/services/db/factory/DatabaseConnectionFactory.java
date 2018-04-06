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

import org.finra.gatekeeper.services.db.connections.MySQLDBConnection;
import org.finra.gatekeeper.services.db.connections.PostgresDBConnection;
import org.finra.gatekeeper.services.db.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.services.db.interfaces.DBConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Factory for getting a Database Connection based on a provided Engine String
 */
@Component
public class DatabaseConnectionFactory {


    private final PostgresDBConnection postgresDBConnection;
    private final MySQLDBConnection mySQLDBConnection;

    @Autowired
    public DatabaseConnectionFactory(PostgresDBConnection postgresDBConnection,
                                     MySQLDBConnection mySQLDBConnection) {
        this.postgresDBConnection = postgresDBConnection;
        this.mySQLDBConnection = mySQLDBConnection;
    }

    public DBConnection getConnection(String dbEngine) throws GKUnsupportedDBException{
        switch(dbEngine.toLowerCase()){
            case "postgres":
                return postgresDBConnection;
            case "mysql":
                return mySQLDBConnection;
            default:
                throw new GKUnsupportedDBException(dbEngine);
        }
    }
}
