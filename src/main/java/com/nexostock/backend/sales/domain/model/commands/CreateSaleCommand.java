package com.nexostock.backend.sales.domain.model.commands;

import java.util.List;
import java.util.Objects;

public record CreateSaleCommand(
        Long businessId, String requestKey, List<Line> lines) {

    public record Line(Long productId, int quantity) {
        public Line {
            Objects.requireNonNull(productId, "productId is required");
            if (quantity < 1) {
                throw new IllegalArgumentException(
                        "quantity must be at least 1");
            }
        }
    }

    public CreateSaleCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "A sale needs at least one line");
        }
    }
}