package com.nexostock.backend.devices.domain.model.commands;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ReceiveReadingCommandTest {

    @Test
    void negativeWeightIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReceiveReadingCommand("SN-1", "key", new BigDecimal("-1"), null));
    }

    @Test
    void weightAboveTheLimitIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReceiveReadingCommand("SN-1", "key", new BigDecimal("100000.001"), null));
    }

    @Test
    void readingFromTheFutureIsRejected() {
        Instant future = Instant.now().plusSeconds(600);

        assertThrows(IllegalArgumentException.class, () ->
                new ReceiveReadingCommand("SN-1", "key", BigDecimal.ONE, future));
    }

    @Test
    void missingDateUsesTheServerTime() {
        var command = new ReceiveReadingCommand("SN-1", "key", BigDecimal.ONE, null);

        assertNotNull(command.measuredAt());
    }
}
