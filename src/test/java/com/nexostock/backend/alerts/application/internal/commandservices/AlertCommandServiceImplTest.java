package com.nexostock.backend.alerts.application.internal.commandservices;

import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.domain.model.aggregates.AlertType;
import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import com.nexostock.backend.alerts.domain.model.commands.EvaluateLowStockCommand;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.OutboundMessageRepository;
import com.nexostock.backend.iam.interfaces.acl.IamContextFacade;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlertCommandServiceImplTest {

    private AlertRepository alerts;
    private OutboundMessageRepository queue;
    private IamContextFacade iam;
    private AlertCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        alerts = mock(AlertRepository.class);
        queue = mock(OutboundMessageRepository.class);
        iam = mock(IamContextFacade.class);
        service = new AlertCommandServiceImpl(alerts, queue, iam);
    }

    private EvaluateLowStockCommand command(int stock) {
        return new EvaluateLowStockCommand(
                1L, 10L, "Arroz", stock, 5);
    }

    private void existingAlert(Optional<Alert> alert) {
        when(alerts.findByBusinessIdAndProductIdAndTypeAndActiveTrue(
                1L, 10L, AlertType.LOW_STOCK))
                .thenReturn(alert);
    }

    @Test
    void lowStockCreatesAlertAndQueuesBothChannels() {
        existingAlert(Optional.empty());
        when(iam.findContact(1L)).thenReturn(Optional.of(
                new IamContextFacade.BusinessContact(
                        1L, "negocio@example.com", "+51999999999")));

        var result = service.handle(command(3));

        assertTrue(result.getValue().orElseThrow());
        verify(alerts).save(any(Alert.class));
        verify(queue, times(2)).save(any(OutboundMessage.class));
        verify(iam).findContact(1L);
    }

    @Test
    void existingAlertIsUpdatedWithoutNewNotifications() {
        Alert alert = new Alert(1L, 10L, AlertType.LOW_STOCK, 3);
        existingAlert(Optional.of(alert));

        var result = service.handle(command(2));

        assertFalse(result.getValue().orElseThrow());
        assertEquals(2, alert.getReferenceStock());
        assertTrue(alert.isActive());
        verify(alerts).save(alert);
        verifyNoInteractions(queue, iam);
    }

    @Test
    void recoveredStockResolvesExistingAlert() {
        Alert alert = new Alert(1L, 10L, AlertType.LOW_STOCK, 3);
        existingAlert(Optional.of(alert));

        var result = service.handle(command(8));

        assertFalse(result.getValue().orElseThrow());
        assertFalse(alert.isActive());
        assertNotNull(alert.getResolvedAt());
        verify(alerts).save(alert);
        verifyNoInteractions(queue, iam);
    }

    @Test
    void healthyStockWithoutAlertMakesNoChanges() {
        existingAlert(Optional.empty());

        var result = service.handle(command(8));

        assertFalse(result.getValue().orElseThrow());
        verify(alerts, never()).save(any(Alert.class));
        verifyNoInteractions(queue, iam);
    }
}