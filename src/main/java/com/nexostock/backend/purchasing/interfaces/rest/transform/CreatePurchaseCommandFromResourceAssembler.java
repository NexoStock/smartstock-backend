
package com.nexostock.backend.purchasing.interfaces.rest.transform;

import com.nexostock.backend.purchasing.domain.model.aggregates.PurchaseItem;
import com.nexostock.backend.purchasing.domain.model.commands.CreatePurchaseCommand;
import com.nexostock.backend.purchasing.interfaces.rest.resources.CreatePurchaseResource;

import java.util.List;

/**
 * Converts a purchase HTTP request into
 * a CreatePurchaseCommand.
 */
public final class CreatePurchaseCommandFromResourceAssembler {

    private CreatePurchaseCommandFromResourceAssembler() {
    }

    public static CreatePurchaseCommand toCommand(
            Long businessId,
            CreatePurchaseResource resource) {

        if (resource == null) {
            throw new IllegalArgumentException(
                    "Purchase resource is required"
            );
        }

        if (resource.items() == null ||
                resource.items().isEmpty()) {

            throw new IllegalArgumentException(
                    "Purchase must contain at least one item"
            );
        }

        List<PurchaseItem> items = resource.items()
                .stream()
                .map(item -> {

                    if (item == null) {
                        throw new IllegalArgumentException(
                                "Purchase item cannot be null"
                        );
                    }

                    if (item.quantity() == null) {
                        throw new IllegalArgumentException(
                                "Item quantity is required"
                        );
                    }

                    return new PurchaseItem(
                            item.productId(),
                            item.quantity(),
                            item.unitPrice()
                    );
                })
                .toList();

        return new CreatePurchaseCommand(
                businessId,
                resource.supplierId(),
                items
        );
    }
}
