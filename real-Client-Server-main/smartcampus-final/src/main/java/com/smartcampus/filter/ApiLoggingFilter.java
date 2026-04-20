package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * PART 5.5 – API Request and Response Logging Filter
 *
 * Implements BOTH filter interfaces in one class:
 *
 *   ContainerRequestFilter  → executed BEFORE the request reaches any resource method
 *   ContainerResponseFilter → executed AFTER the resource method returns a response
 *
 * Uses java.util.logging.Logger as required by the coursework specification.
 *
 * @Provider registers both filter roles with JAX-RS automatically — no XML config needed.
 */
@Provider
public class ApiLoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER =
            Logger.getLogger(ApiLoggingFilter.class.getName());

    /**
     * INCOMING REQUEST — runs before any resource method.
     * Logs the HTTP method and full request URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();

        LOGGER.info(String.format(
                "[REQUEST]  --> %-7s %s", method, uri));
    }

    /**
     * OUTGOING RESPONSE — runs after the resource method completes.
     * Logs the HTTP status code sent back to the client.
     */
    @Override
    public void filter(ContainerRequestContext  requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        int    status = responseContext.getStatus();
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();

        LOGGER.info(String.format(
                "[RESPONSE] <-- %d   for %-7s %s", status, method, uri));
    }
}
