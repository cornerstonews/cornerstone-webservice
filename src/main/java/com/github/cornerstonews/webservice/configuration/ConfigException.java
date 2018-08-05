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
package com.github.cornerstonews.webservice.configuration;

import java.util.Collection;

public class ConfigException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected static final String NEWLINE = String.format("%n");

    private final Collection<String> errors;

    /**
     * Creates a new ConfigurationException for the given path with the given errors.
     *
     * @param path
     *            the bad configuration path
     * @param errors
     *            the errors in the path
     */
    public ConfigException(String path, Collection<String> errors) {
        super(formatMessage(path, errors));
        this.errors = errors;
    }

    /**
     * Creates a new ConfigurationException for the given path with the given errors and cause.
     *
     * @param path
     *            the bad configuration path
     * @param errors
     *            the errors in the path
     * @param cause
     *            the cause of the error(s)
     */
    public ConfigException(String path, Collection<String> errors, Throwable cause) {
        super(formatMessage(path, errors), cause);
        this.errors = errors;
    }

    public Collection<String> getErrors() {
        return errors;
    }

    protected static String formatMessage(String file, Collection<String> errors) {
        final StringBuilder msg = new StringBuilder(file);
        msg.append(errors.size() == 1 ? " has an error:" : " has the following errors:").append(NEWLINE);
        for (String error : errors) {
            msg.append("  * ").append(error).append(NEWLINE);
        }
        return msg.toString();
    }
}
