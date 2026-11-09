package com.nexostock.backend.alerts.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.Instant;

@Entity
public class OutboundMessage extends AuditableAbstractPersistenceEntity {

    public static final int MAX_ATTEMPTS = 3;

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false, length = 16)
    private String channel;

    @Column(nullable = false, length = 255)
    private String destination;

    @Column(length = 255)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, length = 16)
    private String status = "PENDING";

    private int attempts;
    private Instant nextAttemptAt = Instant.now();

    protected OutboundMessage() {}

    public OutboundMessage(
            Long businessId,
            String channel,
            String destination,
            String subject,
            String content) {
        this.businessId = businessId;
        this.channel = channel;
        this.destination = destination;
        this.subject = subject;
        this.content = content;
    }

    public void markSent() {
        attempts++;
        status = "SENT";
    }

    public void markFailedAttempt() {
        attempts++;

        if (attempts >= MAX_ATTEMPTS) {
            status = "FAILED";
        } else {
            status = "PENDING";
            nextAttemptAt = Instant.now().plusSeconds(60L * attempts);
        }
    }

    public void requeue() {
        status = "PENDING";
        attempts = 0;
        nextAttemptAt = Instant.now();
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

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }
}