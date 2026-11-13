package com.nexostock.backend.alerts.domain.model.queries;

import java.util.Objects;

public record GetNotificationsQuery(Long businessId) {

    public GetNotificationsQuery {
        Objects.requireNonNull(businessId, "businessId is required");
    }
}