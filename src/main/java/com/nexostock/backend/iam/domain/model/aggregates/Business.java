package com.nexostock.backend.iam.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/** A business (tenant): all its data is isolated by businessId (Negocio = inquilino). */
@Entity
public class Business extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String businessType;

    /** Optional WhatsApp number for alerts, e.g. +51999999999 (Número opcional para alertas). */
    @Column(length = 32)
    private String contactPhone;

    protected Business() {}

    public Business(String name, String businessType, String contactPhone) {
        this.name = name.trim();
        this.businessType = businessType == null ? "" : businessType.trim();
        this.contactPhone = contactPhone == null || contactPhone.isBlank() ? null : contactPhone.trim();
    }

    public String getName() { return name; }
    public String getBusinessType() { return businessType; }
    public String getContactPhone() { return contactPhone; }
}
