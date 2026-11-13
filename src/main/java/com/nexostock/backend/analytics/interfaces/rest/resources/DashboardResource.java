package com.nexostock.backend.analytics.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Day summary. The JSON field names remain in Spanish for compatibility with the Angular frontend.
 */
public record DashboardResource(
        @JsonProperty("ventasHoy") TotalsResource salesToday,
        @JsonProperty("comprasHoy") TotalsResource purchasesToday,
        @JsonProperty("alertasStockBajo") long lowStockAlerts,
        @JsonProperty("actividadReciente") List<ActivityResource> recentActivity,
        @JsonProperty("resumenStock") List<StockSummaryResource> stock) {

    public record TotalsResource(
            @JsonProperty("total") BigDecimal total,
            @JsonProperty("cantidad") long count) {
    }

    public record ActivityResource(
            @JsonProperty("tipo") String type,
            @JsonProperty("id") String id,
            @JsonProperty("total") BigDecimal total,
            @JsonProperty("fecha") Instant date) {
    }

    public record StockSummaryResource(
            @JsonProperty("productoId") Long productId,
            @JsonProperty("nombre") String name,
            @JsonProperty("unidades") int units,
            @JsonProperty("nivel") String level,
            @JsonProperty("sensor") String sensor) {
    }
}
