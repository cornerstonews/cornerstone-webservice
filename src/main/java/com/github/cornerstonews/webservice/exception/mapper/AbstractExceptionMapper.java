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

import java.util.List;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.cornerstonews.webservice.model.WsError;

public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    private static final Logger log = LogManager.getLogger(AbstractExceptionMapper.class);

    @Context
    private Configuration config;

    @Context
    private Request request;

    protected T exception;

    private final List<Variant> variants = Variant.mediaTypes(
            MediaType.TEXT_PLAIN_TYPE, 
            MediaType.APPLICATION_XML_TYPE, 
            MediaType.APPLICATION_JSON_TYPE).build();

    protected abstract ResponseBuilder getResponseBuilder();

    protected WsError getWsError() {
        return new WsError(this.exception.getMessage());
    }

    @Override
    public Response toResponse(T exception) {
        this.exception = exception;
        log.debug("Exception caught by WsExceptionMapper. Error: {}", this.exception.getMessage());
        log.trace("Exception: ", this.exception);

        ResponseBuilder responseBuilder = getResponseBuilder();

        final Variant variant = request.selectVariant(variants);
        if (variant != null) {
            responseBuilder.type(variant.getMediaType());
        } else {

            // default media type which will be used only when none media type from {@value variants} is in accept
            // header of original request. TODO: could be settable by configuration property.
            responseBuilder.type(MediaType.APPLICATION_JSON_TYPE);
        }

        WsError error = getWsError();
        responseBuilder.entity(new GenericEntity<>(error, new GenericType<WsError>() {}.getType()));
        return responseBuilder.build();
    }
}
