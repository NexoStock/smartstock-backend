
package com.nexostock.backend.purchasing.interfaces.rest.transform;

import com.nexostock.backend.purchasing.domain.model.commands.UpdateSupplierCommand;
import com.nexostock.backend.purchasing.interfaces.rest.resources.SaveSupplierResource;

/**
 * Converts an HTTP request into a command
 * for updating a supplier.
 */
public final class UpdateSupplierCommandFromResourceAssembler {

    private UpdateSupplierCommandFromResourceAssembler() {
    }

    public static UpdateSupplierCommand toCommand(
            Long businessId,
            Long supplierId,
            SaveSupplierResource resource) {

        if (resource == null) {
            throw new IllegalArgumentException(
                    "Supplier resource is required"
            );
        }

        return new UpdateSupplierCommand(
                businessId,
                supplierId,
                resource.name(),
                resource.taxId(),
                resource.email(),
                resource.phone(),
                resource.address()
        );
    }
}
