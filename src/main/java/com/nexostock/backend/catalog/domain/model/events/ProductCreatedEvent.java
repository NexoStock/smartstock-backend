
package com.nexostock.backend.catalog.domain.model.events;

public record ProductCreatedEvent(
        Long productId,
        Long businessId,
        int initialStock) {
}
