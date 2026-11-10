
package com.nexostock.backend.purchasing.domain.model.queries;

import java.util.Objects;

/**
 * Query to get a supplier by its ID.
 *
 * Consulta para obtener un proveedor
 * específico de un negocio.
 */
public record GetSupplierByIdQuery(
        Long businessId,
        Long supplierId
) {

    public GetSupplierByIdQuery {

        Objects.requireNonNull(
                businessId,
                "businessId is required"
        );

        Objects.requireNonNull(
                supplierId,
                "supplierId is required"
        );

        if (businessId <= 0) {
            throw new IllegalArgumentException(
                    "businessId must be positive"
            );
        }

        if (supplierId <= 0) {
            throw new IllegalArgumentException(
                    "supplierId must be positive"
            );
        }
    }
}
