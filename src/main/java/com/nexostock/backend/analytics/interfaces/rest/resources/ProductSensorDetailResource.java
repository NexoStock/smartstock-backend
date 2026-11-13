package com.nexostock.backend.analytics.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Sensor of a product and its latest readings; sensor is null when the product has no sensor.
 */
public record ProductSensorDetailResource(
        @JsonProperty("sensor") SensorResource sensor,
        @JsonProperty("lecturas") List<ReadingResource> readings) {

    public record SensorResource(
            @JsonProperty("id") Long id,
            @JsonProperty("codigo") String code,
            @JsonProperty("estado") String status,
            @JsonProperty("pesoKg") BigDecimal weightKg,
            @JsonProperty("unidades") int units,
            @JsonProperty("ultimaLecturaAt") Instant lastReadingAt) {
    }

    public record ReadingResource(
            @JsonProperty("id") Long id,
            @JsonProperty("fecha") Instant measuredAt,
            @JsonProperty("pesoKg") BigDecimal weightKg,
            @JsonProperty("unidades") int units) {
    }
}
