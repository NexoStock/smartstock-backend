package com.nexostock.backend.alerts.application.internal.commandservices;

import com.nexostock.backend.alerts.application.internal.services.ChannelConfigurationService;
import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import com.nexostock.backend.alerts.domain.model.commands.RetryNotificationCommand;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.OutboundMessageRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationRetryTest {

    private OutboundMessageRepository queue;
    private AlertCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        queue = mock(OutboundMessageRepository.class);
        service = new AlertCommandServiceImpl(
                mock(AlertRepository.class),
                queue,
                mock(ChannelConfigurationService.class));
    }

    private OutboundMessage message() {
        return new OutboundMessage(
                1L, "email", "prueba@example.com",
                "Stock bajo", "Contenido de prueba");
    }

    @Test
    void failedMessageCanBeRequeued() {
        OutboundMessage message = message();
        message.markFailedAttempt();
        message.markFailedAttempt();
        message.markFailedAttempt();

        assertEquals("FAILED", message.getStatus());

        when(queue.findByIdAndBusinessId(10L, 1L))
                .thenReturn(Optional.of(message));
        when(queue.retryFailed(eq(1L), eq(10L), any(Instant.class)))
                .thenReturn(1);

        var result = service.handle(
                new RetryNotificationCommand(1L, 10L));

        assertTrue(result.getValue().orElseThrow());
        verify(queue).retryFailed(eq(1L), eq(10L), any(Instant.class));
    }

    @Test
    void notificationOutsideBusinessIsRejected() {
        when(queue.findByIdAndBusinessId(10L, 2L))
                .thenReturn(Optional.empty());

        var result = service.handle(
                new RetryNotificationCommand(2L, 10L));

        assertTrue(result.isFailure());
        verify(queue, never()).retryFailed(
                anyLong(), anyLong(), any(Instant.class));
    }

    @Test
    void messageWithoutFailedStatusCannotBeRequeued() {
        when(queue.findByIdAndBusinessId(10L, 1L))
                .thenReturn(Optional.of(message()));
        when(queue.retryFailed(eq(1L), eq(10L), any(Instant.class)))
                .thenReturn(0);

        var result = service.handle(
                new RetryNotificationCommand(1L, 10L));

        assertTrue(result.isFailure());
    }
}