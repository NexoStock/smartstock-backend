package com.nexostock.backend.iam.domain.model.aggregates;

import com.nexostock.backend.iam.domain.model.valueobjects.EmailAddress;
import com.nexostock.backend.iam.infrastructure.persistence.jpa.converters.EmailAddressAttributeConverter;
import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;

/** Owner account of a business (Cuenta dueña de un negocio). */
@Entity
public class Account extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long businessId;

    @Convert(converter = EmailAddressAttributeConverter.class)
    @Column(nullable = false, unique = true, length = 254)
    private EmailAddress email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    protected Account() {}

    public Account(Long businessId, EmailAddress email, String passwordHash) {
        this.businessId = businessId;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getBusinessId() { return businessId; }
    public EmailAddress getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
}
