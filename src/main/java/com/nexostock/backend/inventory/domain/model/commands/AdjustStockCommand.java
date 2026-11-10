package com.nexostock.backend.inventory.domain.model.commands;

import java.util.Objects;

/** Command: set the accounting stock to a value, with a reason. */
public record AdjustStockCommand(Long businessId, Long productId, int newStock, String reason) {

  public AdjustStockCommand {
    Objects.requireNonNull(businessId, "businessId is required");
    Objects.requireNonNull(productId, "productId is required");
    if (newStock < 0) {
      throw new IllegalArgumentException("newStock must be zero or positive");
    }
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("reason is required");
    }
  }
}