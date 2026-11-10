
package com.nexostock.backend.purchasing.domain.model.queries;

import java.util.Objects;

/**
 * Query to list all purchases of a business.
 *
 * Consulta para listar todas las compras
 * de un negocio.
 */
public record GetAllPurchasesQuery(
        Long businessId
) {

    public GetAllPurchasesQuery {

        Objects.requireNonNull(
                businessId,
                "businessId is required"
        );

        if (businessId <= 0) {
            throw new IllegalArgumentException(
                    "businessId must be positive"
            );
        }
    }
}
