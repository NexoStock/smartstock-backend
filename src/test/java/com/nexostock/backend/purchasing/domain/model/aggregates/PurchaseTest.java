
package com.nexostock.backend.purchasing.domain.model.aggregates;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTest {

    private PurchaseItem item() {
        return new PurchaseItem(
                1L, 3, new BigDecimal("10.50")
        );
    }

    private Purchase createPurchase() {
        return new Purchase(
                1L, 2L, List.of(item())
        );
    }

    @Test
    void shouldCreatePendingPurchase() {
        Purchase purchase = createPurchase();

        assertEquals(1L, purchase.getBusinessId());
        assertEquals(2L, purchase.getSupplierId());
        assertEquals(PurchaseStatus.PENDING, purchase.getStatus());
        assertEquals(1, purchase.getItems().size());
    }

    @Test
    void shouldCalculateTotal() {
        Purchase purchase = new Purchase(
                1L,
                2L,
                List.of(
                        new PurchaseItem(
                                1L, 3, new BigDecimal("10.50")
                        ),
                        new PurchaseItem(
                                2L, 2, new BigDecimal("4.75")
                        )
                )
        );

        assertEquals(
                new BigDecimal("41.00"),
                purchase.calculateTotal()
        );
    }

    @Test
    void shouldRejectInvalidBusinessId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Purchase(0L, 2L, List.of(item()))
        );
    }

    @Test
    void shouldRejectInvalidSupplierId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Purchase(1L, 0L, List.of(item()))
        );
    }

    @Test
    void shouldRejectEmptyItems() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Purchase(1L, 2L, List.of())
        );
    }

    @Test
    void shouldRejectNullItem() {
        List<PurchaseItem> items = new ArrayList<>();
        items.add(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Purchase(1L, 2L, items)
        );
    }

    @Test
    void shouldReceivePurchaseOnlyOnce() {
        Purchase purchase = createPurchase();

        assertTrue(purchase.receive());
        assertEquals(PurchaseStatus.RECEIVED, purchase.getStatus());

        assertFalse(purchase.receive());
        assertEquals(PurchaseStatus.RECEIVED, purchase.getStatus());
    }

    @Test
    void shouldCancelPendingPurchase() {
        Purchase purchase = createPurchase();

        purchase.cancel();

        assertEquals(PurchaseStatus.CANCELLED, purchase.getStatus());
    }

    @Test
    void shouldNotReceiveCancelledPurchase() {
        Purchase purchase = createPurchase();
        purchase.cancel();

        assertThrows(
                IllegalStateException.class,
                purchase::receive
        );

        assertEquals(PurchaseStatus.CANCELLED, purchase.getStatus());
    }

    @Test
    void shouldNotCancelReceivedPurchase() {
        Purchase purchase = createPurchase();
        purchase.receive();

        assertThrows(
                IllegalStateException.class,
                purchase::cancel
        );

        assertEquals(PurchaseStatus.RECEIVED, purchase.getStatus());
    }

    @Test
    void shouldNotCancelTwice() {
        Purchase purchase = createPurchase();
        purchase.cancel();

        assertThrows(
                IllegalStateException.class,
                purchase::cancel
        );
    }

    @Test
    void shouldProtectPurchaseItems() {
        Purchase purchase = createPurchase();

        assertThrows(
                UnsupportedOperationException.class,
                () -> purchase.getItems().clear()
        );
    }
}
