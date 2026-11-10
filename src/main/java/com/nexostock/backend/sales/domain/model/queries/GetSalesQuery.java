package com.nexostock.backend.sales.domain.model.queries;

import java.time.Instant;
import java.util.Objects;

public record GetSalesQuery(
        Long businessId,
        Instant from,
        Instant to) {

    public GetSalesQuery {
        Objects.requireNonNull(businessId, "businessId is required");

        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException(
                    "from must be before to");
        }
    }
}