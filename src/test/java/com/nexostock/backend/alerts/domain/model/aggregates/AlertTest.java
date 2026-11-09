package com.nexostock.backend.alerts.domain.model.aggregates;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertTest {

    @Test
    void newAlertStartsActive() {
        Alert alert = new Alert(1L, 10L, AlertType.LOW_STOCK, 3);

        assertEquals(Long.valueOf(1L), alert.getBusinessId());
        assertEquals(Long.valueOf(10L), alert.getProductId());
        assertEquals(AlertType.LOW_STOCK, alert.getType());
        assertEquals(3, alert.getReferenceStock());
        assertTrue(alert.isActive());
        assertNull(alert.getResolvedAt());
    }

    @Test
    void refreshUpdatesStockWithoutClosingAlert() {
        Alert alert = new Alert(1L, 10L, AlertType.LOW_STOCK, 3);

        alert.refresh(2);

        assertEquals(2, alert.getReferenceStock());
        assertTrue(alert.isActive());
        assertNull(alert.getResolvedAt());
    }

    @Test
    void resolveClosesAlertAndRecordsDate() {
        Alert alert = new Alert(1L, 10L, AlertType.LOW_STOCK, 3);

        alert.resolve();

        assertFalse(alert.isActive());
        assertNotNull(alert.getResolvedAt());
    }
}