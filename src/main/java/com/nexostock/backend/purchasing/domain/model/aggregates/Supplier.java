
package com.nexostock.backend.purchasing.domain.model.aggregates;

import com.nexostock.backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"businessId", "taxId"}
                )
        }
)
public class Supplier extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 20)
    private String taxId;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private boolean active = true;

    protected Supplier() {
    }

    public Supplier(
            Long businessId,
            String name,
            String taxId,
            String email,
            String phone,
            String address) {

        validateBusinessId(businessId);

        this.businessId = businessId;
        this.name = validateName(name);
        this.taxId = validateTaxId(taxId);
        this.email = normalize(email);
        this.phone = normalize(phone);
        this.address = normalize(address);
        this.active = true;
    }

    public void update(
            String name,
            String taxId,
            String email,
            String phone,
            String address) {

        String validatedName = validateName(name);
        String validatedTaxId = validateTaxId(taxId);

        this.name = validatedName;
        this.taxId = validatedTaxId;
        this.email = normalize(email);
        this.phone = normalize(phone);
        this.address = normalize(address);
    }

    public void deactivate() {
        this.active = false;
    }

    private static void validateBusinessId(Long businessId) {
        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException(
                    "Business ID must be positive"
            );
        }
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Supplier name is required"
            );
        }

        String value = name.trim();

        if (value.length() > 255) {
            throw new IllegalArgumentException(
                    "Supplier name is too long"
            );
        }

        return value;
    }

    private static String validateTaxId(String taxId) {
        if (taxId == null || taxId.isBlank()) {
            throw new IllegalArgumentException(
                    "Supplier tax ID is required"
            );
        }

        String value = taxId.trim();

        if (value.length() > 20) {
            throw new IllegalArgumentException(
                    "Supplier tax ID is too long"
            );
        }

        return value;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public Long getBusinessId() {
        return businessId;
    }

    public String getName() {
        return name;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
    }
}
