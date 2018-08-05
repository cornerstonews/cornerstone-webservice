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
package com.github.cornerstonews.webservice.util;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import com.github.cornerstonews.webservice.exception.NotModifiedException;

public class WSResponse {

    private WSResponse() {
        throw new IllegalStateException("WSResponse class");
    }
    
    public static Response response(Request request, Object entity, Status status) throws NotModifiedException {

        Response.ResponseBuilder response = null;
        EntityTag tag = null;

        if (entity != null) {
            tag = new EntityTag(Integer.toString(entity.hashCode()));

            response = request.evaluatePreconditions(tag);
            if (response != null) {
                StatusType statusInfo = response.build().getStatusInfo();
                String responseString = "HTTP " + statusInfo.getStatusCode() + ' ' + statusInfo.getReasonPhrase();
                throw new NotModifiedException(responseString);
            }
        }

        response = Response.status(status).entity(entity);
        return response.tag(tag).build();
    }
}
