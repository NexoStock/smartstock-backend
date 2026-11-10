package com.nexostock.backend.devices.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Body sent by the device: pesoKg in kilograms and
 * medidoEn (optional, ISO-8601 in UTC).
 * Lectura que envía el dispositivo.
 */
public record ReadingResource(

        @NotNull
        @PositiveOrZero
        @DecimalMax("100000")
        @JsonProperty("pesoKg")
        BigDecimal weightKg,

        @JsonProperty("medidoEn")
        Instant measuredAt

) {
}
