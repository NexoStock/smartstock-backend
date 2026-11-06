
package com.nexostock.backend.catalog.domain.model.queries;

import java.util.Objects;

public record GetProductByIdQuery(
        Long businessId,
        Long productId) {

    public GetProductByIdQuery {
        Objects.requireNonNull(
                businessId, "businessId is required"
        );

        Objects.requireNonNull(
                productId, "productId is required"
        );
    }
}
