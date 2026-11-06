
package com.nexostock.backend.catalog.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request body to create or update a product.
 *
 * The JSON field names remain in Spanish
 * for compatibility with the Angular frontend.
 */
@Schema(description = "Data to create or update a product")
public record SaveProductResource(

        @JsonProperty("nombre")
        @NotBlank
        @Size(max = 255)
        @Schema(example = "Arroz Costeño 5 kg")
        String name,

        @JsonProperty("sku")
        @Size(max = 64)
        @Schema(example = "ARR-005")
        String sku,

        @JsonProperty("categoria")
        @NotBlank
        @Size(max = 255)
        @Schema(example = "Abarrotes")
        String category,

        @JsonProperty("pesoUnitario")
        @NotNull
        @Schema(example = "5.0")
        BigDecimal unitWeightKg,

        @JsonProperty("precioVenta")
        @Schema(example = "24.90")
        BigDecimal salePrice,

        @JsonProperty("costoCompra")
        @Schema(example = "21.50")
        BigDecimal purchaseCost,

        @JsonProperty("proveedorHabitual")
        @Size(max = 255)
        @Schema(example = "Distribuidora Lima")
        String usualSupplier,

        @JsonProperty("stockRegistrado")
        @PositiveOrZero
        @Schema(example = "40")
        Integer initialStock,

        @JsonProperty("umbralMinimo")
        @NotNull
        @Min(1)
        @Schema(example = "10")
        Integer minimumThreshold,

        @JsonProperty("capacidadMaxima")
        @Min(1)
        @Schema(example = "100")
        Integer maximumCapacity

) {
}
