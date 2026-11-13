package com.nexostock.backend.analytics.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Report of a period: totals, stock movements and pending purchases.
 */
public record ReportResource(
        @JsonProperty("desde") LocalDate from,
        @JsonProperty("hasta") LocalDate to,
        @JsonProperty("ventasTotal") BigDecimal salesTotal,
        @JsonProperty("comprasTotal") BigDecimal purchasesTotal,
        @JsonProperty("movimientos") List<StockMovementResource> movements,
        @JsonProperty("comprasPendientes") List<String> pendingPurchases) {
}
