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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class FakeAccountServiceApplication {

	private final AccountConfiguration accountConfiguration;

	@Autowired
	public FakeAccountServiceApplication(AccountConfiguration accountConfiguration){
		this.accountConfiguration = accountConfiguration;
	}

	public static void main(String[] args) {
		SpringApplication.run(FakeAccountServiceApplication.class, args);
	}

	@GetMapping(value = "/accounts",  produces = MediaType.APPLICATION_JSON_VALUE)
	public List<AccountConfiguration.Account> getAccounts(){
		return accountConfiguration.getAccounts();
	}
}
