package com.nexostock.backend.analytics.domain.model.readmodels;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Sensor of a product with its latest readings converted to units (M28).
 */
public record ProductSensorDetail(SensorInfo sensor, List<Reading> readings) {

    public record SensorInfo(
            Long id,
            String code,
            String status,
            BigDecimal weightKg,
            int units,
            Instant lastReadingAt) {
    }

    public record Reading(Long id, Instant measuredAt, BigDecimal weightKg, int units) {
    }
}
