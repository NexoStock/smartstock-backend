
package com.nexostock.backend.purchasing.domain.model.commands;

import com.nexostock.backend.purchasing.domain.model.aggregates.PurchaseItem;
import java.util.List;
import java.util.Objects;

/**
 * Command to register a purchase.
 * Comando para registrar una compra.
 */
public record CreatePurchaseCommand(
        Long businessId,
        Long supplierId,
        List<PurchaseItem> items
) {

    public CreatePurchaseCommand {

        Objects.requireNonNull(
                businessId,
                "businessId is required"
        );

        Objects.requireNonNull(
                supplierId,
                "supplierId is required"
        );

        Objects.requireNonNull(
                items,
                "items are required"
        );

        if (businessId <= 0 || supplierId <= 0) {
            throw new IllegalArgumentException(
                    "Business ID and supplier ID must be positive"
            );
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Purchase must contain at least one item"
            );
        }

        items = List.copyOf(items);
    }
}
