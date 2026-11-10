
package com.nexostock.backend.purchasing.domain.model.queries;

import java.util.Objects;

/**
 * Query to get a purchase by its ID.
 *
 * Consulta para obtener una compra
 * específica de un negocio.
 */
public record GetPurchaseByIdQuery(
        Long businessId,
        Long purchaseId
) {

    public GetPurchaseByIdQuery {

        Objects.requireNonNull(
                businessId,
                "businessId is required"
        );

        Objects.requireNonNull(
                purchaseId,
                "purchaseId is required"
        );

        if (businessId <= 0) {
            throw new IllegalArgumentException(
                    "businessId must be positive"
            );
        }

        if (purchaseId <= 0) {
            throw new IllegalArgumentException(
                    "purchaseId must be positive"
            );
        }
    }
}
