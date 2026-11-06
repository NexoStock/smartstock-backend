
package com.nexostock.backend.catalog.domain.model.valueobjects;

import java.util.UUID;

public record Sku(String value) {

    public Sku {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "SKU must not be blank"
            );
        }

        value = value.trim().toUpperCase();

        if (!value.matches("[A-Z0-9._-]{1,64}")) {
            throw new IllegalArgumentException(
                    "SKU only allows letters, digits, '.', '_' and '-'"
            );
        }
    }

    public static Sku generate() {
        return new Sku(
                "SKU-" + UUID.randomUUID().toString().substring(0, 8)
        );
    }
}
