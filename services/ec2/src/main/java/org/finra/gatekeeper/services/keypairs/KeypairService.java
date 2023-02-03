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

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.openssl.PEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigInteger;


/**
 * Service that generates RSA Public and Private keypairs.
 *
 */
@Component
public class KeypairService {

    private final Logger logger = LoggerFactory.getLogger(KeypairService.class);

    // returns a generated RSA key pair as a KeyPair object
    public KeyPair createKeypair() {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();
        return keyPair;
    }


    public String getPEM(PrivateKey privKey) {

        StringWriter stringWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(stringWriter);
        try {
            pemWriter.writeObject(privKey);
            pemWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String privKeyString = stringWriter.toString();

        return privKeyString;

    }

    // http://stackoverflow.com/questions/3706177/how-to-generate-ssh-compatible-id-rsa-pub-from-java
    public String getPublicKeyString(PublicKey pubKey) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RSAPublicKey publicKey = (RSAPublicKey) pubKey;

        // encode "ssh-rsa"
        byte[] sshrsa = new byte[] {0,0,0,7,'s','s','h','-','r','s','a'};
        try {
            out.write(sshrsa);

            // encode public exponent
            BigInteger e = publicKey.getPublicExponent();
            byte[] data = e.toByteArray();
            encodeUInt32(data.length, out);
            out.write(data);

            // encode modulus
            BigInteger m = publicKey.getModulus();
            data = m.toByteArray();
            encodeUInt32(data.length, out);
            out.write(data);
            byte[] outArr = out.toByteArray();
            String outString = Base64.encodeBase64String(outArr);
            outString = "ssh-rsa " + outString;
            return outString;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void encodeUInt32(int value, OutputStream out) throws IOException
    {
        byte[] tmp = new byte[4];
        tmp[0] = (byte)((value >>> 24) & 0xff);
        tmp[1] = (byte)((value >>> 16) & 0xff);
        tmp[2] = (byte)((value >>> 8) & 0xff);
        tmp[3] = (byte)(value & 0xff);
        out.write(tmp);
    }

}
