
package com.nexostock.backend.purchasing.domain.model.commands;

import java.util.Objects;

/**
 * Command to cancel a purchase.
 * Comando para cancelar una compra.
 */
public record CancelPurchaseCommand(
        Long businessId,
        Long purchaseId
) {

    public CancelPurchaseCommand {

        Objects.requireNonNull(
                businessId,
                "businessId is required"
        );

        Objects.requireNonNull(
                purchaseId,
                "purchaseId is required"
        );

        if (businessId <= 0 || purchaseId <= 0) {
            throw new IllegalArgumentException(
                    "Business ID and purchase ID must be positive"
            );
        }
    }
}
