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
package com.github.cornerstonews.webservice.exception.mapper;

import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.github.cornerstonews.webservice.model.WsError;

@Provider
public class NotAcceptableExceptionMapper extends AbstractExceptionMapper<NotAcceptableException> {

    @Override
    protected ResponseBuilder getResponseBuilder() {
        return Response.status(Status.NOT_ACCEPTABLE);
    }

    @Override
    protected WsError getWsError() {
        String errorString = "Requested resource could not produce acceptable reprentation of the requested user agent. Please check your accept headers.";
        return new WsError(errorString);
    }

}