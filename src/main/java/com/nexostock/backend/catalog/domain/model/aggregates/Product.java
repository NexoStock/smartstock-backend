
package com.nexostock.backend.catalog.domain.model.aggregates;

import com.nexostock.backend.catalog.domain.model.commands.CreateProductCommand;
import com.nexostock.backend.catalog.domain.model.commands.UpdateProductCommand;
import com.nexostock.backend.catalog.domain.model.valueobjects.Money;
import com.nexostock.backend.catalog.domain.model.valueobjects.ProductName;
import com.nexostock.backend.catalog.domain.model.valueobjects.Sku;
import com.nexostock.backend.catalog.infrastructure.persistence.jpa.converters.MoneyAttributeConverter;
import com.nexostock.backend.catalog.infrastructure.persistence.jpa.converters.ProductNameAttributeConverter;
import com.nexostock.backend.catalog.infrastructure.persistence.jpa.converters.SkuAttributeConverter;
import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"businessId", "sku"}))
public class Product extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long businessId;

    @Convert(converter = ProductNameAttributeConverter.class)
    @Column(nullable = false, length = 255)
    private ProductName name;

    @Convert(converter = SkuAttributeConverter.class)
    @Column(nullable = false, length = 64)
    private Sku sku;

    @Column(length = 255)
    private String category;

    @Column(precision = 16, scale = 6)
    private BigDecimal unitWeightKg;

    @Convert(converter = MoneyAttributeConverter.class)
    @Column(precision = 16, scale = 2)
    private Money salePrice;

    @Convert(converter = MoneyAttributeConverter.class)
    @Column(precision = 16, scale = 2)
    private Money purchaseCost;

    @Column(length = 255)
    private String usualSupplier;

    private int registeredStock;
    private int minimumThreshold;
    private int maximumCapacity;
    private boolean active = true;

    protected Product() {
    }

    public Product(CreateProductCommand command) {
        this.businessId = command.businessId();
        this.name = command.name();
        this.sku = command.sku();
        this.category = command.category();
        this.unitWeightKg = command.unitWeightKg();
        this.salePrice = command.salePrice();
        this.purchaseCost = command.purchaseCost();
        this.usualSupplier = command.usualSupplier();
        this.registeredStock = 0;
        this.minimumThreshold = command.minimumThreshold();
        this.maximumCapacity = command.maximumCapacity();
        assertThresholdWithinCapacity();
    }

    public void update(UpdateProductCommand command) {
        assertThresholdWithinCapacity(
                command.minimumThreshold(),
                command.maximumCapacity()
        );

        this.name = command.name();

        if (command.sku() != null) {
            this.sku = command.sku();
        }

        this.category = command.category();
        this.unitWeightKg = command.unitWeightKg();
        this.salePrice = command.salePrice();
        this.purchaseCost = command.purchaseCost();
        this.usualSupplier = command.usualSupplier();
        this.minimumThreshold = command.minimumThreshold();
        this.maximumCapacity = command.maximumCapacity();
    }

    public void changeThreshold(int newThreshold) {
        assertThresholdWithinCapacity(newThreshold, maximumCapacity);
        this.minimumThreshold = newThreshold;
    }

    public void applyStock(int newStock) {
        if (newStock < 0) {
            throw new IllegalStateException("INSUFFICIENT_STOCK");
        }
        this.registeredStock = newStock;
    }

    public void retire() {
        if (registeredStock != 0) {
            throw new IllegalStateException("PRODUCT_HAS_STOCK");
        }
        this.active = false;
    }

    private void assertThresholdWithinCapacity() {
        assertThresholdWithinCapacity(minimumThreshold, maximumCapacity);
    }

    private static void assertThresholdWithinCapacity(
            int minimumThreshold,
            int maximumCapacity) {

        if (minimumThreshold > maximumCapacity) {
            throw new IllegalStateException(
                    "THRESHOLD_EXCEEDS_CAPACITY"
            );
        }
    }

    public Long getBusinessId() {
        return businessId;
    }

    public ProductName getName() {
        return name;
    }

    public Sku getSku() {
        return sku;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getUnitWeightKg() {
        return unitWeightKg;
    }

    public Money getSalePrice() {
        return salePrice;
    }

    public Money getPurchaseCost() {
        return purchaseCost;
    }

    public String getUsualSupplier() {
        return usualSupplier;
    }

    public int getRegisteredStock() {
        return registeredStock;
    }

    public int getMinimumThreshold() {
        return minimumThreshold;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public boolean isActive() {
        return active;
    }
}
