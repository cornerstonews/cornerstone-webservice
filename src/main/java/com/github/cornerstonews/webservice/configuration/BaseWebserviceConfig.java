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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BaseWebserviceConfig {

    @Valid
    @NotNull
    private boolean registerDefaults = true;
    
    @Valid
    @NotNull
    private String adminResourceRole = "";
    
    @Valid
    @NotNull
    private String monitorResourceRole = "";
    
    @Valid
    @NotNull
    private boolean registerCORSFilter = true;

    @Valid
    private CORSFilter corsFilter;
    
    @Valid
    @NotNull
    private boolean registerCSRFFilter = false;
    
    @Valid
    @NotNull(message="must not be null. jwtToken.secretKey must be defined.")
    private JWTToken jwtToken;
    
    @Valid
    @NotNull
    private boolean registerServerProperties = true;

    @Valid
    @NotNull
    private boolean enableAuthenticationAndRoles = true;

    @Valid
    @NotNull
    private String rateUnit = "seconds";
    
    @Valid
    @NotNull
    private String durationUnit = "seconds";
    
    private List<String> authWhitelistedURIs;
    
    private List<Pattern> authWhitelistedURIPatterns;
    
    // ***********************************************************************************************
    
    public static class CORSFilter {
        
        @NotNull
        private String origin = "*";
        
        @NotNull
        private String headers = "origin, content-type, accept, authorization";
        
        @NotNull
        private String credentials = "true";
        
        @NotNull
        private String methods = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
        
        public String getOrigin() {
            return origin;
        }
        
        public void setOrigin(String origin) {
            this.origin = origin;
        }
        
        public String getHeaders() {
            return headers;
        }
        
        public void setHeaders(String headers) {
            this.headers = headers;
        }
        
        public String getCredentials() {
            return credentials;
        }
        
        public void setCredentials(String credentials) {
            this.credentials = credentials;
        }
        
        public String getMethods() {
            return methods;
        }
        
        public void setMethods(String methods) {
            this.methods = methods;
        }
    }

    public static class JWTToken {
        
        @NotNull
        private String secretKey = null;

        @NotNull
        private int expirationInSeconds = 600;

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public int getExpirationInSeconds() {
            return expirationInSeconds;
        }

        public void setExpirationInSeconds(int expirationInSeconds) {
            this.expirationInSeconds = expirationInSeconds;
        }
    }
    
    // ***********************************************************************************************
    // Setters and Getters

    public boolean isRegisterDefaults() {
        return registerDefaults;
    }

    public void setRegisterDefaults(boolean registerDefaults) {
        this.registerDefaults = registerDefaults;
    }    

    public String getAdminResourceRole() {
        return adminResourceRole;
    }

    public void setAdminResourceRole(String adminResourceRole) {
        this.adminResourceRole = adminResourceRole;
    }

    public String getMonitorResourceRole() {
        return monitorResourceRole;
    }

    public void setMonitorResourceRole(String monitorResourceRole) {
        this.monitorResourceRole = monitorResourceRole;
    }
    
    public boolean isRegisterCSRFFilter() {
        return registerCSRFFilter;
    }

    public void setRegisterCSRFFilter(boolean registerCSRFFilter) {
        this.registerCSRFFilter = registerCSRFFilter;
    }

    public boolean isRegisterCORSFilter() {
        return registerCORSFilter;
    }

    public void setRegisterCORSFilter(boolean registerCORSFilter) {
        this.registerCORSFilter = registerCORSFilter;
    }

    public CORSFilter getCorsFilter() {
        if(corsFilter == null) {
            corsFilter = new CORSFilter();
        }
        return corsFilter;
    }

    public void setCorsFilter(CORSFilter corsFilter) {
        this.corsFilter = corsFilter;
    }
    
    public JWTToken getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(JWTToken jwtToken) {
        this.jwtToken = jwtToken;
    }

    public boolean isRegisterServerProperties() {
        return registerServerProperties;
    }

    public void setRegisterServerProperties(boolean registerServerProperties) {
        this.registerServerProperties = registerServerProperties;
    }

    public boolean isEnableAuthenticationAndRoles() {
        return enableAuthenticationAndRoles;
    }

    public void setEnableAuthenticationAndRoles(boolean enableAuthenticationAndRoles) {
        this.enableAuthenticationAndRoles = enableAuthenticationAndRoles;
    }

    public String getRateUnit() {
        return rateUnit;
    }

    public void setRateUnit(String rateUnit) {
        this.rateUnit = rateUnit;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }
    
    public List<String> getAuthWhitelistedURIs() {
        return authWhitelistedURIs;
    }

    public void setAuthWhitelistedURIs(List<String> authWhitelistedURIs) {
        this.authWhitelistedURIs = authWhitelistedURIs;
        this.authWhitelistedURIPatterns = convertStringUriToPattern(this.getAuthWhitelistedURIs());
    }
    
    public List<Pattern> getAuthWhitelistedURIPatterns() {
        if(this.authWhitelistedURIPatterns == null) {
            this.authWhitelistedURIPatterns = convertStringUriToPattern(this.getAuthWhitelistedURIs());
        }
        return authWhitelistedURIPatterns;
    }

    public void setAuthWhitelistedURIPatterns(List<Pattern> authWhitelistedURIPatterns) {
        this.authWhitelistedURIPatterns = authWhitelistedURIPatterns;
    }

    private List<Pattern> convertStringUriToPattern(List<String> authWhitelistedURIs) {
        if(authWhitelistedURIs == null) {
            return null;
        }
        
        List<Pattern> whitelistedURIPatterns = new ArrayList<>(authWhitelistedURIs.size());
        for(String uri : authWhitelistedURIs) {
            whitelistedURIPatterns.add(Pattern.compile(regexPatternFromURIs(uri, true)));
        }
        return whitelistedURIPatterns;
    }
    
    private String regexPatternFromURIs(String uri, boolean allowWildcards) { 
        final String toReplace = "\\.[]{}()^$?+|";
        if('/' == uri.charAt(0)) {
            uri = uri.substring(1);
        }
        StringBuilder regex = new StringBuilder(); 
        for (int i=0; i < uri.length(); i++) { 
            char c = uri.charAt(i); 
            if (c == '*' && allowWildcards) { 
                regex.append('.'); 
            } else if (toReplace.indexOf(c) > -1) { 
                regex.append('\\'); 
            }
            regex.append(c); 
        }
        return regex.toString(); 
    }
        
    public boolean isUriAuthWhitelisted(String path) {
        List<Pattern> whitelistedURIPatterns = this.getAuthWhitelistedURIPatterns();
        if(whitelistedURIPatterns == null) {
            return false;
        }
        
        // Add "/" at the end of path to match "target/*"
        if(!path.endsWith("/")) {
            path += "/";
        }
        
        for(Pattern pattern : whitelistedURIPatterns) {
            if(pattern.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }
}
