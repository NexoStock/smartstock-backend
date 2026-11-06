
package com.nexostock.backend.catalog.domain.model.events;

public record ThresholdChangedEvent(
        Long businessId,
        Long productId) {
}
