package com.smartcampus;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class ApiApplication extends ResourceConfig {

    public ApiApplication() {
        packages("com.smartcampus");
        register(org.glassfish.jersey.jackson.JacksonFeature.class);
        property("jersey.config.server.wadl.disableWadl", true);
    }
}