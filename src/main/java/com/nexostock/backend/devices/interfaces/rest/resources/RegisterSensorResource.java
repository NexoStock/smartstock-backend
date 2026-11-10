package com.nexostock.backend.devices.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body to register a sensor.
 * Datos para registrar un sensor.
 */
public record RegisterSensorResource(

        @NotBlank
        @Size(max = 60)
        @JsonProperty("codigo")
        String code

) {
}
