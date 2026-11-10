
package com.nexostock.backend.purchasing.domain.model.commands;

import java.util.Objects;

/**
 * Command to create a supplier.
 * Comando para registrar un proveedor.
 */
public record CreateSupplierCommand(
        Long businessId,
        String name,
        String taxId,
        String email,
        String phone,
        String address
) {

    public CreateSupplierCommand {

        Objects.requireNonNull(
                businessId,
                "businessId is required"
        );

        if (businessId <= 0) {
            throw new IllegalArgumentException(
                    "businessId must be positive"
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
