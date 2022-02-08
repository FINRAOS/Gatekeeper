/*
 * Copyright 2022. Gatekeeper Contributors
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

package org.finra.gatekeeper.services.passwords;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Service class used to generate user passwords
 */

@Component
public class PasswordGenerationService {

    private static final SecureRandom rand = new SecureRandom();

    public String generatePassword() {
        return getStrongPassword(16);
    }

    private String getStrongPassword(int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, null, rand);
    }
}
