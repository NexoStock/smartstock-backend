
package com.nexostock.backend.catalog.domain.model.valueobjects;

public record ProductName(String value) {

    public ProductName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name must not be blank"
            );
        }

        value = value.trim();

        if (value.length() > 255) {
            throw new IllegalArgumentException(
                    "Product name must have at most 255 characters"
            );
        }
    }
}
