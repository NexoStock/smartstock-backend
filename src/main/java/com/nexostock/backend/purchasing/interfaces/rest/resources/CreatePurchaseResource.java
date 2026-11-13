
package com.nexostock.backend.purchasing.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "Datos para registrar una compra")
public record CreatePurchaseResource(

        @JsonProperty("proveedorId")
        @NotNull(message = "El proveedor es obligatorio")
        @Positive(message = "El ID del proveedor debe ser positivo")
        @Schema(example = "1")
        Long supplierId,

        @JsonProperty("productos")
        @NotEmpty(message = "La compra debe contener productos")
        List<@NotNull @Valid PurchaseItemResource> items

) {
}
