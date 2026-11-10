package com.nexostock.backend.devices.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Weight sensor of a business, linked or not to a product.
 * Sensor de peso del negocio, vinculado o no a un producto.
 */
@Entity
public class Sensor extends AuditableAbstractPersistenceEntity {

    /**
     * Seconds without readings before a linked sensor
     * is shown as disconnected (rule R11).
     */
    public static final long ONLINE_WINDOW_SECONDS = 300;

    /** States of the Angular contract: online, disconnected, available. */
    public enum Status { ONLINE, DISCONNECTED, AVAILABLE }

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false, unique = true, length = 60)
    private String deviceCode;

    /** SHA-256 of the device key; the key itself is shown only once. */
    @Column(nullable = false, length = 64)
    private String deviceKeyHash;

    /** Null while the sensor is available (rule R9). */
    private Long productId;

    private Instant lastReadingAt;

    @Column(precision = 12, scale = 3)
    private BigDecimal lastWeightKg;

    protected Sensor() {
    }

    public Sensor(
            Long businessId,
            String deviceCode,
            String deviceKeyHash) {

        this.businessId = businessId;
        this.deviceCode = deviceCode;
        this.deviceKeyHash = deviceKeyHash;
    }

    /**
     * Keeps the last weight. Readings older than the last one
     * are rejected before by the command service.
     */
    public void registerReading(Instant at, BigDecimal weightKg) {
        this.lastReadingAt = at;
        this.lastWeightKg = weightKg;
    }

    public void linkTo(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (isLinked()) {
            throw new IllegalStateException("SENSOR_ALREADY_LINKED");
        }
        this.productId = productId;
    }

    public void unlink() {
        if (!isLinked()) {
            throw new IllegalStateException("SENSOR_NOT_LINKED");
        }
        this.productId = null;
    }

    public void replaceKeyHash(String newKeyHash) {
        this.deviceKeyHash = newKeyHash;
    }

    public boolean isLinked() {
        return productId != null;
    }

    /**
     * AVAILABLE = not linked; ONLINE = linked and a reading
     * in the last 5 minutes (R11); DISCONNECTED = linked but silent.
     */
    public Status status(Instant now) {
        if (!isLinked()) {
            return Status.AVAILABLE;
        }
        boolean recent = lastReadingAt != null
                && lastReadingAt.isAfter(
                        now.minusSeconds(ONLINE_WINDOW_SECONDS));

        return recent ? Status.ONLINE : Status.DISCONNECTED;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getDeviceKeyHash() {
        return deviceKeyHash;
    }

    public Long getProductId() {
        return productId;
    }

    public Instant getLastReadingAt() {
        return lastReadingAt;
    }

    public BigDecimal getLastWeightKg() {
        return lastWeightKg;
    }
}
