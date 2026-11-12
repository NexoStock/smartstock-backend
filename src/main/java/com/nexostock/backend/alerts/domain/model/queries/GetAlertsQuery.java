package com.nexostock.backend.alerts.domain.model.queries;

import java.util.Objects;

public record GetAlertsQuery(Long businessId) {

    public GetAlertsQuery {
        Objects.requireNonNull(businessId, "businessId is required");
    }
}