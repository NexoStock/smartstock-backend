package com.nexostock.backend.iam.domain.model.commands;

import com.nexostock.backend.iam.domain.model.valueobjects.EmailAddress;
import java.util.Objects;

/** Command: sign in with email and password. */
public record SignInCommand(EmailAddress email, String rawPassword) {
    public SignInCommand {
        Objects.requireNonNull(email, "email is required");
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
    }
}
