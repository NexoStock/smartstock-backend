package com.nexostock.backend.shared.interfaces.rest.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Temporary business provider for local development.
 * Replace with JWT-based provider before deployment.
 */
@Component
@Profile("local")
public class DemoCurrentBusinessProvider
        implements CurrentBusinessProvider {

    @Override
    public Long businessId() {
        return 1L;
    }

    @Override
    public Long accountId() {
        return 1L;
    }
}