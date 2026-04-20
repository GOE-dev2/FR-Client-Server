package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PART 5.1 – Maps RoomNotEmptyException → HTTP 409 Conflict
 *
 * @Provider registers this mapper with JAX-RS automatically.
 * When RoomNotEmptyException is thrown anywhere, JAX-RS calls toResponse()
 * and returns the structured JSON body — no stack trace, no 500 error.
 */
@Provider
public class RoomNotEmptyExceptionMapper
        implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",      409);
        body.put("error",       "Conflict");
        body.put("message",     ex.getMessage());
        body.put("roomId",      ex.getRoomId());
        body.put("sensorCount", ex.getSensorCount());
        body.put("hint",        "Remove or reassign all sensors from this room before deleting it.");
        body.put("timestamp",   System.currentTimeMillis());

        return Response
                .status(Response.Status.CONFLICT)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
