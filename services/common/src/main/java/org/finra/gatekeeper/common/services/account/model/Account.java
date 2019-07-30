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

package org.finra.gatekeeper.common.services.account.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.List;

/**
 * Data Transfer Object for Account Info Endpoint call.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    private String accountId;
    private String name;
    private String sdlc;
    private String alias;
    private List<Region> regions;
    private Integer grouping = 1; // Grouping is to be used to help sort on the UI end.

    public Account(){}
    public Account(String accountId, String name, String sdlc, String alias, List<Region> regions){
        this.accountId = accountId;
        this.name = name;
        this.sdlc = sdlc;
        this.alias = alias;
        this.regions = regions;
    }


    public String getAccountId() {
        return accountId;
    }

    public Account setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }


    public String getName() {
        return name;
    }

    public Account setName(String name) {
        this.name = name;
        return this;
    }


    public String getSdlc() {
        return sdlc;
    }

    public Account setSdlc(String sdlc) {
        this.sdlc = sdlc;
        return this;
    }


    public String getAlias() {
        return alias;
    }

    public Account setAlias(String alias) {
        this.alias = alias;
        return this;
    }


    public List<Region> getRegions() {
        return regions;
    }

    public Account setRegions(List<Region> regions) {
        this.regions = regions;
        return this;
    }

    public Integer getGrouping() {
        return grouping;
    }

    public Account setGrouping(Integer grouping) {
        this.grouping = grouping;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equal(accountId, account.accountId) &&
                Objects.equal(name, account.name) &&
                Objects.equal(sdlc, account.sdlc) &&
                Objects.equal(alias, account.alias) &&
                Objects.equal(regions, account.regions) &&
                Objects.equal(grouping, account.grouping);
    }

    public String toString() {
        return "Account{" +
                "accountId='" + accountId + '\'' +
                ", name='" + name + '\'' +
                ", sdlc='" + sdlc + '\'' +
                ", alias='" + alias + '\'' +
                ", regions=" + regions +
                ", grouping=" + grouping +
                '}';
    }
}
