package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * PART 4.2 – Sensor Reading Sub-Resource
 *
 * Handles:  GET  /api/v1/sensors/{sensorId}/readings   – reading history
 *           POST /api/v1/sensors/{sensorId}/readings   – record a new reading
 *
 * This class has NO @Path at class level. It is instantiated and returned by
 * SensorResource's sub-resource locator method. JAX-RS then dispatches the
 * HTTP verb to the correct method in this class.
 *
 * The sensorId is passed in via the constructor, giving this class the
 * context it needs without any global state.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String    sensorId;
    private final DataStore store;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
        this.store    = DataStore.getInstance();
    }

    // ── GET /api/v1/sensors/{sensorId}/readings ───────────────────────────────
    @GET
    public Response getReadings() {

        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found",
                            "Sensor '" + sensorId + "' does not exist."))
                    .build();
        }

        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    // ── POST /api/v1/sensors/{sensorId}/readings ──────────────────────────────
    @POST
    public Response addReading(SensorReading reading) {

        // 1. Sensor must exist
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found",
                            "Sensor '" + sensorId + "' does not exist."))
                    .build();
        }

        // 2. STATE CONSTRAINT: only ACTIVE sensors accept readings
        String status = sensor.getStatus();
        if ("MAINTENANCE".equalsIgnoreCase(status) || "OFFLINE".equalsIgnoreCase(status)) {
            throw new SensorUnavailableException(sensorId, status);  // → 403
        }

        // 3. Validate: value must be included
        // (Java default for double is 0.0 which is valid, so no null check needed)

        // 4. Auto-generate UUID if client did not provide one
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // 5. Auto-set timestamp if client did not provide one
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // 6. Persist the reading
        store.getReadingsForSensor(sensorId).add(reading);

        // 7. SIDE EFFECT: update parent sensor's currentValue to stay in sync
        sensor.setCurrentValue(reading.getValue());

        URI location = URI.create(
                "/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());
        return Response.created(location).entity(reading).build();   // 201 Created
    }
}
