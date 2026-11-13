
package com.nexostock.backend.inventory.domain.model.queries;

import java.util.Objects;

public record GetMovementsByProductQuery(
        Long businessId,
        Long productId) {

    public GetMovementsByProductQuery {
        Objects.requireNonNull(
                businessId, "businessId is required"
        );

        Objects.requireNonNull(
                productId, "productId is required"
        );
    }
}
