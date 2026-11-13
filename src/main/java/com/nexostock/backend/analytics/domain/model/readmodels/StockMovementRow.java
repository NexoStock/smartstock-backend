package com.nexostock.backend.analytics.domain.model.readmodels;

import java.time.Instant;

/**
 * Stock movement as shown in reports: direction (IN or OUT), source and positive quantity.
 */
public record StockMovementRow(
        Long id,
        Long productId,
        String productName,
        String direction,
        String source,
        String sourceId,
        int quantity,
        Instant date) {
}
