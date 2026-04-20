package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Smart Campus API");
        response.put("version", "1.0");
        response.put("description", "RESTful API for Smart Campus Sensor and Room Management");
        response.put("status", "operational");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("team", "Campus Infrastructure Team");
        contact.put("email", "admin@smartcampus.university.ac.uk");
        contact.put("documentation", "https://github.com/your-username/smartcampus-api");
        response.put("contact", contact);

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "http://localhost:8080/api/v1/rooms");
        resources.put("sensors", "http://localhost:8080/api/v1/sensors");
        response.put("resources", resources);

        Map<String, String> links = new LinkedHashMap<>();
        links.put("GET  /api/v1/rooms", "List all rooms");
        links.put("POST /api/v1/rooms", "Create a new room");
        links.put("GET  /api/v1/rooms/{roomId}", "Get a room by ID");
        links.put("DELETE /api/v1/rooms/{roomId}", "Delete a room (blocked if sensors present)");
        links.put("GET  /api/v1/sensors", "List all sensors");
        links.put("GET  /api/v1/sensors?type={type}", "Filter sensors by type");
        links.put("POST /api/v1/sensors", "Register a new sensor");
        links.put("GET  /api/v1/sensors/{sensorId}", "Get a sensor by ID");
        links.put("GET  /api/v1/sensors/{sensorId}/readings", "Get reading history for a sensor");
        links.put("POST /api/v1/sensors/{sensorId}/readings", "Record a new sensor reading");
        response.put("links", links);

        return Response.ok(response).build();
    }
}