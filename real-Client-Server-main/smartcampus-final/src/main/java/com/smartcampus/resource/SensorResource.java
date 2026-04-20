package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PART 3 & 4 – Sensor Resource
 *
 * GET  /api/v1/sensors              – list all sensors (optional ?type= filter)
 * POST /api/v1/sensors              – register a new sensor (validates roomId)
 * GET  /api/v1/sensors/{sensorId}   – get one sensor
 *      /api/v1/sensors/{id}/readings – sub-resource locator → SensorReadingResource
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // ── GET /api/v1/sensors  (+ optional ?type=CO2) ───────────────────────────
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {

        List<Sensor> all = new ArrayList<>(store.getSensors().values());

        if (type != null && !type.trim().isEmpty()) {
            all = all.stream()
                     .filter(s -> s.getType().equalsIgnoreCase(type.trim()))
                     .collect(Collectors.toList());
        }

        return Response.ok(all).build();
    }

    // ── POST /api/v1/sensors ──────────────────────────────────────────────────
    @POST
    public Response createSensor(Sensor sensor) {

        // Validate id
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Bad Request",
                            "Field 'id' is required."))
                    .build();
        }

        // Validate type
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Bad Request",
                            "Field 'type' is required."))
                    .build();
        }

        // Validate roomId present
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Bad Request",
                            "Field 'roomId' is required."))
                    .build();
        }

        // Duplicate sensor check
        if (store.getSensors().containsKey(sensor.getId())) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(409, "Conflict",
                            "Sensor with ID '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // REFERENTIAL INTEGRITY: roomId must point to an existing room
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room == null) {
            // Throws → LinkedResourceNotFoundExceptionMapper → 422
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        // Persist sensor
        store.getSensors().put(sensor.getId(), sensor);

        // Link sensor ID into the room's sensorIds list
        room.getSensorIds().add(sensor.getId());

        // Initialise empty readings list for this sensor
        store.getSensorReadings().put(sensor.getId(), new ArrayList<>());

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();   // 201 Created
    }

    // ── GET /api/v1/sensors/{sensorId} ───────────────────────────────────────
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {

        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found",
                            "Sensor '" + sensorId + "' does not exist."))
                    .build();
        }

        return Response.ok(sensor).build();
    }

    // ── PART 4.1: Sub-Resource Locator ────────────────────────────────────────
    // No HTTP verb annotation here – this is the locator, not a handler.
    // JAX-RS calls this method and then delegates the actual GET/POST
    // to the returned SensorReadingResource instance.
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(
            @PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
