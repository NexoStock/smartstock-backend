package com.nexostock.backend.devices.domain.model.commands;

import java.util.Objects;

/**
 * Registers a sensor of a business; the service generates its key.
 * Registra un sensor; la clave la genera el servicio.
 */
public record RegisterSensorCommand(Long businessId, String deviceCode) {

    public RegisterSensorCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        if (deviceCode == null
                || deviceCode.isBlank()
                || deviceCode.trim().length() > 60) {
            throw new IllegalArgumentException(
                    "deviceCode is required (max 60 characters)");
        }
        deviceCode = deviceCode.trim();
    }
}
