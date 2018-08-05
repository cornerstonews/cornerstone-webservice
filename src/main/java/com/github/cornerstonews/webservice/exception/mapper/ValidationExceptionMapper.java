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

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.validation.ValidationError;
import org.glassfish.jersey.server.validation.internal.LocalizationMessages;
import org.glassfish.jersey.server.validation.internal.ValidationHelper;

import com.github.cornerstonews.webservice.exception.InputValidationException;
import com.github.cornerstonews.webservice.model.WsError;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException>  {

    private static final Logger log = LogManager.getLogger(ValidationExceptionMapper.class);

    @Context
    private Configuration config;
    
    @Context
    private Request request;

    @Override
    public Response toResponse(final ValidationException exception) {
        if (exception instanceof ConstraintViolationException) {
            log.trace(LocalizationMessages.CONSTRAINT_VIOLATIONS_ENCOUNTERED(), exception);

            final ConstraintViolationException cve = (ConstraintViolationException) exception;
            final Response.ResponseBuilder response = Response.status(ValidationHelper.getResponseStatus(cve));
            response.type(getMediaType());            
            
            List<ValidationError> errors = new ArrayList<>(ValidationHelper.constraintViolationToValidationErrors(cve).size());
            for(ValidationError error : ValidationHelper.constraintViolationToValidationErrors(cve)) {
                error.setMessageTemplate(null);
                error.setPath(error.getPath().substring(error.getPath().lastIndexOf('.') + 1, error.getPath().length()));
                errors.add(error);
            }
            
            response.entity(new GenericEntity<>(errors,new GenericType<List<ValidationError>>() {}.getType()));
            return response.build();
        } else if (exception instanceof InputValidationException) {
            log.debug(LocalizationMessages.VALIDATION_EXCEPTION_RAISED(), exception);
            final Response.ResponseBuilder response = Response.status(Status.BAD_REQUEST);
            response.type(getMediaType());
            WsError error = new WsError(exception.getMessage());
            
            return response.entity(error).build();
            
        } else {
            log.warn(LocalizationMessages.VALIDATION_EXCEPTION_RAISED(), exception);
            final Response.ResponseBuilder response = Response.serverError();
            response.type(getMediaType());
            WsError error = new WsError(exception.getMessage());
            
            return response.entity(error).build();
        }
    }

    private MediaType getMediaType() {
     // Entity.
        final List<Variant> variants = Variant.mediaTypes(
                MediaType.TEXT_PLAIN_TYPE,
                MediaType.TEXT_HTML_TYPE,
                MediaType.APPLICATION_XML_TYPE,
                MediaType.APPLICATION_JSON_TYPE).build();
        final Variant variant = request.selectVariant(variants);
        if (variant != null) {
            return variant.getMediaType();
        } else {

            // default media type which will be used only when none media type from {@value variants} is in accept
            // header of original request.
            // could be settable by configuration property.
            return MediaType.TEXT_PLAIN_TYPE;
        }
    }
    
}
