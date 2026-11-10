package com.nexostock.backend.devices.domain.model.commands;

import java.util.Objects;

/**
 * Removes a sensor and its readings from the business.
 * Elimina un sensor y sus lecturas.
 */
public record DeleteSensorCommand(Long businessId, Long sensorId) {

    public DeleteSensorCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(sensorId, "sensorId is required");
    }
}
