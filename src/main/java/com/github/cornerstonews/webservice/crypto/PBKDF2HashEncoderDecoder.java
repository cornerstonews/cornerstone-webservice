/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cornerstonews.webservice.crypto;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PBKDF2HashEncoderDecoder {

    private static final Logger log = LogManager.getLogger(PBKDF2HashEncoderDecoder.class);

    private static final String ID = "$1409$";
    private static final int SALT_LENGTH = 32;
    private static final int ITERATION_COUNT = 250000;
    private static final int KEY_LENGTH = 512;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

    private PBKDF2HashEncoderDecoder() {
        throw new IllegalStateException("PBKDF2HashEncoderDecoder class");
    }
    
    public static String encode(CharSequence rawPassword) throws GeneralSecurityException {
        return encode(rawPassword, ITERATION_COUNT);
    }

    public static String encode(CharSequence rawPassword, int iterations) throws GeneralSecurityException {
        try {
            char[] password = rawPassword.toString().toCharArray();
            SecureRandom sr = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            sr.nextBytes(salt);

            return ID + toHex(salt) + "$" + iterations + "$" +  toHex(hash(password, salt, iterations));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Failed to encode because PBKDF2HashEncoderDecoder->encode() threw exception: ", e);
            throw e;
        }
    }

    private static byte[] hash(char[] password, byte[] salt, int iterations) throws NoSuchAlgorithmException, InvalidKeySpecException {
        long startTime = System.nanoTime();
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey key = skf.generateSecret(spec);
        byte[] hash = key.getEncoded();
        long endTime = System.nanoTime();
        log.debug("Encode time: {}", TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS));
        return hash;
    }
    
    public static boolean matches(CharSequence rawPassword, CharSequence encodedPassword) {
        try {
            String id = encodedPassword.subSequence(0, 6).toString();
            String error = "Invalid format for encoded password. This password was not encoded by this library.";
            if(! ID.equals(id)) {
                log.warn("{} {}", error, "Id of encoded password does not match.");
                throw new IllegalArgumentException(error);
            }
            
            String ep = encodedPassword.toString().substring(6);
            String[] parts = ep.split("\\$");
            if(parts.length < 3) {
                log.warn("{} {}", error, "Not enough parts in encoded password.");
                throw new IllegalArgumentException(error);
            }
            
            byte[] salt = fromHex(parts[0]);
            int iterations = Integer.parseInt(parts[1]);
            String encodedHash = parts[2];
            char[] password = rawPassword.toString().toCharArray();
            
            String hash = toHex(hash(password, salt, iterations));
            return encodedHash.equalsIgnoreCase(hash);            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Failing matches request because PBKDF2HashEncoderDecoder->matches() threw exception: ", e);
            return false;
        }
    }

    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            String format = "%0" + paddingLength + "d";
            return String.format(format, 0) + hex;
        } else {
            return hex;
        }
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
