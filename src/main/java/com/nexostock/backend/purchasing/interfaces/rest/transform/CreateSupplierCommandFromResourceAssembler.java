
package com.nexostock.backend.purchasing.interfaces.rest.transform;

import com.nexostock.backend.purchasing.domain.model.commands.CreateSupplierCommand;
import com.nexostock.backend.purchasing.interfaces.rest.resources.SaveSupplierResource;

/**
 * Converts an HTTP request into a command
 * for creating a supplier.
 */
public final class CreateSupplierCommandFromResourceAssembler {

    private CreateSupplierCommandFromResourceAssembler() {
    }

    public static CreateSupplierCommand toCommand(
            Long businessId,
            SaveSupplierResource resource) {

        if (resource == null) {
            throw new IllegalArgumentException(
                    "Supplier resource is required"
            );
        }

        return new CreateSupplierCommand(
                businessId,
                resource.name(),
                resource.taxId(),
                resource.email(),
                resource.phone(),
                resource.address()
        );
    }
}
