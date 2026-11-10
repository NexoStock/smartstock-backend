
package com.nexostock.backend.purchasing.domain.model.aggregates;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseItemTest {

    @Test
    void shouldCreatePurchaseItem() {
        PurchaseItem item = new PurchaseItem(
                1L,
                5,
                new BigDecimal("10.50")
        );

        assertEquals(1L, item.getProductId());
        assertEquals(5, item.getQuantity());
        assertEquals(new BigDecimal("10.50"), item.getUnitPrice());
    }

    @Test
    void shouldCalculateSubtotal() {
        PurchaseItem item = new PurchaseItem(
                1L,
                3,
                new BigDecimal("12.50")
        );

        assertEquals(
                new BigDecimal("37.50"),
                item.subtotal()
        );
    }

    @Test
    void shouldRejectInvalidProductId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PurchaseItem(
                        0L, 3, new BigDecimal("10.00")
                )
        );
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PurchaseItem(
                        1L, 0, new BigDecimal("10.00")
                )
        );
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PurchaseItem(
                        1L, 2, new BigDecimal("-10.00")
                )
        );
    }

    @Test
    void shouldRoundUnitPriceToTwoDecimals() {
        PurchaseItem item = new PurchaseItem(
                1L,
                2,
                new BigDecimal("10.456")
        );

        assertEquals(
                new BigDecimal("10.46"),
                item.getUnitPrice()
        );

        assertEquals(
                new BigDecimal("20.92"),
                item.subtotal()
        );
    }
}
