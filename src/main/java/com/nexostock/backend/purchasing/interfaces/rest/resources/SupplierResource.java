
package com.nexostock.backend.purchasing.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de un proveedor registrado")
public record SupplierResource(

        @JsonProperty("id")
        Long id,

        @JsonProperty("nombre")
        String name,

        @JsonProperty("ruc")
        String taxId,

        @JsonProperty("correo")
        String email,

        @JsonProperty("telefono")
        String phone,

        @JsonProperty("direccion")
        String address,

        @JsonProperty("activo")
        boolean active

) {
}
