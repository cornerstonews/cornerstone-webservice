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
package com.github.cornerstonews.webservice.configuration.parser;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.cornerstonews.webservice.configuration.ConfigException;

public final class ConfigFactory {

    private static Logger log = LogManager.getLogger(ConfigFactory.class); 
    
    private ConfigFactory() {
        throw new IllegalStateException("ConfigFactory class");
    }
    
    public static <T> ConfigParser<T> getDefaultParser(Class<T> clazz) throws ConfigException {
        return getParser(getConfigPath(null), clazz);
    }

    public static <T> ConfigParser<T> getParser(String path, Class<T> clazz) {

        ConfigParser<T> configurationParser = new YamlConfigParser<>(clazz);
        
        if (path != null && path.toLowerCase().endsWith("json")) {
            configurationParser = new JsonConfigParser<>(clazz);
        }

        return configurationParser;
    }

    public static String getConfigPath(String propertyKey) throws ConfigException {
        String path = null;
        Path filePath = null;

        // Read property key from command line
        if(propertyKey != null) {
            path = System.getProperty(propertyKey);
            if(path != null) {
                filePath = Paths.get(path);
            }
        }

        if(filePath != null && Files.isReadable(filePath) && Files.isRegularFile(filePath)) {
            return path;
        }

        return getConfigPath();
    }

    public static String getConfigPath() throws ConfigException {
        String path = null;

        // Load config file from classpath
        URL pathUrl = Thread.currentThread().getContextClassLoader().getResource("application.yaml");
        if (pathUrl == null) {
            pathUrl = Thread.currentThread().getContextClassLoader().getResource("application.yml");
        }

        if (pathUrl == null) {
            pathUrl = Thread.currentThread().getContextClassLoader().getResource("application.json");
        }

        if (pathUrl != null && !pathUrl.getFile().isEmpty()) {
            path = pathUrl.getFile();
        }

        // override config path if 'APPCONFIG' environment variable is set
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            String envPath = (String) env.lookup("APPCONFIG");
            if (envPath != null && !envPath.isEmpty()) {
                path = envPath;
            }
        } catch (NameNotFoundException | NoInitialContextException e) {
            log.info("'APPCONFIG' property not found.");
            // Ignore error if 'APPCONFIG' variable is not found.
        } catch (NamingException ex) {
            ArrayList<String> errors = new ArrayList<>();
            errors.add(ex.getMessage());
            throw new ConfigException("Environment variable 'APPCONFIG'", errors, ex);
        }

        return path;
    }
}
