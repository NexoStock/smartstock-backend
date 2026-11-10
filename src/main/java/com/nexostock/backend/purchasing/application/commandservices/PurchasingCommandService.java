
package com.nexostock.backend.purchasing.application.commandservices;

import com.nexostock.backend.purchasing.domain.model.aggregates.Purchase;
import com.nexostock.backend.purchasing.domain.model.aggregates.Supplier;
import com.nexostock.backend.purchasing.domain.model.commands.CancelPurchaseCommand;
import com.nexostock.backend.purchasing.domain.model.commands.CreatePurchaseCommand;
import com.nexostock.backend.purchasing.domain.model.commands.CreateSupplierCommand;
import com.nexostock.backend.purchasing.domain.model.commands.DeactivateSupplierCommand;
import com.nexostock.backend.purchasing.domain.model.commands.ReceivePurchaseCommand;
import com.nexostock.backend.purchasing.domain.model.commands.UpdateSupplierCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

/**
 * Application command service for the Purchasing context.
 *
 * Servicio de comandos para gestionar proveedores y compras.
 *
 * Each domain command has a corresponding handle method.
 */
public interface PurchasingCommandService {

    /**
     * Registers a new supplier for a business.
     */
    Result<Supplier, ApplicationError> handle(
            CreateSupplierCommand command
    );

    /**
     * Updates an existing supplier.
     */
    Result<Supplier, ApplicationError> handle(
            UpdateSupplierCommand command
    );

    /**
     * Deactivates an existing supplier.
     */
    Result<Supplier, ApplicationError> handle(
            DeactivateSupplierCommand command
    );

    /**
     * Registers a purchase with one or more items.
     */
    Result<Purchase, ApplicationError> handle(
            CreatePurchaseCommand command
    );

    /**
     * Receives a purchase and records stock entries
     * through InventoryContextFacade.
     *
     * Repeated receipts must not duplicate stock.
     */
    Result<Purchase, ApplicationError> handle(
            ReceivePurchaseCommand command
    );

    /**
     * Cancels a purchase that has not been received.
     */
    Result<Purchase, ApplicationError> handle(
            CancelPurchaseCommand command
    );
}
