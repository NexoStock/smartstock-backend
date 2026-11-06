
package com.nexostock.backend.catalog.domain.model.commands;

import java.util.Objects;

public record RetireProductCommand(
        Long businessId,
        Long productId) {

    public RetireProductCommand {
        Objects.requireNonNull(
                businessId, "businessId is required"
        );

        Objects.requireNonNull(
                productId, "productId is required"
        );
    }
}
