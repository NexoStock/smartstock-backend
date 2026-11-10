package com.nexostock.backend.devices.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Sensor returned by the API; same fields as the Angular SensorResource.
 * Sensor que devuelve la API, igual al modelo de Angular.
 */
public record SensorResource(

        Long id,

        @JsonProperty("codigo")
        String code,

        @JsonProperty("productoId")
        Long productId,

        @JsonProperty("productoNombre")
        String productName,

        @JsonProperty("estado")
        String status,

        @JsonProperty("ultimaLecturaAt")
        Instant lastReadingAt,

        @JsonProperty("pesoKg")
        BigDecimal weightKg,

        @JsonProperty("unidades")
        Integer units

) {
}
