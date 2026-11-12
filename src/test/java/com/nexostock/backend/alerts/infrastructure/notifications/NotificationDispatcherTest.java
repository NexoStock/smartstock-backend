package com.nexostock.backend.alerts.infrastructure.notifications;

import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.OutboundMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationDispatcherTest {

    private OutboundMessageRepository queue;
    private NotificationSender sender;
    private NotificationDispatcher dispatcher;
    private OutboundMessage message;

    @BeforeEach
    void setUp() {
        queue = mock(OutboundMessageRepository.class);
        sender = mock(NotificationSender.class);
        dispatcher = new NotificationDispatcher(queue, List.of(sender));

        message = new OutboundMessage(
                1L, "email", "prueba@example.com",
                "Stock bajo", "Producto con stock bajo");

        ReflectionTestUtils.setField(message, "id", 10L);

        when(queue.findTop20ByStatusAndNextAttemptAtLessThanEqualOrderById(
                eq("PENDING"), any(Instant.class)))
                .thenReturn(List.of(message));
    }

    private void availableMessage() {
        when(queue.claim(eq(10L), any(Instant.class))).thenReturn(1);
        when(queue.findById(10L)).thenReturn(Optional.of(message));
        when(sender.supports("email")).thenReturn(true);
    }

    @Test
    void acceptedMessageIsMarkedSent() {
        availableMessage();

        dispatcher.dispatchPending();

        verify(sender).send(message);
        assertEquals("SENT", message.getStatus());
        assertEquals(1, message.getAttempts());
        verify(queue).save(message);
    }

    @Test
    void providerFailureLeavesMessagePendingForRetry() {
        availableMessage();
        doThrow(new IllegalStateException("Provider unavailable"))
                .when(sender).send(message);

        dispatcher.dispatchPending();

        assertEquals("PENDING", message.getStatus());
        assertEquals(1, message.getAttempts());
        verify(queue).save(message);
    }

    @Test
    void thirdFailureMarksMessageFailed() {
        availableMessage();
        message.markFailedAttempt();
        message.markFailedAttempt();

        doThrow(new IllegalStateException("Provider unavailable"))
                .when(sender).send(message);

        dispatcher.dispatchPending();

        assertEquals("FAILED", message.getStatus());
        assertEquals(3, message.getAttempts());
        verify(queue).save(message);
    }

    @Test
    void messageClaimedByAnotherProcessIsNotSent() {
        when(queue.claim(eq(10L), any(Instant.class))).thenReturn(0);

        dispatcher.dispatchPending();

        verify(queue, never()).findById(anyLong());
        verify(queue, never()).save(any(OutboundMessage.class));
        verifyNoInteractions(sender);
    }
}