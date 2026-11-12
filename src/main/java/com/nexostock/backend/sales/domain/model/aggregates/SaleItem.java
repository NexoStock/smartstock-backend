package com.nexostock.backend.sales.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class SaleItem extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long saleId;

    @Column(nullable = false)
    private Long productId;

    private int quantity;

    @Column(precision = 16, scale = 2)
    private BigDecimal unitPrice;

    protected SaleItem() {}

    public SaleItem(
            Long saleId, Long productId,
            int quantity, BigDecimal unitPrice) {
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getSaleId() { return saleId; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}