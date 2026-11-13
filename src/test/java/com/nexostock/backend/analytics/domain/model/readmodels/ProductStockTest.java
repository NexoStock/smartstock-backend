package com.nexostock.backend.analytics.domain.model.readmodels;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductStockTest {

    private static final Instant NOW = Instant.parse("2026-11-13T15:00:00Z");

    private static ProductStock product(int registered, String weightKg, Instant lastReading) {
        return new ProductStock(1L, "Arroz 5 kg", registered, 10, new BigDecimal("5.0"),
                7L, "SN-0001", weightKg == null ? null : new BigDecimal(weightKg), lastReading);
    }

    @Test
    void weightIsConvertedToWholeUnits() {
        assertEquals(30, ProductStock.toUnits(new BigDecimal("150"), new BigDecimal("5.0")));
        assertEquals(3, ProductStock.toUnits(new BigDecimal("12.5"), new BigDecimal("5.0")));
    }

    @Test
    void sensorWithoutRecentReadingsIsDisconnected() {
        var p = product(40, "150", NOW.minusSeconds(301));
        assertEquals("disconnected", p.sensorStatus(NOW));
        assertNull(p.sensorUnits(NOW));
        assertEquals(40, p.referenceUnits(NOW));
    }

    @Test
    void productWithoutSensorUsesRegisteredStock() {
        var p = new ProductStock(1L, "Arroz 5 kg", 8, 10, new BigDecimal("5.0"), null, null, null, null);
        assertEquals("none", p.sensorStatus(NOW));
        assertEquals("lowStock", p.level(NOW));
        assertEquals(ComparisonRow.REGISTERED_ONLY, p.compare(NOW).result());
    }

    @Test
    void differenceAboveTenPercentIsDiscrepancy() {
        var row = product(38, "150", NOW.minusSeconds(60)).compare(NOW);
        assertEquals(30, row.physicalUnits());
        assertEquals(new BigDecimal("-21.1"), row.differencePercent());
        assertEquals(ComparisonRow.DISCREPANCY, row.result());
    }

    @Test
    void differenceUpToTenPercentIsMatch() {
        var row = product(40, "190", NOW.minusSeconds(60)).compare(NOW);
        assertEquals(38, row.physicalUnits());
        assertEquals(ComparisonRow.MATCH, row.result());
    }
}
