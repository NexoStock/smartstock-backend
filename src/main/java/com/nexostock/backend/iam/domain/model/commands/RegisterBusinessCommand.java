package com.nexostock.backend.iam.domain.model.commands;

import com.nexostock.backend.iam.domain.model.valueobjects.EmailAddress;
import java.util.Objects;

/** Command: register a business and its owner account. */
public record RegisterBusinessCommand(
        String businessName, String businessType, String contactPhone, EmailAddress email, String rawPassword) {

    public RegisterBusinessCommand {
        if (businessName == null || businessName.isBlank()) {
            throw new IllegalArgumentException("businessName is required");
        }
        Objects.requireNonNull(email, "email is required");
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("password must have at least 8 characters");
        }
    }
}
