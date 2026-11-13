package com.nexostock.backend.analytics.domain.model.queries;

/**
 * Registered stock against the physical stock measured by the sensors (TS06, M39).
 */
public record GetInventoryComparisonQuery(Long businessId) {
}
