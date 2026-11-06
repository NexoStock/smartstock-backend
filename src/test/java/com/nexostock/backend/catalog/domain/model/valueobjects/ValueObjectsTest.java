
package com.nexostock.backend.catalog.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ValueObjectsTest {

    @Test
    void productNameRejectsBlank() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProductName("  ")
        );
    }

    @Test
    void skuIsUpperCased() {

        assertEquals(
                "ARR-001",
                new Sku("arr-001").value()
        );
    }

    @Test
    void moneyKeepsTwoDecimalsAndRejectsNegative() {

        assertEquals(
                new BigDecimal("3.46"),
                new Money(
                        new BigDecimal("3.456")
                ).value()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Money(
                        new BigDecimal("-1")
                )
        );
    }
}
