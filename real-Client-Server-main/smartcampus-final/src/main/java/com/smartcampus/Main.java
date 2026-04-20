package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws IOException {

        final ResourceConfig rc = new ResourceConfig()
                .packages("com.smartcampus")
                .register(org.glassfish.jersey.jackson.JacksonFeature.class)
                .property("jersey.config.server.wadl.disableWadl", true);

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), rc
        );

        System.out.println("===========================================");
        System.out.println(" Smart Campus API is running!");
        System.out.println(" Base URL : " + BASE_URI);
        System.out.println(" API Root : " + BASE_URI + "api/v1");
        System.out.println(" Press ENTER to stop the server...");
        System.out.println("===========================================");

        System.in.read();
        server.stop();
        System.out.println("Server stopped.");
    }
}