package com.smartcampus.exception;

/**
 * PART 5.1 – Thrown when DELETE /rooms/{roomId} is called on a room
 * that still has sensors assigned to it.
 *
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {

    private final String roomId;
    private final int    sensorCount;

    public RoomNotEmptyException(String roomId, int sensorCount) {
        super("Room '" + roomId + "' cannot be deleted because it has "
              + sensorCount + " sensor(s) still assigned to it.");
        this.roomId      = roomId;
        this.sensorCount = sensorCount;
    }

    public String getRoomId()     { return roomId; }
    public int    getSensorCount(){ return sensorCount; }
}
