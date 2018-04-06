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

import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for Account Info Endpoint call.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    private Long accountId;
    private String name;
    private String sdlc;
    private String alias;
    private List<Region> regions;

    public Account(){}
    public Account(Long accountId, String name, String sdlc, String alias, List<Region> regions){
        this.accountId = accountId;
        this.name = name;
        this.sdlc = sdlc;
        this.alias = alias;
        this.regions = regions;
    }


    public Long getAccountId() {
        return accountId;
    }

    public Account setAccountId(Long accountId) {
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

    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        Account that = (Account)o;
        return Objects.equals(this.accountId, that.getAccountId())
                && Objects.equals(this.name, that.getName())
                && Objects.equals(this.sdlc, that.getSdlc())
                && Objects.equals(this.alias, that.getAlias())
                && Objects.equals(this.regions, that.getRegions());
    }

    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("Account Id", accountId)
                .add("Name", name)
                .add("SDLC", sdlc)
                .add("Alias", alias)
                .add("Regions", regions)
                .toString();

    }


}
