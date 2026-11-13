package com.nexostock.backend.analytics.domain.model.queries;

/**
 * Day summary of a business: sales, purchases, low stock alerts and stock levels (US15, M17).
 */
public record GetDashboardQuery(Long businessId) {
}
