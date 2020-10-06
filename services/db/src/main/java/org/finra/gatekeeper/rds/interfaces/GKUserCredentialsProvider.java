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

package org.finra.gatekeeper.rds.interfaces;

import org.finra.gatekeeper.rds.model.RdsQuery;

/**
 * An Interface to provide a custom implementation in which a unique Gatekeeper user's password can be retrieved
 */
public interface GKUserCredentialsProvider {

    /**
     * Get the secret for the gatekeeper accoount
     *
     * @param details - The RDS Query being used.
     * @return - the secret for the given RdsQuery
     */
    public String getGatekeeperSecret(RdsQuery details);
}
