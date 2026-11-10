
package com.nexostock.backend.purchasing.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar o actualizar un proveedor")
public record SaveSupplierResource(

        @JsonProperty("nombre")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255)
        @Schema(example = "Distribuidora Lima SAC")
        String name,

        @JsonProperty("ruc")
        @NotBlank(message = "El documento tributario es obligatorio")
        @Size(max = 20)
        @Schema(example = "20123456789")
        String taxId,

        @JsonProperty("correo")
        @Size(max = 255)
        @Schema(example = "ventas@proveedor.com")
        String email,

        @JsonProperty("telefono")
        @Size(max = 30)
        @Schema(example = "987654321")
        String phone,

        @JsonProperty("direccion")
        @Size(max = 255)
        @Schema(example = "Av. Principal 123, Lima")
        String address

) {
}
