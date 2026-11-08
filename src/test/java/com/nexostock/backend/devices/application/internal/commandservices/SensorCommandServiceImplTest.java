package com.nexostock.backend.devices.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexostock.backend.devices.application.internal.outboundservices.ExternalCatalogService;
import com.nexostock.backend.devices.domain.model.aggregates.Sensor;
import com.nexostock.backend.devices.domain.model.aggregates.SensorReading;
import com.nexostock.backend.devices.domain.model.commands.ReceiveReadingCommand;
import com.nexostock.backend.devices.infrastructure.persistence.jpa.SensorReadingRepository;
import com.nexostock.backend.devices.infrastructure.persistence.jpa.SensorRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SensorCommandServiceImplTest {

    private static final String KEY = "a".repeat(64);

    private SensorRepository sensors;
    private SensorReadingRepository readings;
    private SensorCommandServiceImpl service;
    private Sensor sensor;

    @BeforeEach
    void setUp() {
        sensors = mock(SensorRepository.class);
        readings = mock(SensorReadingRepository.class);
        service = new SensorCommandServiceImpl(
                sensors, readings, mock(ExternalCatalogService.class));

        sensor = new Sensor(1L, "SN-0001", DeviceKeys.sha256(KEY));
        when(sensors.findByDeviceCode("SN-0001")).thenReturn(Optional.of(sensor));
        when(readings.save(any(SensorReading.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ReceiveReadingCommand reading(String key, Instant at) {
        return new ReceiveReadingCommand("SN-0001", key, new BigDecimal("12.5"), at);
    }

    private String errorCode(ReceiveReadingCommand command) {
        return service.handle(command).getError()
                .map(ApplicationError::code)
                .orElse("SUCCESS");
    }

    @Test
    void readingWithoutKeyIsUnauthorized() {
        assertEquals(ApplicationError.UNAUTHORIZED, errorCode(reading(null, null)));
        verify(readings, never()).save(any());
    }

    @Test
    void readingWithAnotherDeviceKeyIsUnauthorized() {
        assertEquals(ApplicationError.UNAUTHORIZED, errorCode(reading("b".repeat(64), null)));
    }

    @Test
    void unknownDeviceIsUnauthorizedToo() {
        var command = new ReceiveReadingCommand("SN-9999", KEY, BigDecimal.ONE, null);

        assertEquals(ApplicationError.UNAUTHORIZED, errorCode(command));
    }

    @Test
    void validReadingIsStored() {
        Instant at = Instant.now().minusSeconds(5);

        assertTrue(service.handle(reading(KEY, at)).isSuccess());
        assertEquals(at, sensor.getLastReadingAt());
    }

    @Test
    void duplicateReadingIsAConflict() {
        Instant at = Instant.now().minusSeconds(5);
        when(readings.existsBySensorIdAndMeasuredAt(any(), any())).thenReturn(true);

        assertEquals(ApplicationError.CONFLICT, errorCode(reading(KEY, at)));
    }

    @Test
    void olderReadingThanTheLastOneIsAConflict() {
        Instant last = Instant.now().minusSeconds(10);
        sensor.registerReading(last, BigDecimal.TEN);

        assertEquals(ApplicationError.CONFLICT, errorCode(reading(KEY, last.minusSeconds(30))));
    }
}
