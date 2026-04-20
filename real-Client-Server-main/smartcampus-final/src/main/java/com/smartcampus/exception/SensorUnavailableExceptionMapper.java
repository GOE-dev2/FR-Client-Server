package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PART 5.3 – Maps SensorUnavailableException → HTTP 403 Forbidden
 *
 * The server understood and located the resource but is refusing the operation
 * because the sensor's current state does not permit it.
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",        403);
        body.put("error",         "Forbidden");
        body.put("message",       ex.getMessage());
        body.put("sensorId",      ex.getSensorId());
        body.put("currentStatus", ex.getCurrentStatus());
        body.put("hint",          "Only sensors with status 'ACTIVE' can receive readings.");
        body.put("timestamp",     System.currentTimeMillis());

        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
