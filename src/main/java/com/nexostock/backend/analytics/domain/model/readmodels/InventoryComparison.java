package com.nexostock.backend.analytics.domain.model.readmodels;

import java.util.List;

/**
 * Comparison of every product of the business and how many show a discrepancy.
 */
public record InventoryComparison(List<ComparisonRow> items, long discrepancies) {
}
