
package com.nexostock.backend.catalog.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Product returned by the API.
 * Producto que devuelve la API.
 */
public record ProductResource(

        Long id,

        @JsonProperty("nombre")
        String name,

        @JsonProperty("sku")
        String sku,

        @JsonProperty("categoria")
        String category,

        @JsonProperty("pesoUnitario")
        BigDecimal unitWeightKg,

        @JsonProperty("precioVenta")
        BigDecimal salePrice,

        @JsonProperty("costoCompra")
        BigDecimal purchaseCost,

        @JsonProperty("proveedorHabitual")
        String usualSupplier,

        @JsonProperty("stockRegistrado")
        int registeredStock,

        @JsonProperty("umbralMinimo")
        int minimumThreshold,

        @JsonProperty("capacidadMaxima")
        int maximumCapacity

) {
}
