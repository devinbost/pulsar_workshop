/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.example.pulsarworkshop;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class LookupService {

    public LookupService() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
    }

    public String lookup(String encryptionKeyName) {

        // read from the database the public key
        // return null in case the user doesn't exist anymore
        if (encryptionKeyName.equals("billybob")) {
            return null;
        }
        // in prod, we'd be looking up the key from a secure store.

        return "qO4KFETfxi0vLaFw6r+CLo6feaPG2USmcDCBADDfM40=";
    }
    public String encrypt(String input, String encryptionKeyName)
            throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        //String to SecretKeySpec
        byte[] aesByte = Base64.getDecoder().decode(lookup(encryptionKeyName));
        SecretKeySpec mAesKey = new SecretKeySpec(aesByte, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, mAesKey);
        cipher.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] ciphertext = cipher.doFinal();
        // Use it to encrypt values.
        return Base64.getEncoder().encodeToString(ciphertext);
    }
    public String decryptField(String input, String encryptionKeyName)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        var decoded = Base64.getDecoder().decode(input);
        Cipher cipher = Cipher.getInstance("AES");
        byte[] aesByte = Base64.getDecoder().decode(lookup(encryptionKeyName));
        SecretKeySpec mAesKey = new SecretKeySpec(aesByte, "AES");
        cipher.init(Cipher.DECRYPT_MODE, mAesKey);
        cipher.update(decoded);
        byte[] decrypted = cipher.doFinal();

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}