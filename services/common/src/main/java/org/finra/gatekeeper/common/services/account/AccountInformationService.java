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

package org.finra.gatekeeper.common.services.account;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.finra.gatekeeper.common.services.account.model.Account;
import org.finra.gatekeeper.common.services.backend2backend.Backend2BackendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service which does backend calls
 */
@Component
public class AccountInformationService {

    private final Logger logger = LoggerFactory.getLogger(AccountInformationService.class);
    @Value("${gatekeeper.accountInfoEndpoint}")
    private String accountInforUrl;

    @Value("${gatekeeper.accountInfoUri}")
    private String accountInfoUri;

    private final Backend2BackendService backend2backendService;

    @Autowired
    public AccountInformationService(Backend2BackendService backend2BackendService){
        this.backend2backendService = backend2BackendService;
    }

    /* Accounts -> Account Details Cache, do not want to hit Account Information API every time. */
    private final LoadingCache<String, List<Account>> accountCache = CacheBuilder.newBuilder()
            .maximumSize(1)
            .concurrencyLevel(10)
            .refreshAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, List<Account>>() {
                @Override
                public List<Account> load(String account) throws Exception {
                    return loadAccounts();
                }
            });

    public List<Account> getAccounts(){
        return accountCache.getUnchecked(accountInfoUri);
    }

    public Account getAccountByAlias(String alias){
        List<Account> result =  accountCache.getUnchecked(accountInfoUri)
                .stream()
                .filter(account -> account.getAlias().equalsIgnoreCase(alias))
                .collect(Collectors.toList());

        return !result.isEmpty() ? result.get(0) : null;
    }

    private List<Account> loadAccounts(){
        logger.info("Getting Account information from " + accountInforUrl + accountInfoUri);
        return Arrays.asList(backend2backendService.makeGetCall(accountInforUrl, accountInfoUri, true, Account[].class));
    }

}