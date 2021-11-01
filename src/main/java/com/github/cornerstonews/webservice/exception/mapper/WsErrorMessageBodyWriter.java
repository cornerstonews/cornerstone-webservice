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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.MarshallerProperties;

import com.github.cornerstonews.webservice.model.WsError;

@Provider
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, "*/*" })
public class WsErrorMessageBodyWriter implements MessageBodyWriter<WsError> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == WsError.class;
    }

    @Override
    public long getSize(WsError t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return -1;
    }

    @Override
    public void writeTo(WsError error, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        try {
            JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] { type }, null);
            Marshaller marshaller = jaxbContext.createMarshaller();

            if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType) || mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)
                    || (!MediaType.APPLICATION_XML_TYPE.equals(mediaType) && !MediaType.TEXT_PLAIN_TYPE.equals(mediaType))) {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
                marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, Boolean.FALSE);
            }

            if (MediaType.TEXT_PLAIN_TYPE.equals(mediaType)) {
                entityStream.write(error.toString().getBytes(Charset.forName("UTF-8")));
            } else {
                marshaller.marshal(error, entityStream);
            }
        } catch (JAXBException jaxbException) {
            throw new ProcessingException("Error serializing a WsError to the output stream", jaxbException);
        }
    }

}
