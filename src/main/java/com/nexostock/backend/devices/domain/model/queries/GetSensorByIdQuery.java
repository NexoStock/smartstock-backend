package com.nexostock.backend.devices.domain.model.queries;

import java.util.Objects;

/**
 * One sensor of the business.
 * Detalle de un sensor del negocio.
 */
public record GetSensorByIdQuery(Long businessId, Long sensorId) {

    public GetSensorByIdQuery {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(sensorId, "sensorId is required");
    }
}
