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

package org.finra.gatekeeper.rds.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class RdsQuery {
    /**
     * The name of the AWS account
     */
    private String account;

    /**
     * The AWS account ID
     */
    private String accountId;

    /**
     * The AWS account's region
     */
    private String region;

    /**
     * the AWS account's SDLC
     */
    private String sdlc;

    /**
     * The Database's address
     */
    private String address;

    /**
     * The Database's Instance name
     */
    private String dbInstanceName;

    /**
     * The Engine the DB uses
     */
    private String dbEngine;

    /**
     * Whether RDS IAM Auth is Enabled
     */
    private boolean rdsIAMAuth;

    public String getAccount() {
        return account;
    }

    public RdsQuery withAccount(String account) {
        this.account = account;
        return this;
    }

    public String getAccountId() {
        return accountId;
    }

    public RdsQuery withAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public RdsQuery withRegion(String region) {
        this.region = region;
        return this;
    }

    public String getSdlc() {
        return sdlc;
    }

    public RdsQuery withSdlc(String sdlc) {
        this.sdlc = sdlc;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public RdsQuery withAddress(String address) {
        this.address = address;
        return this;
    }

    public String getDbInstanceName() {
        return dbInstanceName;
    }

    public RdsQuery withDbInstanceName(String dbName) {
        this.dbInstanceName = dbName;
        return this;
    }

    public String getDbEngine() {
        return dbEngine;
    }

    public RdsQuery withDbEngine(String dbEngine) {
        this.dbEngine = dbEngine;
        return this;
    }

    public boolean isRdsIAMAuth() {
        return rdsIAMAuth;
    }

    public RdsQuery withRdsIAMAuth(boolean rdsIAMAuth) {
        this.rdsIAMAuth = rdsIAMAuth;
        return this;
    }

    public RdsQuery(String account, String accountId, String region, String sdlc, String address, String dbInstanceName, String dbEngine, boolean rdsIAMAuth) {
        this.account = account;
        this.accountId = accountId;
        this.region = region;
        this.sdlc = sdlc;
        this.address = address;
        this.dbInstanceName = dbInstanceName;
        this.dbEngine = dbEngine;
        this.rdsIAMAuth = rdsIAMAuth;
    }

    public RdsQuery() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("account", account)
                .add("accountId", accountId)
                .add("region", region)
                .add("sdlc", sdlc)
                .add("address", address)
                .add("dbInstanceName", dbInstanceName)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RdsQuery)) return false;
        RdsQuery rdsQuery = (RdsQuery) o;
        return Objects.equal(account, rdsQuery.account) &&
                Objects.equal(accountId, rdsQuery.accountId) &&
                Objects.equal(region, rdsQuery.region) &&
                Objects.equal(sdlc, rdsQuery.sdlc) &&
                Objects.equal(address, rdsQuery.address) &&
                Objects.equal(dbInstanceName, rdsQuery.dbInstanceName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(account, accountId, region, sdlc, address, dbInstanceName);
    }
}
