package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.log(Level.SEVERE, "[GLOBAL SAFETY NET] Unhandled exception caught", exception);

        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            Response original = webEx.getResponse();
            int status = original.getStatus();
            String errorName = original.getStatusInfo().getReasonPhrase();
            String message = webEx.getMessage();

            if (message == null || message.trim().isEmpty()) {
                message = errorName;
            }

            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(status, errorName, message))
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(
                        500,
                        "Internal Server Error",
                        "An unexpected error occurred. Please try again later or contact the administrator."
                ))
                .build();
    }
}