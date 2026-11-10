package com.nexostock.backend.alerts.domain.model.queries;

import java.util.Objects;

public record GetChannelsQuery(Long businessId) {

    public GetChannelsQuery {
        Objects.requireNonNull(businessId, "businessId is required");
    }
}