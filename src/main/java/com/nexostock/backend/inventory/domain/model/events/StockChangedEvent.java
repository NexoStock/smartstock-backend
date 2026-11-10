package com.nexostock.backend.inventory.domain.model.events;

/** Published after every stock movement; Alerts listens to it. */
public record StockChangedEvent(Long businessId, Long productId, int newStock, int minimumThreshold) {}