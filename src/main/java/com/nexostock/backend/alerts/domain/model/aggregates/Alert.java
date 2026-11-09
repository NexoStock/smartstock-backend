package com.nexostock.backend.alerts.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;

@Entity
@jakarta.persistence.Table(
        uniqueConstraints = @jakarta.persistence.UniqueConstraint(
                columnNames = {"activeKey"}
        )
)
public class Alert extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertType type;

    private boolean active = true;

    @Column(length = 100)
    private String activeKey;

    private int referenceStock;
    private Instant resolvedAt;

    protected Alert() {}

    public Alert(
            Long businessId,
            Long productId,
            AlertType type,
            int referenceStock) {
        this.businessId = businessId;
        this.productId = productId;
        this.type = type;
        this.referenceStock = referenceStock;
        this.activeKey = businessId + ":" + productId + ":" + type.name();
    }

    public void refresh(int newReferenceStock) {
        this.referenceStock = newReferenceStock;
    }

    public void resolve() {
        this.active = false;
        this.activeKey = null;
        this.resolvedAt = Instant.now();
    }

    public Long getBusinessId() {
        return businessId;
    }

    public Long getProductId() {
        return productId;
    }

    public AlertType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public int getReferenceStock() {
        return referenceStock;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}