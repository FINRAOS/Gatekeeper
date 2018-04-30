/*
 *
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
 */

package org.finra.fakeaccountservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "")
public class AccountConfiguration {
    private List<Account> accounts;

    public List<Account> getAccounts() {
        return accounts;
    }

    public AccountConfiguration setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        return this;
    }

    public AccountConfiguration(){

    }

    public AccountConfiguration(List<Account> accounts){
        this.accounts = accounts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
        private String accountId;
        private String name;
        private String sdlc;
        private String alias;
        private List<Region> regions;

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

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Region {

            private String name;

            public Region setName(String name){
                this.name = name;
                return this;
            }

            public String getName(){
                return this.name;
            }

        }

    }

}
