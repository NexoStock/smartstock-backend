package com.nexostock.backend.devices.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Answer with the device key. It is shown only once:
 * the device must keep it and send it in X-Device-Key.
 * La clave se muestra una sola vez.
 */
public record SensorKeyResource(

        Long id,

        @JsonProperty("codigo")
        String code,

        @JsonProperty("claveDispositivo")
        String deviceKey

) {
}
