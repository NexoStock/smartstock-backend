
package com.nexostock.backend.purchasing.interfaces.rest.transform;

import com.nexostock.backend.purchasing.domain.model.aggregates.Supplier;
import com.nexostock.backend.purchasing.interfaces.rest.resources.SupplierResource;

/**
 * Converts a Supplier entity into a response
 * resource for the REST API.
 */
public final class SupplierResourceFromEntityAssembler {

    private SupplierResourceFromEntityAssembler() {
    }

    public static SupplierResource toResource(
            Supplier supplier) {

        if (supplier == null) {
            throw new IllegalArgumentException(
                    "Supplier is required"
            );
        }

        return new SupplierResource(
                supplier.getId(),
                supplier.getName(),
                supplier.getTaxId(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.isActive()
        );
    }
}
