package com.nexostock.backend.iam.domain.model.valueobjects;

import java.util.Locale;

/** Email address, stored in lower case (Correo guardado en minúsculas). */
public record EmailAddress(String value) {
    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 254 || !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email is not valid");
        }
    }
}
