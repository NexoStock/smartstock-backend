package com.nexostock.backend.devices.domain.model.queries;

import java.util.Objects;

/**
 * Latest readings of a sensor, newest first.
 * Últimas lecturas de un sensor.
 */
public record GetLatestReadingsQuery(Long businessId, Long sensorId) {

    public GetLatestReadingsQuery {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(sensorId, "sensorId is required");
    }
}
