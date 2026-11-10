package com.nexostock.backend.inventory.domain.model.commands;

import com.nexostock.backend.inventory.domain.model.aggregates.MovementType;
import java.util.Objects;

/**
 * Command: register a stock movement. It is the ONLY way to change stock.
 */
public record RegisterMovementCommand(
    Long businessId, Long productId, MovementType type, int quantity, String reference, String reason) {

  public RegisterMovementCommand {
    Objects.requireNonNull(businessId, "businessId is required");
    Objects.requireNonNull(productId, "productId is required");
    Objects.requireNonNull(type, "type is required");
    if (quantity == 0) {
      throw new IllegalArgumentException("quantity must not be zero");
    }
  }
}