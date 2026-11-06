
package com.nexostock.backend.catalog.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal value) {

    public Money {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(
                    "Money must be zero or positive"
            );
        }

        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }
}
