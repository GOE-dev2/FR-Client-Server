package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application bootstrap class.
 *
 * @ApplicationPath("/api/v1") prefixes ALL resource endpoints with /api/v1.
 *
 * Example:  @Path("/rooms")  →  http://localhost:8080/api/v1/rooms
 *
 * Lifecycle note (Part 1.1):
 * JAX-RS creates a NEW resource class instance per request (request-scoped).
 * Resource classes are NOT singletons. All shared data must therefore live
 * in the DataStore singleton backed by ConcurrentHashMap.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Empty – Jersey discovers @Path / @Provider classes via package scan in Main.java
}
