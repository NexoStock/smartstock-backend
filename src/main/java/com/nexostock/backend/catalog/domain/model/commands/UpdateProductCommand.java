
package com.nexostock.backend.catalog.domain.model.commands;

import com.nexostock.backend.catalog.domain.model.valueobjects.Money;
import com.nexostock.backend.catalog.domain.model.valueobjects.ProductName;
import com.nexostock.backend.catalog.domain.model.valueobjects.Sku;
import java.math.BigDecimal;
import java.util.Objects;

public record UpdateProductCommand(
        Long businessId,
        Long productId,
        ProductName name,
        Sku sku,
        String category,
        BigDecimal unitWeightKg,
        Money salePrice,
        Money purchaseCost,
        String usualSupplier,
        int minimumThreshold,
        int maximumCapacity) {

    public UpdateProductCommand {
        Objects.requireNonNull(
                businessId, "businessId is required"
        );

        Objects.requireNonNull(
                productId, "productId is required"
        );

        Objects.requireNonNull(
                name, "name is required"
        );

        if (unitWeightKg == null || unitWeightKg.signum() <= 0) {
            throw new IllegalArgumentException(
                    "unitWeightKg must be greater than zero"
            );
        }

        if (minimumThreshold < 1 || maximumCapacity < 1) {
            throw new IllegalArgumentException(
                    "minimumThreshold and maximumCapacity must be at least 1"
            );
        }
    }
}
