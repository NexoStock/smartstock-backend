package com.nexostock.backend.analytics.domain.model.readmodels;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Day summary of the business (M17).
 */
public record DashboardSummary(
        Totals salesToday,
        Totals purchasesToday,
        long lowStockAlerts,
        List<Activity> recentActivity,
        List<StockSummary> stock) {

    public record Totals(BigDecimal total, long count) {
    }

    /** type is SALE or PURCHASE. */
    public record Activity(String type, String id, BigDecimal total, Instant date) {
    }

    public record StockSummary(Long productId, String name, int units, String level, String sensor) {
    }
}
