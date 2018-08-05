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

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

public class JWTPrincipal implements Principal {

    protected final String name;
    protected final String[] roles;

    /**
     * Construct a new JWTPrincipal, for the specified username, with the specified role names (as Strings).
     *
     * @param name The username of the user represented by this Principal
     * @param roles List of roles (must be Strings) possessed by this user
     */
    public JWTPrincipal(String name, List<String> roles) {
        this.name = name;
        if (roles == null) {
            this.roles = new String[0];
        } else {
            this.roles = roles.toArray(new String[roles.size()]);
            if (this.roles.length > 1) {
                Arrays.sort(this.roles);
            }
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String[] getRoles() {
        return Arrays.copyOf(this.roles, this.roles.length);
    }

    public Principal getUserPrincipal() {
        return this;
    }

    public boolean hasRole(String role) {
        if ("*".equals(role)) {
            return true;
        }
        if (role == null) {
            return false;
        }
        return Arrays.binarySearch(roles, role) >= 0;
    }

}