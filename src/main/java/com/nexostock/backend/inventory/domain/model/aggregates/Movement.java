package com.nexostock.backend.inventory.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** One audited stock change. */
@Entity
public class Movement extends AuditableAbstractPersistenceEntity {

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private Long productId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private MovementType type;

  /** Positive = enters, negative = leaves. */
  private int quantity;

  private int stockAfter;

  @Column(length = 255)
  private String reference;

  @Column(length = 255)
  private String reason;

  protected Movement() {}

  public Movement(
      Long businessId,
      Long productId,
      MovementType type,
      int quantity,
      int stockAfter,
      String reference,
      String reason) {
    this.businessId = businessId;
    this.productId = productId;
    this.type = type;
    this.quantity = quantity;
    this.stockAfter = stockAfter;
    this.reference = reference;
    this.reason = reason;
  }

  public Long getBusinessId() { return businessId; }
  public Long getProductId() { return productId; }
  public MovementType getType() { return type; }
  public int getQuantity() { return quantity; }
  public int getStockAfter() { return stockAfter; }
  public String getReference() { return reference; }
  public String getReason() { return reason; }
}