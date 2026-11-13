package com.nexostock.backend.analytics.domain.model.readmodels;

import java.math.BigDecimal;

/**
 * Comparison of one product: registered stock against the units measured by its sensor.
 */
public record ComparisonRow(
        Long productId,
        String productName,
        int registeredStock,
        BigDecimal weightKg,
        Integer physicalUnits,
        BigDecimal differencePercent,
        String result) {

    public static final String MATCH = "MATCH";
    public static final String DISCREPANCY = "DISCREPANCY";
    public static final String REGISTERED_ONLY = "REGISTERED_ONLY";
}
