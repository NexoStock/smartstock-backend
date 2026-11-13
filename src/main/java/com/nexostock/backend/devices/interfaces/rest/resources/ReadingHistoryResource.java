package com.nexostock.backend.devices.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * One stored reading of a sensor.
 * Una lectura guardada del sensor.
 */
public record ReadingHistoryResource(

        Long id,

        @JsonProperty("pesoKg")
        BigDecimal weightKg,

        @JsonProperty("medidoEn")
        Instant measuredAt

) {
}
