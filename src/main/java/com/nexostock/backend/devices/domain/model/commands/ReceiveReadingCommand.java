package com.nexostock.backend.devices.domain.model.commands;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A device sends a weight reading in kilograms.
 * Un dispositivo envía una lectura de peso.
 */
public record ReceiveReadingCommand(
        String deviceCode,
        String deviceKey,
        BigDecimal weightKg,
        Instant measuredAt) {

    public static final BigDecimal MAX_WEIGHT_KG = new BigDecimal("100000");

    /** Tolerance for devices whose clock is a little ahead. */
    public static final long CLOCK_SKEW_SECONDS = 60;

    public ReceiveReadingCommand {
        if (deviceCode == null || deviceCode.isBlank()) {
            throw new IllegalArgumentException("deviceCode is required");
        }
        if (weightKg == null
                || weightKg.signum() < 0
                || weightKg.compareTo(MAX_WEIGHT_KG) > 0) {
            throw new IllegalArgumentException(
                    "weightKg must be between 0 and 100000");
        }
        if (measuredAt == null) {
            // devices without a clock: the server time is used
            measuredAt = Instant.now();
        }
        if (measuredAt.isAfter(
                Instant.now().plusSeconds(CLOCK_SKEW_SECONDS))) {
            throw new IllegalArgumentException(
                    "measuredAt cannot be in the future");
        }
    }
}
