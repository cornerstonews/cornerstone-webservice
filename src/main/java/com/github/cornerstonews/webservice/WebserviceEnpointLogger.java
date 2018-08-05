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
package com.github.cornerstonews.webservice;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

public class WebserviceEnpointLogger {
    private static final String NEWLINE = String.format("%n");
    
    public String getEndpointsInfo(Set<Class<?>> allResourcesClasses, Set<Resource> allResources) {
        final StringBuilder msg = new StringBuilder(1024);
        final Set<EndpointLogLine> endpointLogLines = new TreeSet<>(new EndpointComparator());

        msg.append("The following paths were found for the configured resources:");
        msg.append(NEWLINE).append(NEWLINE);

        for (Class<?> klass : allResourcesClasses) {
            new EndpointLogger("/*", klass).populate(endpointLogLines);
        }

        for (Resource res : allResources) {
            for (Resource childRes : res.getChildResources()) {
                // It is not necessary to check if a handler class is already being logged.
                //
                // This code will never be reached because of ambiguous (sub-)resource methods
                // related to the OPTIONS method and @Consumes/@Produces annotations.
                
                for (Class<?> childResHandlerClass : childRes.getHandlerClasses()) {
                    EndpointLogger epl = new EndpointLogger("/*", childResHandlerClass);
                    epl.populate(cleanUpPath(res.getPath() + epl.rootPath), epl.klass, false, childRes, endpointLogLines);
                }
            }
        }

        if (!endpointLogLines.isEmpty()) {
            for (EndpointLogLine line : endpointLogLines) {
                msg.append(line).append(NEWLINE);
            }
        } else {
            msg.append("    NONE").append(NEWLINE);
        }

        return msg.toString();
    }

    private static final Pattern PATH_DIRTY_SLASHES = Pattern.compile("\\s*/\\s*/+\\s*");

    private String cleanUpPath(String path) {
        return PATH_DIRTY_SLASHES.matcher(path).replaceAll("/").trim();
    }

    /**
     * Takes care of recursively creating all registered endpoints and providing
     * them as Collection of lines to log on application start.
     */
    private static class EndpointLogger {
        private final String rootPath;
        private final Class<?> klass;

        public EndpointLogger(String urlPattern, Class<?> klass) {
            this.rootPath = urlPattern.endsWith("/*") ? urlPattern.substring(0, urlPattern.length() - 1) : urlPattern;
            this.klass = klass;
        }

        public void populate(Set<EndpointLogLine> endpointLogLines) {
            populate(this.rootPath, klass, false, endpointLogLines);
        }

        private void populate(String basePath, Class<?> klass, boolean isLocator, Set<EndpointLogLine> endpointLogLines) {
            populate(basePath, klass, isLocator, Resource.from(klass), endpointLogLines);
        }

        private void populate(String basePath, Class<?> klass, boolean isLocator, Resource resource, Set<EndpointLogLine> endpointLogLines) {
            if (!isLocator) {
                basePath = normalizePath(basePath, resource.getPath());
            }

            for (ResourceMethod method : resource.getResourceMethods()) {
                endpointLogLines.add(new EndpointLogLine(method.getHttpMethod(), basePath, klass));
            }

            final TypeResolver typeResolver = new TypeResolver();
            for (Resource childResource : resource.getChildResources()) {
                for (ResourceMethod method : childResource.getAllMethods()) {
                    if (method.getType() == ResourceMethod.JaxrsType.RESOURCE_METHOD) {
                        final String path = normalizePath(basePath, childResource.getPath());
                        endpointLogLines.add(new EndpointLogLine(method.getHttpMethod(), path, klass));
                    } else if (method.getType() == ResourceMethod.JaxrsType.SUB_RESOURCE_LOCATOR) {
                        final String path = normalizePath(basePath, childResource.getPath());
                        final ResolvedType responseType = typeResolver.resolve(method.getInvocable().getResponseType());
                        final Class<?> erasedType = !responseType.getTypeBindings().isEmpty() ? responseType.getTypeBindings().getBoundType(0).getErasedType()
                                : responseType.getErasedType();
                        if (Resource.from(erasedType) == null) {
                            endpointLogLines.add(new EndpointLogLine(method.getHttpMethod(), path, erasedType));
                        } else {
                            populate(path, erasedType, true, endpointLogLines);
                        }
                    }
                }
            }
        }

        private static String normalizePath(String basePath, String path) {
            if (path == null) {
                return basePath;
            }
            if (basePath.endsWith("/")) {
                return path.startsWith("/") ? basePath + path.substring(1) : basePath + path;
            }
            return path.startsWith("/") ? basePath + path : basePath + "/" + path;
        }
    }

    private static class EndpointLogLine {
        private final String httpMethod;
        private final String basePath;
        private final Class<?> klass;

        EndpointLogLine(String httpMethod, String basePath, Class<?> klass) {
            this.basePath = basePath;
            this.klass = klass;
            this.httpMethod = httpMethod;
        }

        @Override
        public String toString() {
            final String method = httpMethod == null ? "UNKNOWN" : httpMethod;
            return String.format("    %-7s %s (%s)", method, basePath, klass.getCanonicalName());
        }
    }

    private static class EndpointComparator implements Comparator<EndpointLogLine>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(EndpointLogLine endpointA, EndpointLogLine endpointB) {
            int basePathCompare = endpointA.basePath.compareTo(endpointB.basePath);
            if (basePathCompare != 0) {
                return basePathCompare;
            }

            if(endpointA.httpMethod == null && endpointB.httpMethod == null) {
                return 0;
            } else if (endpointA.httpMethod != null && endpointB.httpMethod == null) {
                return 1;
            } else if (endpointA.httpMethod == null) {
                return -1;
            } else {
                return endpointA.httpMethod.compareTo(endpointB.httpMethod);
            }
        }
    }
}
