
package com.nexostock.backend.purchasing.interfaces.rest.transform;

import com.nexostock.backend.purchasing.domain.model.aggregates.Purchase;
import com.nexostock.backend.purchasing.interfaces.rest.resources.PurchaseItemResource;
import com.nexostock.backend.purchasing.interfaces.rest.resources.PurchaseResource;

import java.util.List;

/**
 * Converts a Purchase entity into
 * a REST response resource.
 */
public final class PurchaseResourceFromEntityAssembler {

    private PurchaseResourceFromEntityAssembler() {
    }

    public static PurchaseResource toResource(
            Purchase purchase) {

        if (purchase == null) {
            throw new IllegalArgumentException(
                    "Purchase is required"
            );
        }

        List<PurchaseItemResource> items = purchase
                .getItems()
                .stream()
                .map(item -> new PurchaseItemResource(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        return new PurchaseResource(
                purchase.getId(),
                purchase.getSupplierId(),
                purchase.getStatus().name(),
                items,
                purchase.calculateTotal()
        );
    }
}
