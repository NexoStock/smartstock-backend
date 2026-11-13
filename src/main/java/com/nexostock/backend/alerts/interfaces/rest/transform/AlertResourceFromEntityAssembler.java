package com.nexostock.backend.alerts.interfaces.rest.transform;

import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.interfaces.rest.resources.AlertResource;
import com.nexostock.backend.catalog.interfaces.acl.ProductSnapshot;

public class AlertResourceFromEntityAssembler {

    private AlertResourceFromEntityAssembler() {}

    public static AlertResource toResource(
            Alert alert, ProductSnapshot product) {

        return new AlertResource(
                alert.getId(),
                alert.getProductId(),
                product == null ? "" : product.name(),
                alert.getType().name(),
                alert.isActive() ? "ACTIVE" : "RESOLVED",
                alert.getReferenceStock(),
                "registered",
                null,
                product == null ? null : product.registeredStock(),
                product == null ? null : product.minimumThreshold(),
                alert.getCreatedAt(),
                alert.getResolvedAt());
    }
}