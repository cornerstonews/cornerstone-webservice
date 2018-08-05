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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cornerstonews.webservice.authorization.MonitorRole;
import com.github.cornerstonews.webservice.configuration.BaseWebserviceConfig;
import com.github.cornerstonews.webservice.configuration.injection.Config;

@Path("metrics")
@MonitorRole
public class MetricsResource {
    
    @Inject
    private MetricRegistry registry;

    @Config
    private BaseWebserviceConfig config;

    private ObjectMapper getObjectMapper() {
        final TimeUnit rateUnit = parseTimeUnit(config.getRateUnit(), TimeUnit.SECONDS);
        final TimeUnit durationUnit = parseTimeUnit(config.getDurationUnit(), TimeUnit.SECONDS);
        final boolean showSamples = false;

        // MetricFilter filter = (MetricFilter) context.getAttribute(METRIC_FILTER);
        MetricFilter filter = MetricFilter.ALL;

        return new ObjectMapper().registerModule(new MetricsModule(rateUnit, durationUnit, showSamples, filter));
    }

    private TimeUnit parseTimeUnit(String value, TimeUnit defaultValue) {
        try {
            return TimeUnit.valueOf(String.valueOf(value).toUpperCase(Locale.US));
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getMetrics(@QueryParam("pretty") boolean pretty) throws JsonProcessingException {
        
        String result;
        if (pretty) {
            result = this.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registry);
        } else {
            result = this.getObjectMapper().writer().writeValueAsString(registry);
        }
        
        return Response.ok(result).header("Cache-Control", "must-revalidate,no-cache,no-store").build();
    }

}
