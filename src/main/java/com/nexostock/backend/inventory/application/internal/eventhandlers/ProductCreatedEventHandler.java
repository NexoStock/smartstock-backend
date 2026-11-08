package com.nexostock.backend.inventory.application.internal.eventhandlers;

import com.nexostock.backend.catalog.domain.model.events.ProductCreatedEvent;
import com.nexostock.backend.inventory.application.commandservices.MovementCommandService;
import com.nexostock.backend.inventory.domain.model.aggregates.MovementType;
import com.nexostock.backend.inventory.domain.model.commands.RegisterMovementCommand;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Records the initial stock of a new product as the first movement. */
@Component
public class ProductCreatedEventHandler {

  private final MovementCommandService movements;

  public ProductCreatedEventHandler(MovementCommandService movements) {
    this.movements = movements;
  }

  @EventListener
  public void on(ProductCreatedEvent event) {
    if (event.initialStock() > 0) {
      var result =
          movements.handle(
          new RegisterMovementCommand(
              event.businessId(), event.productId(), MovementType.ADJUSTMENT,
              event.initialStock(), "INITIAL-" + event.productId(), "Initial stock"));
      if (result.isFailure()) {
        // Throwing makes the product creation roll back: no product without its initial stock
        throw new IllegalStateException(
            "INITIAL_STOCK_FAILED: " + result.getError().map(e -> e.message()).orElse("unknown"));
      }
    }
  }
}