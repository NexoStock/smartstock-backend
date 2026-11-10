package com.nexostock.backend.alerts.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        columnNames = {"businessId", "channel"}))
public class NotificationChannel
        extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false, length = 16)
    private String channel;

    @Column(nullable = false, length = 255)
    private String destination;

    @Column(nullable = false)
    private boolean active;

    protected NotificationChannel() {}

    public NotificationChannel(
            Long businessId,
            String channel,
            String destination,
            boolean active) {
        this.businessId = businessId;
        this.channel = channel;
        this.destination = destination;
        this.active = active;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public String getChannel() {
        return channel;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isActive() {
        return active;
    }
}