package com.nexostock.backend.sales.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        columnNames = {"businessId", "requestKey"}))
public class Sale extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(length = 64)
    private String requestKey;

    @Column(precision = 16, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    protected Sale() {}

    public Sale(Long businessId, String code, String requestKey) {
        this.businessId = businessId;
        this.code = code;
        this.requestKey = requestKey;
    }

    public void addToTotal(BigDecimal amount) {
        total = total.add(amount);
    }

    public Long getBusinessId() { return businessId; }
    public String getCode() { return code; }
    public BigDecimal getTotal() { return total; }
}