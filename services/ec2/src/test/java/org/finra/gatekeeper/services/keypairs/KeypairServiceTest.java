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
 */

package org.finra.gatekeeper.services.keypairs;

import org.junit.Test;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Test for KeyPairService
 */
public class KeypairServiceTest {

    private KeypairService keypairService = new KeypairService();


    // createKeypair() returns a RSA key pair as a KeyPair object
    @Test
    public void testCreateKeypair() {
        KeyPair kp = keypairService.createKeypair();
        assertNotNull(kp);
        assert(kp.getClass().equals(KeyPair.class));

        PrivateKey priv = kp.getPrivate();
        PublicKey pub = kp.getPublic();
        assertNotNull(priv);
        assertNotNull(pub);
        assertEquals(priv.getAlgorithm(), new String("RSA"));
        assertEquals(pub.getAlgorithm(), new String("RSA"));

        KeyPair kp2 = keypairService.createKeypair();
        assertNotEquals(kp, kp2);
    }

    // getPEM() returns the private key as a PEM-encoded string
    @Test
    public void testGetPEM() {
        KeyPair kp = keypairService.createKeypair();
        PrivateKey privateKey = kp.getPrivate();
        String privateKeyString = keypairService.getPEM(privateKey);
        assertNotNull(privateKeyString);
        Pattern pattern = Pattern.compile("-----BEGIN RSA PRIVATE KEY-----\\r?\\n.*-----END RSA PRIVATE KEY-----\\r?\\n",Pattern.DOTALL);
        Matcher regexMatcher = pattern.matcher(privateKeyString);
        assertTrue(regexMatcher.matches());
    }

    // getPublicKeyString() returns the public key as a string
    @Test
    public void testGetPublicKeyString() {
        KeyPair kp = keypairService.createKeypair();
        PublicKey publicKey = kp.getPublic();
        String publicKeyString = keypairService.getPublicKeyString(publicKey);
        assertNotNull(publicKeyString);
        assert(publicKeyString.startsWith("ssh-rsa"));
    }


}