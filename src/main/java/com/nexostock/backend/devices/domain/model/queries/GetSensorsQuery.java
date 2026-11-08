package com.nexostock.backend.devices.domain.model.queries;

import java.util.Objects;

/**
 * Sensors of a business with their status.
 * Sensores del negocio con su estado.
 */
public record GetSensorsQuery(Long businessId) {

    public GetSensorsQuery {
        Objects.requireNonNull(businessId, "businessId is required");
    }
}
