
package com.nexostock.backend.purchasing.domain.model.commands;

import java.util.Objects;

/**
 * Command to update supplier information.
 * Comando para actualizar un proveedor.
 */
public record UpdateSupplierCommand(
        Long businessId,
        Long supplierId,
        String name,
        String taxId,
        String email,
        String phone,
        String address
) {

    public UpdateSupplierCommand {

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

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Supplier name is required"
            );
        }

        if (taxId == null || taxId.isBlank()) {
            throw new IllegalArgumentException(
                    "Supplier tax ID is required"
            );
        }

        name = name.trim();
        taxId = taxId.trim();

        if (name.length() > 255) {
            throw new IllegalArgumentException(
                    "Supplier name must have at most 255 characters"
            );
        }

        if (taxId.length() > 20) {
            throw new IllegalArgumentException(
                    "Supplier tax ID must have at most 20 characters"
            );
        }

        email = email == null ? "" : email.trim();
        phone = phone == null ? "" : phone.trim();
        address = address == null ? "" : address.trim();
    }
}
