package com.nexostock.backend.devices.domain.model.commands;

import java.util.Objects;

/**
 * Replaces the device key of a sensor (for example, if it leaked).
 * Cambia la clave del dispositivo; la anterior deja de valer.
 */
public record RotateDeviceKeyCommand(Long businessId, Long sensorId) {

    public RotateDeviceKeyCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(sensorId, "sensorId is required");
    }
}
