package com.nexostock.backend.analytics.domain.model.readmodels;

import java.math.BigDecimal;
import java.util.List;

/**
 * Totals and stock movements of a period (M18). A pending purchase does not move stock yet.
 */
public record Report(
        BigDecimal salesTotal,
        BigDecimal purchasesTotal,
        List<StockMovementRow> movements,
        List<String> pendingPurchases) {
}
