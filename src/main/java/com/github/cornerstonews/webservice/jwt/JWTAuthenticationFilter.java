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
package com.github.cornerstonews.webservice.jwt;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
//import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.cornerstonews.webservice.authorization.DefaultAuthenticationFilter;
import com.github.cornerstonews.webservice.configuration.BaseWebserviceConfig;
import com.github.cornerstonews.webservice.configuration.injection.Config;
import com.github.cornerstonews.webservice.model.WsError;

import io.jsonwebtoken.ExpiredJwtException;

@Provider
//@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthenticationFilter extends DefaultAuthenticationFilter {

    private static final Logger log = LogManager.getLogger(JWTAuthenticationFilter.class);

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Config
    BaseWebserviceConfig config;

    @Context
    private UriInfo uriInfo;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (isAuthenticated(requestContext) || isWhitelisted(requestContext)) {
            // continue to next auth filter
            return;
        }

        // Get the Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        // For JWT, Authorization header must not be null and must be prefixed with "Bearer" plus a whitespace
        if (authorizationHeader == null || !authorizationHeader.startsWith(AUTHENTICATION_SCHEME + " ")) {
            // continue to next auth filter
            return;
        }

        try {
            String jwtToken = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
            JWTPrincipal principal = validateToken(jwtToken);
            JWTSecurityContext newSecurityContext = new JWTSecurityContext(principal, requestContext.getSecurityContext().isSecure());
            requestContext.setSecurityContext(newSecurityContext);
        } catch (Exception e) {
            log.info("Exception caught in AuthenticationFilter. JWT Token validation error: {}", e.getMessage());
            log.debug("Exception stacktrace: ", e);
            WsError error = new WsError("Provided JWT token is invalid.");
            if (e instanceof ExpiredJwtException) {
                error.setErrorMessage("Provided JWT token is expired.");
            }
            abortWithUnauthorized(requestContext, error);
        }
    }

    public JWTPrincipal validateToken(String jwtToken) throws JWTException {
        JWTTokenUtil jwtUtil = new JWTTokenUtil(config.getJwtToken().getSecretKey());
        return jwtUtil.validateToken(jwtToken);
    }

    public static class JWTSecurityContext implements SecurityContext {

        private JWTPrincipal principal;
        private boolean isSecure;

        public JWTSecurityContext(JWTPrincipal principal, boolean isSecure) {
            this.principal = principal;
            this.isSecure = isSecure;
        }

        @Override
        public Principal getUserPrincipal() {
            return this.principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return this.principal.hasRole(role);
        }

        @Override
        public boolean isSecure() {
            return this.isSecure;
        }

        @Override
        public String getAuthenticationScheme() {
            return AUTHENTICATION_SCHEME;
        }
    }

}
