
package com.nexostock.backend.purchasing.domain.model.commands;

import java.util.Objects;

/**
 * Command to deactivate a supplier.
 * Comando para desactivar un proveedor.
 */
public record DeactivateSupplierCommand(
        Long businessId,
        Long supplierId
) {

    public DeactivateSupplierCommand {

        Objects.requireNonNull(
                businessId,
                "businessId is required"
        );

        Objects.requireNonNull(
                supplierId,
                "supplierId is required"
        );

        if (businessId <= 0 || supplierId <= 0) {
            throw new IllegalArgumentException(
                    "Business ID and supplier ID must be positive"
            );
        }
    }
}
