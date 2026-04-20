 Smart Campus API

**Module:** 5COSC022W — Client-Server Architectures  
**Title:** Smart Campus Sensor & Room Management API  
**Technology:** Java 11+, JAX-RS 2.x (Jersey 2.41), Grizzly HTTP Server  
**Storage:** In-memory `ConcurrentHashMap` — no database  

---

## Table of Contents

1. [API Overview](#api-overview)
2. [Project Structure](#project-structure)
3. [How to Build and Run](#how-to-build-and-run)
4. [API Endpoints Reference](#api-endpoints-reference)
5. [Sample curl Commands](#sample-curl-commands)
6. [Report — Question Answers](#report--question-answers)

---

## API Overview

This RESTful API provides a backend service for managing campus rooms and IoT sensors as part of the University of Westminster's Smart Campus initiative. It is built using JAX-RS (Jersey implementation) with an embedded Grizzly HTTP server, requiring no external web server or database.

| Property   | Value                              |
|------------|------------------------------------|
| Base URL   | `http://localhost:8080/api/v1`     |
| Format     | JSON only (`application/json`)     |
| Framework  | JAX-RS 2.x — Jersey 2.41          |
| Server     | Grizzly (embedded, port 8080)      |
| Storage    | `ConcurrentHashMap` (in-memory)    |

### Resource Hierarchy

```
/api/v1
├── /rooms
│   ├── GET    /rooms              → List all rooms
│   ├── POST   /rooms              → Create a room
│   ├── GET    /rooms/{roomId}     → Get one room
│   └── DELETE /rooms/{roomId}     → Delete a room (blocked if sensors exist)
└── /sensors
    ├── GET    /sensors            → List all sensors (supports ?type= filter)
    ├── POST   /sensors            → Register a sensor (validates roomId)
    ├── GET    /sensors/{id}       → Get one sensor
    └── /sensors/{id}/readings
        ├── GET  /readings         → Get reading history
        └── POST /readings         → Add a reading (updates parent sensor value)
```

### Pre-loaded Data on Startup

The server seeds the following data automatically so the API is not empty:

| Type   | ID        | Details                              |
|--------|-----------|--------------------------------------|
| Room   | `LIB-301` | Library Quiet Study, capacity 50     |
| Room   | `ENG-101` | Engineering Lab A, capacity 30       |
| Sensor | `TEMP-001`| Temperature, ACTIVE, in LIB-301      |
| Sensor | `CO2-002` | CO2, ACTIVE, in LIB-301              |
| Sensor | `OCC-003` | Occupancy, MAINTENANCE, in ENG-101   |

### Error Handling

All errors return a structured JSON body — never a raw Java stack trace.

| HTTP Code | Scenario                                              |
|-----------|-------------------------------------------------------|
| 400       | Missing required fields (id, roomId)                  |
| 403       | POST reading attempted on MAINTENANCE / OFFLINE sensor|
| 404       | Requested resource does not exist                     |
| 409       | Attempt to DELETE a room that still has sensors       |
| 415       | Wrong Content-Type (not `application/json`)           |
| 422       | Sensor references a roomId that does not exist        |
| 500       | Unexpected server error (safe generic message only)   |

---

## Project Structure

```
smartcampus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java                                  ← Starts embedded Grizzly server
    ├── SmartCampusApplication.java                ← @ApplicationPath("/api/v1")
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── store/
    │   └── DataStore.java                         ← Singleton ConcurrentHashMap store
    ├── resource/
    │   ├── DiscoveryResource.java                 ← GET /api/v1
    │   ├── RoomResource.java                      ← /api/v1/rooms
    │   ├── SensorResource.java                    ← /api/v1/sensors
    │   └── SensorReadingResource.java             ← /api/v1/sensors/{id}/readings
    ├── exception/
    │   ├── RoomNotEmptyException.java
    │   ├── RoomNotEmptyExceptionMapper.java        ← → HTTP 409
    │   ├── LinkedResourceNotFoundException.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java  ← → HTTP 422
    │   ├── SensorUnavailableException.java
    │   ├── SensorUnavailableExceptionMapper.java   ← → HTTP 403
    │   └── GlobalExceptionMapper.java              ← Catch-all → HTTP 500
    └── filter/
        └── ApiLoggingFilter.java                  ← Logs all requests & responses
```

---

## How to Build and Run

### Prerequisites

- **Java 11 or higher** — verify with: `java -version`
- **Apache Maven 3.6 or higher** — verify with: `mvn -version`

Both must be installed and on your system PATH before proceeding.

---

### Step 1 — Clone the repository

```bash
git clone https://github.com/YOUR-USERNAME/smartcampus-api.git
cd smartcampus-api
```

---

### Step 2 — Build the fat JAR

```bash
mvn clean package
```

Maven will download all dependencies (Jersey, Grizzly, Jackson) and package everything into a single executable JAR file. This requires an internet connection the first time. The output file will be located at:

```
target/smartcampus-api.jar
```

If the build succeeds you will see `BUILD SUCCESS` at the end of the output.

---

### Step 3 — Start the server

```bash
java -jar target/smartcampus-api.jar
```

You will see the following output in the terminal:

```
===========================================
 Smart Campus API is running!
 Discovery : GET http://localhost:8080/api/v1/
 Rooms     : http://localhost:8080/api/v1/rooms
 Sensors   : http://localhost:8080/api/v1/sensors
 Press ENTER to stop the server...
===========================================
```

The server is now running on port 8080. Leave this terminal open.

---

### Step 4 — Test with Postman or curl

Open a **new terminal** (keep the server terminal running) and use the curl commands below, or open Postman and send requests to `http://localhost:8080/api/v1/...`.

For every POST request, ensure the following header is set:
```
Content-Type: application/json
```

---

### Step 5 — Stop the server

Return to the server terminal and press **ENTER**. You will see `Server stopped.`

---

## API Endpoints Reference

### Part 1 — Discovery

| Method | Path       | Response | Description                        |
|--------|------------|----------|------------------------------------|
| GET    | `/api/v1/` | 200      | Returns API metadata and all links |

---

### Part 2 — Room Management

| Method | Path                    | Response | Description                                   |
|--------|-------------------------|----------|-----------------------------------------------|
| GET    | `/api/v1/rooms`         | 200      | List all rooms                                |
| POST   | `/api/v1/rooms`         | 201      | Create a new room                             |
| GET    | `/api/v1/rooms/{roomId}`| 200      | Get a specific room by ID                     |
| DELETE | `/api/v1/rooms/{roomId}`| 204      | Delete a room (409 if sensors still assigned) |

---

### Part 3 — Sensor Operations

| Method | Path                      | Response | Description                                     |
|--------|---------------------------|----------|-------------------------------------------------|
| GET    | `/api/v1/sensors`         | 200      | List all sensors                                |
| GET    | `/api/v1/sensors?type=CO2`| 200      | Filter sensors by type (case-insensitive)       |
| POST   | `/api/v1/sensors`         | 201      | Register a sensor (validates roomId exists)     |
| GET    | `/api/v1/sensors/{id}`    | 200      | Get a specific sensor by ID                     |

---

### Part 4 — Sensor Readings (Sub-Resource)

| Method | Path                                  | Response | Description                                            |
|--------|---------------------------------------|----------|--------------------------------------------------------|
| GET    | `/api/v1/sensors/{id}/readings`       | 200      | Get the full reading history for a sensor              |
| POST   | `/api/v1/sensors/{id}/readings`       | 201      | Add a new reading (also updates sensor's currentValue) |

---

## Sample curl Commands

> The server seeds data on startup. `LIB-301`, `ENG-101`, `TEMP-001`, `CO2-002` and `OCC-003` are already available without any setup.

---

### 1. GET — Discovery endpoint

```bash
curl -X GET http://localhost:8080/api/v1/ \
  -H "Accept: application/json"
```

**Expected:** `200 OK` — returns API version, contact info, and a map of all resource links.

---

### 2. POST — Create a new Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LAB-002",
    "name": "Computer Science Lab",
    "capacity": 25
  }'
```

**Expected:** `201 Created` — returns the created room object with a `Location` header.

---

### 3. POST — Register a new Sensor (valid roomId)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-099",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.0,
    "roomId": "ENG-101"
  }'
```

**Expected:** `201 Created` — sensor is created and linked to room ENG-101.

---

### 4. GET — Filter sensors by type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```

**Expected:** `200 OK` — returns only sensors with type `CO2` (i.e., `CO2-002`).

---

### 5. POST — Add a sensor reading

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 24.7
  }'
```

**Expected:** `201 Created` — reading is logged; `GET /api/v1/sensors/TEMP-001` will now show `currentValue: 24.7`.

---

### 6. GET — Retrieve reading history

```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Accept: application/json"
```

**Expected:** `200 OK` — returns an array of all readings recorded for TEMP-001.

---

### 7. DELETE — Attempt to delete a room that has sensors (error demo)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**Expected:** `409 Conflict` — clean JSON error body, room not deleted because it has sensors.

---

### 8. POST — Attempt reading on a MAINTENANCE sensor (error demo)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-003/readings \
  -H "Content-Type: application/json" \
  -d '{ "value": 12.0 }'
```

**Expected:** `403 Forbidden` — OCC-003 is seeded as MAINTENANCE, so readings are blocked.

---

### 9. POST — Register sensor with non-existent roomId (error demo)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "FAKE-999",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "ROOM-DOES-NOT-EXIST"
  }'
```

**Expected:** `422 Unprocessable Entity` — the roomId does not reference an existing room.

---

### 10. DELETE — Successfully delete an empty room

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LAB-002
```

**Expected:** `204 No Content` — LAB-002 was created in command 2 with no sensors, so deletion succeeds.

---

## Report — Question Answers

---

### Part 1.1 — Default Lifecycle of a JAX-RS Resource Class

By default, JAX-RS creates a **new instance** of each `@Path`-annotated resource class for **every incoming HTTP request**. This is known as the **per-request lifecycle** (also called request-scoped). The resource class is not a singleton — a fresh object is instantiated, used to handle the request, and then discarded.

This architectural decision has a direct impact on how shared state must be managed. Because each request creates a new resource object, any instance fields declared inside a resource class (such as `private Map<String, Room> rooms = new HashMap<>()`) would be reinitialised to empty on every request, causing all previously stored data to be lost immediately. This makes it impossible to store the in-memory "database" directly inside a resource class.

To solve this, the application uses a separate **DataStore singleton** — a class with a `private static final` instance that is created once when the application starts and shared across all resource instances via `DataStore.getInstance()`. The singleton holds three `ConcurrentHashMap` collections (one for rooms, one for sensors, one for readings). `ConcurrentHashMap` is used instead of a standard `HashMap` because multiple requests can arrive simultaneously on different threads, and concurrent writes to a regular `HashMap` can cause data corruption or loss in a race condition. `ConcurrentHashMap` handles concurrent access safely at the data structure level, ensuring thread safety without requiring explicit `synchronized` blocks on every operation.

---

### Part 1.2 — HATEOAS and Hypermedia in RESTful Design

HATEOAS — Hypermedia as the Engine of Application State — is the principle that an API response should contain links that tell the client what it can do next, rather than requiring the client to have prior knowledge of the API's URL structure. It is considered a hallmark of mature RESTful design because it brings the web's own navigational model (following links) into API interactions.

In this API, `GET /api/v1/` returns a `"links"` object listing every available endpoint and its path. A client application can request this endpoint at startup and build its entire request routing from the response, without any hardcoded URLs.

**Benefits over static documentation:**

**Self-documenting:** A developer or automated tool hitting `GET /api/v1/` can discover the complete API from the response itself, the same way a web browser discovers pages by following `<a href>` links. Static documentation must be read separately and is not always up to date.

**Loose coupling:** If a resource path changes — for example, `/rooms` is renamed to `/campus-rooms` — only the discovery response needs updating. Client applications that navigate by following the links returned by the server, rather than using hardcoded URL strings, continue to work without modification.

**Always accurate:** Static documentation is written by humans and tends to drift out of date as the API evolves. HATEOAS responses are generated directly by the running server, so they always reflect the actual current state of the API.

**Reduced integration friction:** External systems integrating with the API can use the discovery endpoint to verify available routes at runtime, making integration more robust and reducing the number of support queries caused by stale documentation.

---

### Part 2.1 — Returning IDs Only vs Full Room Objects

When `GET /api/v1/rooms` is called, there are two common design choices for the response format, and each involves trade-offs between network bandwidth and client-side processing.

**Returning full objects** (as implemented here) sends all room fields — `id`, `name`, `capacity`, `sensorIds` — in a single response payload. The advantage is that the client has everything it needs immediately and can render a complete room listing without making any additional requests. The disadvantage is that for large datasets (for example, a university with hundreds of rooms), the response payload becomes very large, consuming more bandwidth and increasing parse time on the client.

**Returning IDs only** produces a far smaller initial response (e.g., `["LIB-301", "ENG-101"]`). This reduces bandwidth for the initial call. However, if the client needs to display room names or capacity, it must then make one additional GET request per room — a pattern known as the **N+1 request problem** — which dramatically increases total latency and server load.

The industry best practice is to return a **summary representation** for collection endpoints: include the most commonly needed fields (id, name, capacity) but exclude large or infrequently needed nested data. Full detail is reserved for individual resource endpoints (`GET /rooms/{roomId}`). For very large collections, **pagination** (`?page=1&size=20`) should also be applied to prevent unbounded response sizes and protect server performance.

---

### Part 2.2 — Is DELETE Idempotent?

**Yes, DELETE is idempotent in this implementation.**

Idempotency means that executing the same operation multiple times produces the same final server state as executing it once. It is about the state of the server, not the HTTP response code returned.

**First DELETE** of `/api/v1/rooms/LAB-002`: The room exists in the store. It is removed. The server returns `204 No Content`. Server state: room is gone.

**Second DELETE** of `/api/v1/rooms/LAB-002`: The room no longer exists. The server returns `404 Not Found`. However, no state change occurs — the room was already gone. Server state: room is still gone.

After both calls, the server is in exactly the same state: the room does not exist. This satisfies the definition of idempotency. The response codes differ (204 vs 404), but that is correct and expected behaviour — the response code reflects what happened on that specific call, not the overall operation's idempotency.

This property is valuable in practice: if a client sends a DELETE request and receives no response due to a network timeout, it can safely resend the same request without risk. Whether the first attempt succeeded silently or the second attempt is the effective deletion, the outcome is identical — the room is removed.

---

### Part 3.1 — Technical Consequences of Wrong Content-Type with @Consumes

The `@Consumes(MediaType.APPLICATION_JSON)` annotation on the `POST /api/v1/sensors` method declares a **media type contract**: this method will only accept and process request bodies with the `Content-Type: application/json` header.

If a client sends a request with `Content-Type: text/plain` or `Content-Type: application/xml`, the following sequence occurs:

1. The HTTP request arrives at the Grizzly server and is handed to the Jersey (JAX-RS) runtime.
2. Jersey inspects the `Content-Type` header of the incoming request.
3. It searches all registered resource methods for one whose `@Consumes` declaration matches the incoming media type.
4. Finding no match for `text/plain` or `application/xml`, Jersey **rejects the request** before the resource method is ever invoked.
5. Jersey automatically returns **HTTP `415 Unsupported Media Type`** to the client.

The resource method body is never executed. No developer-written validation code is needed to handle this case — `@Consumes` acts as a declarative, framework-level media type guard. The client must correct the `Content-Type` header to `application/json` and resubmit the request.

---

### Part 3.2 — @QueryParam vs Path-Based Filtering

Two designs are possible for filtering sensors by type:

- **Path-based:** `GET /api/v1/sensors/type/CO2`
- **Query parameter:** `GET /api/v1/sensors?type=CO2`

The query parameter approach is considered superior for the following reasons:

**Semantic correctness:** A URL path is meant to identify a resource. `/sensors/type/CO2` implies that `type/CO2` is a resource identifier — it is not. `CO2` is a filter criterion applied to the `/sensors` collection. Query parameters are specifically designed to modify or constrain a request without altering the identity of the resource being addressed. Using them keeps the resource identity (`/sensors`) clean and correct.

**Composability:** Query parameters combine naturally with the `&` operator: `?type=CO2&status=ACTIVE&roomId=LIB-301`. The path-based equivalent becomes awkward and unreadable: `/sensors/type/CO2/status/ACTIVE/room/LIB-301`, and requires defining a separate route pattern for each combination of filters.

**Optional by design:** A query parameter is inherently optional. Omitting `?type=` simply returns all sensors. A path segment cannot be easily "omitted" — its absence implies a different resource or requires an additional route to handle the unfiltered case.

**Established REST convention:** REST conventions treat query strings as the standard mechanism for filtering, sorting, searching, and paginating collections. This is widely understood by client libraries, caching proxies, and API gateway tools. Developers integrating with the API will immediately recognise `?type=CO2` as a filter, whereas `/type/CO2` in a path could be misread as a sub-resource.

---

### Part 4.1 — Architectural Benefits of the Sub-Resource Locator Pattern

The sub-resource locator pattern involves a method in a parent resource class that has no HTTP verb annotation (`@GET`, `@POST`, etc.) but carries a `@Path` annotation. When a request arrives matching that path, JAX-RS calls the method and receives back an instance of another class, which then handles the remaining request processing.

**Without the pattern**, all nested paths would have to be defined directly in `SensorResource`:

```java
@GET  @Path("/{id}/readings")       public Response getReadings(...) { ... }
@POST @Path("/{id}/readings")       public Response addReading(...) { ... }
@GET  @Path("/{id}/readings/{rid}") public Response getReading(...) { ... }
```

As the API grows, `SensorResource` accumulates methods for both sensor management and reading history management. The class becomes a "God class" — large, difficult to navigate, and violating the Single Responsibility Principle. Changing how readings are stored or formatted requires modifying the sensor file, creating the risk of accidentally introducing bugs in unrelated sensor endpoints.

**With the pattern**, `SensorResource` delegates all reading logic with a single method:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

`SensorReadingResource` is a dedicated class responsible solely for reading operations. Each class has one job — sensor logic in `SensorResource`, reading logic in `SensorReadingResource` — which is the **Single Responsibility Principle**.

In a large production API with dozens of resource types, each potentially having multiple levels of nesting, this separation is essential. It enables parallel development (different developers can work on `SensorResource` and `SensorReadingResource` simultaneously without merge conflicts), isolated unit testing of each class, and significantly easier onboarding for new team members who need only understand one class at a time.

---

### Part 5.2 — Why HTTP 422 is More Semantically Accurate Than 404

**HTTP 404 Not Found** communicates that the URL requested does not exist on the server. When a client sends `POST /api/v1/sensors`, the endpoint clearly exists and is reachable — responding with `404` would be factually incorrect and misleading, as it would suggest the client called the wrong URL.

The actual problem is different in nature: the request URL is correct, the JSON body is syntactically valid, but the `roomId` field inside the body references a room that does not exist in the system. This is a **semantic error** — the data violates a business rule, not the request structure.

**HTTP 422 Unprocessable Entity** is the appropriate status code for this scenario. Its specification in RFC 4918 states: *"The server understands the content type of the request entity, and the syntax of the request entity is correct, but it was unable to process the contained instructions."* This matches the situation exactly: the server received valid JSON but cannot act on it because an internal reference is broken.

Using `422` rather than `404` is also practically valuable for client developers. A `404` response would lead a developer to check whether they called the correct URL, wasting debugging time. A `422` response immediately communicates: *"Your URL is correct, your JSON is valid, but something inside your request body is wrong."* The error body in this API further specifies the `referencedResource` type and `referencedId`, allowing the client to identify and fix the issue precisely.

---

### Part 5.4 — Cybersecurity Risks of Exposing Java Stack Traces

Exposing raw Java stack traces in HTTP responses to external API consumers creates multiple serious security vulnerabilities:

**1. Technology fingerprinting:** Stack traces typically include the names and versions of all frameworks and libraries in the call stack, for example `jersey-server-2.41`, `grizzly-http-2.3.35`, or `jackson-databind-2.14`. An attacker can search public vulnerability databases (such as the National Vulnerability Database) for known CVEs affecting those exact versions and craft an exploit targeting a specific weakness.

**2. Internal architecture disclosure:** Package and class names — such as `com.smartcampus.store.DataStore` or `com.smartcampus.resource.SensorResource` — expose the internal structure and naming conventions of the application. This allows an attacker to build an accurate mental model of how the codebase is organised, which components handle which responsibilities, and where sensitive operations are likely to reside.

**3. Precise failure location:** Stack traces include file names and line numbers, for example `DataStore.java:87`. An attacker who knows that line 87 of `DataStore.java` throws a `NullPointerException` under a certain input can craft repeated requests to reliably trigger the same crash, enabling targeted denial-of-service attacks or deeper exploitation of that specific code path.

**4. Business logic leakage:** Method names visible in a stack trace — such as `validateAdminPermission()`, `generateAuthToken()`, or `decryptPayload()` — reveal the existence and internal location of sensitive operations. An attacker who identifies these methods can focus their efforts on subverting them specifically.

**5. Sensitive data in exception messages:** Certain exceptions include runtime values in their messages, such as `NullPointerException: Cannot read field 'password' because 'user' is null`. This can inadvertently expose field names, internal identifiers, or other data that should remain server-side.

The mitigation implemented in this API is the `GlobalExceptionMapper`, which intercepts every unhandled `Throwable`, logs the full stack trace internally via `java.util.logging.Logger` at `SEVERE` level (accessible only to server administrators), and returns a generic `"An unexpected error occurred"` message to the client — providing full observability for developers while leaking nothing to external consumers.

---

### Part 5.5 — JAX-RS Filters vs Manual Logging in Resource Methods

Inserting `Logger.info()` statements manually inside every resource method is a valid approach for small APIs with a handful of endpoints. However, it violates the **DRY (Don't Repeat Yourself)** principle and creates several practical problems as the API grows. Logging is a **cross-cutting concern** — it applies uniformly to every endpoint regardless of business logic — making it an ideal candidate for the filter mechanism.

**The problems with manual logging:**

Every new resource method must have its own `Logger.info()` calls added. Developers writing new endpoints may forget, creating blind spots in observability. Changing the log format — for example, adding a correlation request ID — requires editing every resource method individually, which is time-consuming and error-prone.

**The advantages of using JAX-RS filters:**

**Single implementation:** The `ApiLoggingFilter` class, annotated with `@Provider`, automatically applies to every request and response across the entire application. Adding a new resource class or method requires zero additional logging code.

**Consistency:** Because the filter runs unconditionally, every request is logged with identical formatting. There are no gaps in the log, regardless of how many endpoints exist or who wrote them.

**Separation of concerns:** Resource methods contain only business logic. Logging, authentication, CORS header injection, and rate limiting all belong in filters. This keeps resource classes focused, shorter, and easier to unit test in isolation — tests for `RoomResource` do not need to account for logging behaviour.

**Easy modification:** Changing the log format, adding structured logging fields, or disabling logging entirely requires modifying one class. The change propagates to every endpoint automatically.

**Correct timing guarantees:** `ContainerRequestFilter` runs before the resource method is invoked; `ContainerResponseFilter` runs after the full response has been constructed, including any modifications made by exception mappers. This guarantees that the status code recorded in the log always reflects the actual status code sent to the client — something a `Logger.info()` call placed at the start of a resource method cannot guarantee, because an exception thrown later in the method may result in a different status code being returned.
