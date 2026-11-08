package com.nexostock.backend.devices.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SensorTest {

    private final Instant now = Instant.parse("2026-11-08T15:00:00Z");

    private Sensor sensor() {
        return new Sensor(1L, "SN-0001", "hash");
    }

    @Test
    void sensorWithoutProductIsAvailable() {
        assertEquals(Sensor.Status.AVAILABLE, sensor().status(now));
    }

    @Test
    void linkedSensorWithRecentReadingIsOnline() {
        Sensor s = sensor();
        s.linkTo(7L);
        s.registerReading(now.minusSeconds(60), new BigDecimal("12.5"));

        assertEquals(Sensor.Status.ONLINE, s.status(now));
    }

    @Test
    void linkedSensorSilentForMoreThanFiveMinutesIsDisconnected() {
        Sensor s = sensor();
        s.linkTo(7L);
        s.registerReading(now.minusSeconds(301), new BigDecimal("12.5"));

        assertEquals(Sensor.Status.DISCONNECTED, s.status(now));
    }

    @Test
    void linkedSensorWithoutReadingsIsDisconnected() {
        Sensor s = sensor();
        s.linkTo(7L);

        assertEquals(Sensor.Status.DISCONNECTED, s.status(now));
    }

    @Test
    void sensorCannotBeLinkedTwice() {
        Sensor s = sensor();
        s.linkTo(7L);

        assertThrows(IllegalStateException.class, () -> s.linkTo(8L));
    }

    @Test
    void unlinkMakesTheSensorAvailableAgain() {
        Sensor s = sensor();
        s.linkTo(7L);
        s.unlink();

        assertNull(s.getProductId());
        assertEquals(Sensor.Status.AVAILABLE, s.status(now));
    }
}
