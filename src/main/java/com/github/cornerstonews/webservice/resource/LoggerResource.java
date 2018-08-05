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

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.github.cornerstonews.webservice.authorization.AdminRole;

@Path("logger")
@AdminRole
public class LoggerResource {

    private static final Logger log = LogManager.getLogger(LoggerResource.class);
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/root/{level}")
    public Response setRootLogLevel(@PathParam("level") String level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
        loggerConfig.setLevel(Level.getLevel(level.toUpperCase()));
        ctx.updateLoggers();
        log.log(Level.getLevel(level.toUpperCase()), "Updated logger to '{}' log level.", level);
        return Response.ok().build();
    }
    
    @PUT
    @Path("/{logName}/{level}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setLogLevel(@PathParam("logName") String logName, @PathParam("level") String level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(logName);
        LoggerConfig specificConfig = loggerConfig;

        // We need a specific configuration for this logger,
        // otherwise we would change the level of all other loggers
        if (!loggerConfig.getName().equals(logName)) {
            specificConfig = new LoggerConfig(logName, Level.getLevel(level.toUpperCase()), true);
            specificConfig.setParent(loggerConfig);
            config.addLogger(logName, specificConfig);
        }
        specificConfig.setLevel(Level.getLevel(level.toUpperCase()));
        ctx.updateLoggers();
        return Response.ok().build();
    }
    
}
