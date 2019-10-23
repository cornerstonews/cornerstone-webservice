package com.github.cornerstonews.webservice.authorization;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.cornerstonews.webservice.configuration.BaseWebserviceConfig;
import com.github.cornerstonews.webservice.configuration.injection.Config;
import com.github.cornerstonews.webservice.model.WsError;

@Provider
//@PreMatching
@Priority(Priorities.AUTHENTICATION + 999) // Adding 999, will make this filter the last one to run
public class DefaultAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = LogManager.getLogger(DefaultAuthenticationFilter.class);

    @Config
    BaseWebserviceConfig config;

    @Context
    private UriInfo uriInfo;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (isAuthenticated(requestContext) || isWhitelisted(requestContext)) {
            // continue to next auth filter
            return;
        }

        // Fail request if not authenticated
        abortWithUnauthorized(requestContext);
    }

    protected boolean isAuthenticated(ContainerRequestContext requestContext) {
        return requestContext.getSecurityContext().getUserPrincipal() != null;
    }

    protected boolean isWhitelisted(ContainerRequestContext requestContext) {
        Method resourceMethod = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();

        // Access allowed for all if method has permit all annotation
        // or class has permit all annotation but then method must not have roles allowed
        if (resourceMethod.isAnnotationPresent(PermitAll.class)
                || (!resourceMethod.isAnnotationPresent(RolesAllowed.class) && resourceClass.isAnnotationPresent(PermitAll.class))) {
            return true;
        }

        return false;
    }

    protected void abortWithUnauthorized(ContainerRequestContext requestContext) {
        abortWithUnauthorized(requestContext, null);
    }

    protected void abortWithUnauthorized(ContainerRequestContext requestContext, WsError error) {
        // Abort the filter chain with a 401 status code
        if (error == null) {
            error = new WsError("This request requires authentication.");
        }

        MediaType mediaType = requestContext.getMediaType();
        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_JSON_TYPE;
        }
        requestContext.abortWith(Response.status(Status.UNAUTHORIZED).entity(error).type(mediaType).build());
    }
}
