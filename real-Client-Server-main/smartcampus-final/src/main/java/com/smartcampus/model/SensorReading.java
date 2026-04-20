package com.smartcampus.model;

/**
 * SensorReading – a single timestamped measurement captured by a sensor.
 */
public class SensorReading {

    private String id;        // UUID assigned on creation
    private long   timestamp; // Epoch milliseconds
    private double value;     // Measured value

    // ── Constructors ──────────────────────────────────────────────────────────
    public SensorReading() {}

    public SensorReading(String id, long timestamp, double value) {
        this.id        = id;
        this.timestamp = timestamp;
        this.value     = value;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getId()               { return id; }
    public void   setId(String id)     { this.id = id; }

    public long   getTimestamp()        { return timestamp; }
    public void   setTimestamp(long t) { this.timestamp = t; }

    public double getValue()            { return value; }
    public void   setValue(double v)   { this.value = v; }
}
