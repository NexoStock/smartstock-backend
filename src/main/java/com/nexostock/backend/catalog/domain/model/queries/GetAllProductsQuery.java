
package com.nexostock.backend.catalog.domain.model.queries;

import java.util.Objects;

public record GetAllProductsQuery(Long businessId) {

    public GetAllProductsQuery {
        Objects.requireNonNull(
                businessId, "businessId is required"
        );
    }
}
