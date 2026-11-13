package com.nexostock.backend.analytics.domain.model.queries;

/**
 * Sensor of a product with its latest readings (TS02, M28).
 */
public record GetProductSensorDetailQuery(Long businessId, Long productId) {
}
