package com.nexostock.backend.sales.domain.model.queries;

import java.util.Objects;

public record GetSaleByCodeQuery(Long businessId, String code) {

    public GetSaleByCodeQuery {
        Objects.requireNonNull(businessId, "businessId is required");

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
    }
}