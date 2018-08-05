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

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

public class WebserviceRequestEventListener implements RequestEventListener {
    private static final Logger log = LogManager.getLogger(WebserviceRequestEventListener.class);
    
    private final int requestNumber;
    private long startTime;
    
    private final Timer connectionTimer;
    private Counter activeRequests;
    private final Meter[] responses;
    private Timer.Context context = null;
    
    public WebserviceRequestEventListener(int requestNumber, Counter activeRequests, Timer connectionTimer, final Meter[] responses) {
        this.requestNumber = requestNumber;
        this.activeRequests = activeRequests;
        this.activeRequests.inc();
        this.connectionTimer = connectionTimer;
        this.responses = Arrays.copyOf(responses, responses.length);
        
        startTime = System.currentTimeMillis();
        context = this.connectionTimer.time();
    }

    @Override
    public void onEvent(RequestEvent event) {
        switch (event.getType()) {
            case RESOURCE_METHOD_START:
                ExtendedUriInfo uriInfo = event.getUriInfo();
                log.info("Received request: {} {}", uriInfo.getMatchedResourceMethod().getHttpMethod(), uriInfo.getRequestUri());
                log.debug("Starting request #{}. Request: {} {}", requestNumber, uriInfo.getMatchedResourceMethod().getHttpMethod(), uriInfo.getRequestUri());
                break;
            case FINISHED:
                context.stop();
                log.debug("Request #{} finished.", requestNumber);
                log.trace("Processing time for request '{}': '{} ms'", requestNumber, (System.currentTimeMillis() - startTime));
                updateResponses(event.getContainerResponse());
                break;
            case EXCEPTION_MAPPER_FOUND:
                break;
            case EXCEPTION_MAPPING_FINISHED:
                break;
            case LOCATOR_MATCHED:
                break;
            case MATCHING_START:
                break;
            case ON_EXCEPTION:
                break;
            case REQUEST_FILTERED:
                break;
            case REQUEST_MATCHED:
                break;
            case RESOURCE_METHOD_FINISHED:
                break;
            case RESP_FILTERS_FINISHED:
                break;
            case RESP_FILTERS_START:
                break;
            case START:
                break;
            case SUBRESOURCE_LOCATED:
                break;
            default:
                break;
        }
    }
    
    private void updateResponses(ContainerResponse containerResponse) {
        if(containerResponse != null) {
            final int responseStatus = containerResponse.getStatus() / 100;
            if (responseStatus >= 1 && responseStatus <= 5) {
                responses[responseStatus - 1].mark();
            }
        }
        activeRequests.dec();
    }
}
