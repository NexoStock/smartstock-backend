package com.nexostock.backend.devices.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Body sent by the web app to link a sensor: { sensorId, productoId }.
 * Datos para vincular un sensor con un producto.
 */
public record LinkSensorResource(

        @NotNull
        @JsonProperty("sensorId")
        Long sensorId,

        @NotNull
        @JsonProperty("productoId")
        Long productId

) {
}
