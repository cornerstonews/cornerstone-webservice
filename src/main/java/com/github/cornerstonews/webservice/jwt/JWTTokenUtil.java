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

import java.nio.charset.Charset;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTTokenUtil {

    private static final String USER = "username";
    private static final String ROLE = "roles";
    private final String secretKey;
    private final Key key;
    private final SignatureAlgorithm algorithm;

    private static final Charset charset = Charset.forName("UTF-8");

    public JWTTokenUtil(String secretKey) {
        this.secretKey = secretKey;
        this.algorithm = SignatureAlgorithm.HS512;
        this.key = null;
    }

    public JWTTokenUtil(Key key) {
        this.key = key;
        this.algorithm = SignatureAlgorithm.RS512;
        this.secretKey = null;
    }

    public String generateToken(JWTPrincipal principal, Date expiration, Map<String, Object> claims) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .setSubject(principal.getName())
                .setExpiration(expiration)
                .setIssuedAt(new Date())
                .claim(USER, principal.getName())
                .claim(ROLE, principal.getRoles())
                .addClaims(claims);

        if (this.secretKey != null) {
            SecretKey key = new SecretKeySpec(this.secretKey.getBytes(charset), this.algorithm.getJcaName());
            jwtBuilder.signWith(key, this.algorithm);
        } else {
            jwtBuilder.signWith(key);
        }

        return jwtBuilder.compact();
    }

    public JWTPrincipal validateToken(String jwtToken) throws JWTException {
        Jws<Claims> claims = null;
        if (this.secretKey != null) {
            claims = Jwts.parserBuilder().setSigningKey(this.secretKey.getBytes(charset)).build().parseClaimsJws(jwtToken);
        } else {
            claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
        }

        if (!this.algorithm.getValue().equals(claims.getHeader().getAlgorithm())) {
            throw new JWTException("Invalid token algorithm.");
        }

        Claims body = claims.getBody();

        if (new Date().after(body.getExpiration())) {
            throw new JWTException("Token has expired.");
        }

        String username = body.getSubject();
        @SuppressWarnings("unchecked")
        ArrayList<String> roles = (ArrayList<String>) body.get(ROLE);

        return new JWTPrincipal(username, roles);
    }

    public Object getClaim(String jwtToken, Object keyName) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(this.secretKey.getBytes(charset)).build().parseClaimsJws(jwtToken);
        Claims body = claims.getBody();

        return body.get(keyName);
    }

    public Map<String, Object> getClaims(String jwtToken, List<String> claimKeys) {
        Map<String, Object> claims = new HashMap<>();

        Jws<Claims> jwtClaims = Jwts.parserBuilder().setSigningKey(this.secretKey.getBytes(charset)).build().parseClaimsJws(jwtToken);
        Claims body = jwtClaims.getBody();
        for (String claimKey : claimKeys) {
            Object value;
            if ((value = body.get(claimKey)) != null) {
                claims.put(claimKey, value);
            }
        }

        return claims;
    }
}
