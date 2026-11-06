
package com.nexostock.backend.purchasing.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Purchase extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false)
    private Long supplierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseStatus status;

    @ElementCollection
    @CollectionTable(
            name = "purchase_items",
            joinColumns = @JoinColumn(name = "purchase_id")
    )
    @OrderColumn(name = "line_number")
    private List<PurchaseItem> items = new ArrayList<>();

    protected Purchase() {
    }

    public Purchase(
            Long businessId,
            Long supplierId,
            List<PurchaseItem> items) {

        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException(
                    "Business ID must be positive"
            );
        }

        if (supplierId == null || supplierId <= 0) {
            throw new IllegalArgumentException(
                    "Supplier ID must be positive"
            );
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Purchase must contain at least one item"
            );
        }

        if (items.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException(
                    "Purchase cannot contain null items"
            );
        }

        this.businessId = businessId;
        this.supplierId = supplierId;
        this.items = new ArrayList<>(items);
        this.status = PurchaseStatus.PENDING;
    }

    public boolean receive() {

        if (status == PurchaseStatus.RECEIVED) {
            return false;
        }

        if (status == PurchaseStatus.CANCELLED) {
            throw new IllegalStateException(
                    "CANCELLED_PURCHASE_CANNOT_BE_RECEIVED"
            );
        }

        status = PurchaseStatus.RECEIVED;
        return true;
    }

    public void cancel() {

        if (status == PurchaseStatus.RECEIVED) {
            throw new IllegalStateException(
                    "RECEIVED_PURCHASE_CANNOT_BE_CANCELLED"
            );
        }

        if (status == PurchaseStatus.CANCELLED) {
            throw new IllegalStateException(
                    "PURCHASE_ALREADY_CANCELLED"
            );
        }

        status = PurchaseStatus.CANCELLED;
    }

    public BigDecimal calculateTotal() {

        return items.stream()
                .map(PurchaseItem::subtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    public Long getBusinessId() {
        return businessId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public List<PurchaseItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
