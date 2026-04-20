package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PART 5.2 – Maps LinkedResourceNotFoundException → HTTP 422 Unprocessable Entity
 *
 * Returns 422 (not 404) because the endpoint URL was found and reached correctly;
 * the problem is a semantic error inside the valid JSON request body.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",             422);
        body.put("error",              "Unprocessable Entity");
        body.put("message",            ex.getMessage());
        body.put("referencedResource", ex.getResourceType());
        body.put("referencedId",       ex.getReferencedId());
        body.put("hint",               "Create the referenced resource first, then retry.");
        body.put("timestamp",          System.currentTimeMillis());

        return Response
                .status(422)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
