
package com.nexostock.backend.purchasing.domain.model.commands;

import java.util.Objects;

/**
 * Command to receive a purchase.
 * Comando para recibir una compra.
 */
public record ReceivePurchaseCommand(
        Long businessId,
        Long purchaseId
) {

    public ReceivePurchaseCommand {

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
