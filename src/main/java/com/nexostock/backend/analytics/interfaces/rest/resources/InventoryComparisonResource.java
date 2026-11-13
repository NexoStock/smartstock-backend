package com.nexostock.backend.analytics.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Registered stock against the stock measured by the sensors.
 * resultado is MATCH, DISCREPANCY (difference above 10 percent) or REGISTERED_ONLY (no online sensor).
 */
public record InventoryComparisonResource(
        @JsonProperty("items") List<RowResource> items,
        @JsonProperty("discrepancias") long discrepancies,
        @JsonProperty("alertaId") Long alertId) {

    public record RowResource(
            @JsonProperty("productoId") Long productId,
            @JsonProperty("productoNombre") String productName,
            @JsonProperty("stockRegistrado") int registeredStock,
            @JsonProperty("pesoKg") BigDecimal weightKg,
            @JsonProperty("unidadesFisicas") Integer physicalUnits,
            @JsonProperty("diferenciaPct") BigDecimal differencePercent,
            @JsonProperty("resultado") String result) {
    }
}
