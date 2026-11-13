
package com.nexostock.backend.purchasing.domain.model.queries;

import java.util.Objects;

/**
 * Query to get all active suppliers
 * belonging to a business.
 *
 * Consulta para obtener todos los proveedores
 * activos de un negocio.
 */
public record GetAllSuppliersQuery(
        Long businessId
) {

    public GetAllSuppliersQuery {

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
