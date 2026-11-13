package com.nexostock.backend.devices.domain.model.commands;

import java.util.Objects;

/**
 * Releases a sensor so it becomes available again.
 * Libera un sensor para que vuelva a estar disponible.
 */
public record UnlinkSensorCommand(Long businessId, Long sensorId) {

    public UnlinkSensorCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(sensorId, "sensorId is required");
    }
}
