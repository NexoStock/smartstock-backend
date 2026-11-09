package com.nexostock.backend.alerts.domain.model.commands;

import java.util.Objects;

public record EvaluateLowStockCommand(
        Long businessId,
        Long productId,
        String productName,
        int referenceStock,
        int minimumThreshold) {

    public EvaluateLowStockCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(productId, "productId is required");
    }
}