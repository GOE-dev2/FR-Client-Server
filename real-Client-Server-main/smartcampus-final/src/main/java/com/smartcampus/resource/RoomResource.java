package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path("/api/v1/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        try {
            List<Room> rooms = new ArrayList<>(store.getRooms().values());
            return Response.ok(rooms).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Internal Server Error",
                            "Failed to fetch rooms: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    public Response createRoom(Room room) {
        try {
            if (room == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "Bad Request",
                                "Request body is required."))
                        .build();
            }

            if (room.getId() == null || room.getId().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "Bad Request",
                                "Field 'id' is required."))
                        .build();
            }

            if (room.getName() == null || room.getName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "Bad Request",
                                "Field 'name' is required."))
                        .build();
            }

            if (room.getCapacity() < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "Bad Request",
                                "Field 'capacity' must be 0 or greater."))
                        .build();
            }

            if (store.getRooms().containsKey(room.getId())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse(409, "Conflict",
                                "Room with ID '" + room.getId() + "' already exists."))
                        .build();
            }

            if (room.getSensorIds() == null) {
                room.setSensorIds(new ArrayList<>());
            }

            store.getRooms().put(room.getId(), room);

            URI location = UriBuilder.fromPath("/api/v1/rooms/{id}")
                    .build(room.getId());

            return Response.created(location)
                    .entity(room)
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Internal Server Error",
                            "Failed to create room: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        try {
            Room room = store.getRooms().get(roomId);

            if (room == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse(404, "Not Found",
                                "Room '" + roomId + "' does not exist."))
                        .build();
            }

            return Response.ok(room).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Internal Server Error",
                            "Failed to fetch room: " + e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        try {
            Room room = store.getRooms().get(roomId);

            if (room == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse(404, "Not Found",
                                "Room '" + roomId + "' does not exist."))
                        .build();
            }

            if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
                throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
            }

            store.getRooms().remove(roomId);
            return Response.noContent().build();

        } catch (RoomNotEmptyException e) {
            throw e;
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Internal Server Error",
                            "Failed to delete room: " + e.getMessage()))
                    .build();
        }
    }
}