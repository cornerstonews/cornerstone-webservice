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

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptHashEncoderDecoder {

    private static final Logger log = LogManager.getLogger(BCryptHashEncoderDecoder.class);

    //The default log_rounds is 10, and the valid range is 4 to 30.
    private static final int ITERATION_COUNT = 12;

    private BCryptHashEncoderDecoder() {
        throw new IllegalStateException("BCryptHashEncoderDecoder class");
    }
    
    public static String encode(CharSequence rawPassword) {
        return encode(rawPassword, ITERATION_COUNT);
    }
        
    public static String encode(CharSequence rawPassword, int iterations) {
        String salt = BCrypt.gensalt(iterations);
        long startTime = System.nanoTime();
        String hash = BCrypt.hashpw(rawPassword.toString(), salt);
        long endTime = System.nanoTime();
        log.debug("Encode time: {}", TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS));
        return hash;
    }

    public static boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() == 0) {
            log.warn("Empty encoded password");
            return false;
        }

        long startTime = System.nanoTime();
        boolean passwordCheck = BCrypt.checkpw(rawPassword.toString(), encodedPassword);
        long endTime = System.nanoTime();
        log.debug("Encode time: {}", TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS));
        return passwordCheck;
    }
}
