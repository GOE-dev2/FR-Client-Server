package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataStore – thread-safe in-memory "database".
 *
 * Implemented as an eager singleton so that one shared instance is used across
 * every JAX-RS resource class instance (which are request-scoped, not singletons).
 *
 * ConcurrentHashMap provides safe concurrent read/write access without external
 * synchronisation, preventing race conditions when multiple requests run in parallel.
 */
public class DataStore {

    // Eagerly initialised singleton
    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room>                rooms          = new ConcurrentHashMap<>();
    private final Map<String, Sensor>              sensors        = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        seedSampleData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public Map<String, Room>                getRooms()          { return rooms; }
    public Map<String, Sensor>              getSensors()        { return sensors; }
    public Map<String, List<SensorReading>> getSensorReadings() { return sensorReadings; }

    /**
     * Returns the readings list for a sensor, creating an empty list if none exists.
     * computeIfAbsent is atomic in ConcurrentHashMap – safe under concurrent access.
     */
    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }

    // ── Seed data ─────────────────────────────────────────────────────────────
    private void seedSampleData() {

        // --- Rooms ---
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("ENG-101", "Engineering Lab A",   30);

        // --- Sensors ---
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",      21.5,  "LIB-301");
        Sensor s2 = new Sensor("CO2-002",  "CO2",         "ACTIVE",      412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-003",  "Occupancy",   "MAINTENANCE",   0.0, "ENG-101");

        // Link sensors → rooms
        r1.getSensorIds().add("TEMP-001");
        r1.getSensorIds().add("CO2-002");
        r2.getSensorIds().add("OCC-003");

        // Persist everything
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Initialise empty reading lists
        sensorReadings.put("TEMP-001", new ArrayList<>());
        sensorReadings.put("CO2-002",  new ArrayList<>());
        sensorReadings.put("OCC-003",  new ArrayList<>());
    }
}
