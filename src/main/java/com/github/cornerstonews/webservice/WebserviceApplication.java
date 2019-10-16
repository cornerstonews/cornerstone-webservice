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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.CsrfProtectionFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.github.cornerstonews.configuration.ConfigException;
import com.github.cornerstonews.configuration.ConfigFactory;
import com.github.cornerstonews.webservice.authorization.AdminRoleFilter;
import com.github.cornerstonews.webservice.configuration.BaseWebserviceConfig;
import com.github.cornerstonews.webservice.configuration.injection.Config;
import com.github.cornerstonews.webservice.configuration.injection.ConfigInjectionResolver;
import com.github.cornerstonews.webservice.exception.mapper.JavaLangErrorMapper;
import com.github.cornerstonews.webservice.exception.mapper.NotAcceptableExceptionMapper;
import com.github.cornerstonews.webservice.exception.mapper.NotModifiedExceptionMapper;
import com.github.cornerstonews.webservice.exception.mapper.ValidationExceptionMapper;
import com.github.cornerstonews.webservice.exception.mapper.WebApplicationExceptionMapper;
import com.github.cornerstonews.webservice.exception.mapper.WsErrorMessageBodyWriter;
import com.github.cornerstonews.webservice.exception.mapper.WsThrowableMapper;
import com.github.cornerstonews.webservice.resource.HealthCheckResource;
import com.github.cornerstonews.webservice.resource.LoggerResource;
import com.github.cornerstonews.webservice.resource.MetricsResource;
import com.github.cornerstonews.webservice.util.ClassFinder;
import com.github.cornerstonews.webservice.util.Generics;

public abstract class WebserviceApplication<T extends BaseWebserviceConfig> extends ResourceConfig {
    
    private static final Logger log = LogManager.getLogger(WebserviceApplication.class);

    protected T configuration;

    private MetricsRegistryFeature metricsRegistryFeature;
    private boolean metricsAreRegistered;
    private HealthCheckRegistry healthCheckRegistry;
    
    public WebserviceApplication(String applicationName) {
        super();
        setApplicationName(applicationName);
        initialize();
    }
    
    public final void initialize() {
        try {
            this.loadConfig(getConfigPath());
            if(this.configuration.isRegisterDefaults()) {
                registerDefaults();
            }
            initializeJersey();
        } catch (ConfigException | IOException e) {
            log.error("Exception initializing webservice.", e);
            // TODO: shutdownapp
        }
    }

    public abstract void initializeJersey();

    public abstract void initApp();

    public abstract void destroyApp();

    public String getConfigPath() throws ConfigException {
        return ConfigFactory.getConfigPath();
    }
    
    private Class<T> getConfigurationClass() {
        return Generics.getTypeParameter(getClass(), BaseWebserviceConfig.class);
    }
    
    public T getWebserviceConfiguration() {
        return this.configuration;
    }

    public final T loadConfig(String path) throws ConfigException, IOException {
        if (this.configuration == null) {
            this.configuration = ConfigFactory.loadConfig(path, this.getConfigurationClass());
            register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(configuration).to(BaseWebserviceConfig.class);
                    bind(configuration).to(getConfigurationClass());
                    
//                    // Below will use factory to provide configuration instance but bind above provides the same
//                    BaseWebserviceConfigFactory factory = new BaseWebserviceConfigFactory(configuration);
//                    bindFactory(factory).to(BaseWebserviceConfig.class);
//                    bindFactory(factory).to(getConfigurationClass());
                    
                    bind(ConfigInjectionResolver.class).to(new TypeLiteral<InjectionResolver<Config>>(){}).in(Singleton.class);
                }
            });
        }
        return this.configuration;
    }

    private final void registerDefaults() {

        this.registerMetrics(new MetricsRegistryFeature());

        this.registerHealthCheckRegistry();

        registerApplicationEventListener();

        register(MultiPartFeature.class);

        if(this.configuration.isRegisterCSRFFilter()) {
            register(new CsrfProtectionFilter());
        }
        
        if (this.configuration.isRegisterCORSFilter()) {
            register(getCORSFilter());
        }

        if (this.configuration.isRegisterRolesAllowedDynamicFeature()) {
            log.debug("Registering RolesAllowedDynamicFeature.class");
            register(RolesAllowedDynamicFeature.class);
        }

        if (this.configuration.isRegisterServerProperties()) {
            getServerProperties().entrySet().forEach(p -> property(p.getKey(), p.getValue()));
        }
        
        // Register Exception Mappers
        register(JavaLangErrorMapper.class);
        register(NotAcceptableExceptionMapper.class);
        register(NotModifiedExceptionMapper.class);
        register(ValidationExceptionMapper.class);
        register(WebApplicationExceptionMapper.class);
        register(WsErrorMessageBodyWriter.class);
        register(WsThrowableMapper.class);
        findAndRegisterMappers();
        
        // Register resources
        register(LoggerResource.class);
        register(AdminRoleFilter.class);
        
    }

	private void findAndRegisterMappers() {
		try {
			ClassFinder classFinder = new ClassFinder(ExceptionMapper.class, "com.github.cornerstonews");
			classFinder.getClasses().forEach(c -> {
				if(c.isAnnotationPresent(Provider.class)) {
					log.debug("Registering exception mapper: {}", c.getName());
					register(c);
				}
			});
		} catch (IOException e) {
			log.error("Exception thrown while finding and registering exception mapper. Exception: ", e);
		}
	}
    
    private void registerMetrics(MetricsRegistryFeature metricsRegistryFeature) {
        if (metricsAreRegistered) {
            return;
        }
        
        this.metricsRegistryFeature = metricsRegistryFeature;
        register(new InstrumentedResourceMethodApplicationListener(metricsRegistryFeature.getMetricRegistry()));
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(metricsRegistryFeature.getMetricRegistry()).to(MetricRegistry.class);
            }
        });

        register(MetricsResource.class);
        
        metricsAreRegistered = true;
    }
    
    private void registerHealthCheckRegistry() {
        this.healthCheckRegistry = new HealthCheckRegistry();
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(healthCheckRegistry).to(HealthCheckRegistry.class);
            }
        });
        register(HealthCheckResource.class);
    }

    protected void destroyDefaults() {
        this.getHealthchecks().shutdown();
        this.metricsRegistryFeature.deregisterMetrics();
    }

    private boolean isComponentRegistered(final Class<?> type) {
        boolean result = false;
        for (Class<?> c : getClasses()) {
            if (c.isInstance(type)) {
                result = true;
            }
        }
        return result;
    }

    protected ApplicationEventListener getApplicationEventListener() {
        return new WebserviceApplicationEventListener(this.metricsRegistryFeature.getMetricRegistry());
    }

    private void registerApplicationEventListener() {
        if (!isComponentRegistered(RequestEventListener.class)) {
            ApplicationEventListener eventListener = getApplicationEventListener();
            log.debug("Registering {} as application event listener.", eventListener);
            register(eventListener);
        }
    }

    protected ContainerResponseFilter getCORSFilter() {
        return new CORSFilter();
    }

    protected Map<String, Boolean> getServerProperties() {
        HashMap<String, Boolean> serverProperties = new HashMap<>();
        serverProperties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, Boolean.FALSE);
        serverProperties.put(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, Boolean.TRUE);
        return serverProperties;
    }

    public HealthCheckRegistry getHealthchecks() {
        return healthCheckRegistry;
    }
    
    private class WebserviceApplicationEventListener implements ApplicationEventListener {
        private final Logger log2 = LogManager.getLogger(WebserviceApplicationEventListener.class);

        private final String newline = String.format("%n");
        
        private volatile int requestCnt = 0;
        private Meter[] responses;
        private Timer connectionTimer;    
        private Counter activeRequests;
        private MetricRegistry metricRegistry;

        public WebserviceApplicationEventListener(MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
            
            this.connectionTimer = this.metricRegistry.timer(MetricRegistry.name(WebserviceRequestEventListener.class, "connections"));
            this.activeRequests = this.metricRegistry.counter(MetricRegistry.name(WebserviceRequestEventListener.class, "active-requests"));
            this.responses = new Meter[]{
                    this.metricRegistry.meter(MetricRegistry.name(WebserviceRequestEventListener.class, "1xx-responses")), // 1xx
                    this.metricRegistry.meter(MetricRegistry.name(WebserviceRequestEventListener.class, "2xx-responses")), // 2xx
                    this.metricRegistry.meter(MetricRegistry.name(WebserviceRequestEventListener.class, "3xx-responses")), // 3xx
                    this.metricRegistry.meter(MetricRegistry.name(WebserviceRequestEventListener.class, "4xx-responses")), // 4xx
                    this.metricRegistry.meter(MetricRegistry.name(WebserviceRequestEventListener.class, "5xx-responses"))  // 5xx
            };
        }
        
        @Override
        public void onEvent(ApplicationEvent event) {

            String appName = event.getResourceConfig().getApplicationName();

            switch (event.getType()) {
            case INITIALIZATION_FINISHED:
                log2.info("Starting {} web service.", appName);
                this.logComponents(event.getResourceConfig().getClasses(), event.getResourceConfig().getResources());

                // Do something, i.e. initialize application
                initApp();
                
                log2.info("{} web service is ready.", appName);
                break;

            case DESTROY_FINISHED:
                log2.info("{} web service shutdown called.", appName);
                
                // Do something, i.e. shutdown application
                destroyApp();
                destroyDefaults();
                
                log2.info("{} web service destroyed.", appName);
                break;

            case INITIALIZATION_APP_FINISHED:
                log2.info("Jersey initialization finished.");
                break;

            case INITIALIZATION_START:
                log2.info("Starting {} web service initialization", appName);
                break;

            case RELOAD_FINISHED:
                break;

            default:
                log2.info("WebserviceApplicationConfiguration: Unknown Application event happend", event.getType());
                break;
            }
        }
        
        private void logComponents(Set<Class<?>> classes, Set<Resource> allResources) {
            final Set<String> resources = new HashSet<>();
            final StringBuilder resourcesSB = new StringBuilder();
            resourcesSB.append(newline).append("The following resource classes were found:").append(newline).append(newline);
            
            final Set<String> providers = new HashSet<>(); 
            final StringBuilder providersSB = new StringBuilder();
            providersSB.append("The following provider classes were found:").append(newline).append(newline);
            
            final Set<Class<?>> allResourcesClasses = new HashSet<>();
            
            classes.forEach(c -> {
                if(c.isAnnotationPresent((Class<? extends Annotation>) Path.class)) {
                    resources.add(c.getCanonicalName());
                    resourcesSB.append("    - ").append(c.getCanonicalName()).append(newline);
                } else if(c.isAnnotationPresent((Class<? extends Annotation>) Provider.class)) {
                    providers.add(c.getCanonicalName());
                    providersSB.append("    - ").append(c.getCanonicalName()).append(newline);
                }
                
                if (!c.isInterface() && Resource.from(c) != null) {
                    allResourcesClasses.add(c);
                }
            });
            
            log2.info(resourcesSB.toString());
            log2.info(providersSB.toString());
            log2.info(new WebserviceEnpointLogger().getEndpointsInfo(allResourcesClasses, allResources));
        }

        @Override
        public RequestEventListener onRequest(RequestEvent requestEvent) {
            return new WebserviceRequestEventListener(this.requestCnt, this.activeRequests, this.connectionTimer, this.responses);
        }

    }
}