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

import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.github.cornerstonews.webservice.configuration.BaseWebserviceConfig;
import com.github.cornerstonews.webservice.configuration.injection.Config;
import com.github.cornerstonews.webservice.model.WsError;

public abstract class JWTAuthenticationResource {

    @Config
    protected BaseWebserviceConfig config;
    
    @Inject
    protected JWTAuthenticator authenticator;

    // Must provide injected JWTAuthenticator;
    public JWTAuthenticationResource() {
    }
    
    public JWTAuthenticationResource(JWTAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postAuth(@FormParam("username") String username, @FormParam("password") String password) {
        return authenticateUser(username, password);
    }
    
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postLogin(@FormParam("username") String username, @FormParam("password") String password) {
        return authenticateUser(username, password);
    }

    private Response authenticateUser(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            JWTPrincipal jwtPrincipal = authenticator.authenticate(username, password);

            // Issue a token for the user
            JWTTokenUtil jwtUtil = new JWTTokenUtil(config.getJwtToken().getSecretKey());
            long expiration = System.currentTimeMillis() + (config.getJwtToken().getExpirationInSeconds() * 1000);
            String jwtToken = jwtUtil.generateToken(jwtPrincipal, new Date(expiration), null);

            // Return the JWT token in the response
            return Response.ok(new JwtTokenDO(jwtToken)).build();

        } catch (Exception e) {
            return Response.status(Status.FORBIDDEN).entity(new WsError(e.getMessage())).build();
        }
    }
    
}
