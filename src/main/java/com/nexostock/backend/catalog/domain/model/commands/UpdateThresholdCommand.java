
package com.nexostock.backend.catalog.domain.model.commands;

import java.util.Objects;

public record UpdateThresholdCommand(
        Long businessId,
        Long productId,
        int minimumThreshold) {

    public UpdateThresholdCommand {
        Objects.requireNonNull(
                businessId, "businessId is required"
        );

        Objects.requireNonNull(
                productId, "productId is required"
        );

        if (minimumThreshold < 1) {
            throw new IllegalArgumentException(
                    "minimumThreshold must be at least 1"
            );
        }
    }
}
