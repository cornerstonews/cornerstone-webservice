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
package com.github.cornerstonews.webservice.resource;

import java.util.Map;
import java.util.SortedMap;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.json.HealthCheckModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cornerstonews.webservice.authorization.MonitorRole;

@Path("healthcheck")
@MonitorRole
public class HealthCheckResource {
    private static final Logger log = LogManager.getLogger(HealthCheckResource.class);

    @Inject
    private HealthCheckRegistry registry;
    
    private ObjectMapper mapper;
    
    public HealthCheckResource() {
        this.mapper = new ObjectMapper().registerModule(new HealthCheckModule());
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getHealthchecks(@QueryParam("pretty") boolean pretty) throws JsonProcessingException {

        final SortedMap<String, HealthCheck.Result> healthCheckResults = this.runHealthChecks();
        
        if(healthCheckResults.isEmpty()) {
            return Response.status(Status.NOT_IMPLEMENTED).entity("").build();
        }
        
        Status status = Status.INTERNAL_SERVER_ERROR;
        if(isAllHealthy(healthCheckResults)) {
            status = Status.OK;
        }
        
        String result;
        if(pretty) {
            result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(healthCheckResults);
        } else {
            result = mapper.writer().writeValueAsString(healthCheckResults);
        }
        
        return Response.status(status).entity(result).header("Cache-Control", "must-revalidate,no-cache,no-store").build();
    }
    
    private static boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
        for (HealthCheck.Result result : results.values()) {
            if (!result.isHealthy()) {
                return false;
            }
        }
        return true;
    }
    
    private SortedMap<String, HealthCheck.Result> runHealthChecks() {
//        if (executorService == null) {
            log.trace("Running healthcheck...");
            return registry.runHealthChecks();
//        }
//        return registry.runHealthChecks(executorService);
    }
}
