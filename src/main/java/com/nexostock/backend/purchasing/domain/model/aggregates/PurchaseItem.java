
package com.nexostock.backend.purchasing.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public class PurchaseItem {

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal unitPrice;

    protected PurchaseItem() {
    }

    public PurchaseItem(
            Long productId,
            int quantity,
            BigDecimal unitPrice) {

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be positive"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Unit price must be greater than zero"
            );
        }

        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(
                BigDecimal.valueOf(quantity)
        );
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
