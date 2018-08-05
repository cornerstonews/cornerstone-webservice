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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import com.github.cornerstonews.webservice.model.WsError;

@Provider
public class WebApplicationExceptionMapper extends AbstractExceptionMapper<WebApplicationException> {

    @Override
    protected ResponseBuilder getResponseBuilder() {
        return Response.status(exception.getResponse().getStatus());
    }

    @Override
    protected WsError getWsError() {

        WsError error = new WsError(exception.getMessage());
        Throwable cause = exception.getCause();
        if (cause != null) {
            Throwable cause2;
            if (cause.getMessage() != null) {
                error.setErrorMessage(cause.getMessage());
            } else if ((cause2 = cause.getCause()) != null && cause2.getMessage() != null) {
                error.setErrorMessage(cause2.getMessage());
            } else if ((((JAXBException) cause).getLinkedException()) != null 
                    && (((JAXBException) cause).getLinkedException().getMessage()) != null) {
                error.setErrorMessage(((JAXBException) cause).getLinkedException().getMessage());
            }
        }

        return error;
    }

}
