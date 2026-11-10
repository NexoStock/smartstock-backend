package com.nexostock.backend.devices.domain.model.commands;

import java.util.Objects;

/**
 * Links an available sensor to a product of the same business.
 * Vincula un sensor disponible a un producto.
 */
public record LinkSensorCommand(Long businessId, Long sensorId, Long productId) {

    public LinkSensorCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(sensorId, "sensorId is required");
        Objects.requireNonNull(productId, "productId is required");
    }
}
