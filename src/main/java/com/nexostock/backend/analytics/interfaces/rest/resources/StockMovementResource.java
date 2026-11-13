package com.nexostock.backend.analytics.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Stock movement: tipo is IN or OUT and origen is SALE, PURCHASE or ADJUSTMENT.
 */
public record StockMovementResource(
        @JsonProperty("id") Long id,
        @JsonProperty("productoId") Long productId,
        @JsonProperty("productoNombre") String productName,
        @JsonProperty("tipo") String direction,
        @JsonProperty("origen") String source,
        @JsonProperty("origenId") String sourceId,
        @JsonProperty("cantidad") int quantity,
        @JsonProperty("fecha") Instant date) {
}
