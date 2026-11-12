package com.nexostock.backend.alerts.interfaces.rest.transform;

import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.interfaces.rest.resources.AlertResource;

public class AlertResourceFromEntityAssembler {

    private AlertResourceFromEntityAssembler() {}

    public static AlertResource toResource(Alert a) {
        return new AlertResource(
                a.getId(),
                a.getProductId(),
                a.getType().name(),
                a.isActive() ? "ACTIVE" : "RESOLVED",
                a.getReferenceStock(),
                a.getCreatedAt(),
                a.getResolvedAt());
    }
}