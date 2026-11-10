package com.nexostock.backend.devices.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * One weight reading in kilograms. The pair (sensor, measuredAt)
 * is unique, so a retry of the device never duplicates it.
 * Lectura de peso; un reintento no se duplica.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        columnNames = {"sensorId", "measuredAt"}))
public class SensorReading extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long sensorId;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal weightKg;

    @Column(nullable = false)
    private Instant measuredAt;

    protected SensorReading() {
    }

    public SensorReading(
            Long sensorId,
            BigDecimal weightKg,
            Instant measuredAt) {

        this.sensorId = sensorId;
        this.weightKg = weightKg;
        this.measuredAt = measuredAt;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }
}
