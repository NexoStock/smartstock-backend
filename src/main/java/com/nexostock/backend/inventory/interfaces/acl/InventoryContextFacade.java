package com.nexostock.backend.inventory.interfaces.acl;

import com.nexostock.backend.inventory.application.commandservices.MovementCommandService;
import com.nexostock.backend.inventory.domain.model.aggregates.MovementType;
import com.nexostock.backend.inventory.domain.model.commands.RegisterMovementCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import org.springframework.stereotype.Service;

/**
 * Door of the Inventory context for Sales and Purchasing.
 * Other contexts never call MovementCommandService directly.
 */
@Service
public class InventoryContextFacade {

  private final MovementCommandService movements;

  public InventoryContextFacade(MovementCommandService movements) {
    this.movements = movements;
  }

  /** Removes stock because of a sale; fails with INSUFFICIENT_STOCK if there is not enough. */
  public Result<Long, ApplicationError> removeStockForSale(
      Long businessId, Long productId, int quantity, String reference) {
    return movements
        .handle(
            new RegisterMovementCommand(
                businessId, productId, MovementType.SALE, -quantity, reference, "Sale " + reference))
        .map(m -> m.getId());
  }

  /** Adds stock because a purchase was received (MovementType.PURCHASE_RECEIPT). */
  public Result<Long, ApplicationError> addStockForPurchase(
      Long businessId, Long productId, int quantity, String reference) {
    return movements
        .handle(
            new RegisterMovementCommand(
                businessId, productId, MovementType.PURCHASE_RECEIPT, quantity, reference,
                "Purchase " + reference))
        .map(m -> m.getId());
  }
}