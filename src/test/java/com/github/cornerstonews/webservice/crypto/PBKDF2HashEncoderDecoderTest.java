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


import java.security.GeneralSecurityException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PBKDF2HashEncoderDecoderTest {

    private final static Logger log = LogManager.getLogger(PBKDF2HashEncoderDecoderTest.class);
    
    private final static String TEST_STRING = "PBKDF2HashEncoderDecoderTest";
    
    @Test
    public void encodeDecodeTest() throws GeneralSecurityException {
        String encodedString = PBKDF2HashEncoderDecoder.encode(TEST_STRING);
        log.debug("Encoded String: {}->{}", encodedString.length(), encodedString);
        Assertions.assertTrue(PBKDF2HashEncoderDecoder.matches(TEST_STRING, encodedString));
    }
    
    @Test
    public void encodeDecodeWithIterationCountTest() throws GeneralSecurityException {
        String encodedString = PBKDF2HashEncoderDecoder.encode(TEST_STRING, 100000);
        log.debug("Encoded String: {}", encodedString);
        Assertions.assertTrue(PBKDF2HashEncoderDecoder.matches(TEST_STRING, encodedString));
    }
}
