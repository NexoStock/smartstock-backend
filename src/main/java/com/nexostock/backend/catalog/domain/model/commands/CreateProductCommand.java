
package com.nexostock.backend.catalog.domain.model.commands;

import com.nexostock.backend.catalog.domain.model.valueobjects.Money;
import com.nexostock.backend.catalog.domain.model.valueobjects.ProductName;
import com.nexostock.backend.catalog.domain.model.valueobjects.Sku;
import java.math.BigDecimal;
import java.util.Objects;

public record CreateProductCommand(
        Long businessId,
        ProductName name,
        Sku sku,
        String category,
        BigDecimal unitWeightKg,
        Money salePrice,
        Money purchaseCost,
        String usualSupplier,
        int initialStock,
        int minimumThreshold,
        int maximumCapacity) {

    public CreateProductCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(sku, "sku is required");

        if (unitWeightKg == null || unitWeightKg.signum() <= 0) {
            throw new IllegalArgumentException(
                    "unitWeightKg must be greater than zero"
            );
        }

        if (initialStock < 0) {
            throw new IllegalArgumentException(
                    "initialStock must be zero or positive"
            );
        }

        if (minimumThreshold < 1 || maximumCapacity < 1) {
            throw new IllegalArgumentException(
                    "minimumThreshold and maximumCapacity must be at least 1"
            );
        }
    }
}
