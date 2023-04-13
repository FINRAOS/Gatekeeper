/*
 * Copyright 2023. Gatekeeper Contributors
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

package org.finra.gatekeeper.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties relating to DynamoDB for gatekeeper
 */
@Component
@ConfigurationProperties(prefix="gatekeeper.dynamodb")
public class GatekeeperDynamoDBProperties {

   private String table;
   private String primaryKey;
   private String tokenKey;
   private String ttlKey;

   public String getTable() {
      return table;
   }

   public GatekeeperDynamoDBProperties setTable(String table) {
      this.table = table;
      return this;
   }

   public String getPrimaryKey() {
      return primaryKey;
   }

   public GatekeeperDynamoDBProperties setPrimaryKey(String primaryKey) {
      this.primaryKey = primaryKey;
      return this;
   }

   public String getTokenKey() {
      return tokenKey;
   }

   public GatekeeperDynamoDBProperties setTokenKey(String tokenKey) {
      this.tokenKey = tokenKey;
      return this;
   }

   public String getTtlKey() {
      return ttlKey;
   }

   public GatekeeperDynamoDBProperties setTtlKey(String ttlKey) {
      this.ttlKey = ttlKey;
      return this;
   }
}
