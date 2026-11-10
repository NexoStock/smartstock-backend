
package com.nexostock.backend.purchasing.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Producto incluido en una compra")
public record PurchaseItemResource(

        @JsonProperty("productoId")
        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El ID del producto debe ser positivo")
        @Schema(example = "1")
        Long productId,

        @JsonProperty("cantidad")
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        @Schema(example = "10")
        Integer quantity,

        @JsonProperty("precioUnitario")
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(
                value = "0.01",
                message = "El precio debe ser al menos 0.01"
        )
        @Digits(
                integer = 14,
                fraction = 2,
                message = "El precio debe tener como máximo 2 decimales"
        )
        @Schema(example = "15.50")
        BigDecimal unitPrice

) {
}
