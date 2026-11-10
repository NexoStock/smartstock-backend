package com.nexostock.backend.alerts.infrastructure.notifications;

import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;

public interface NotificationSender {

    boolean supports(String channel);

    void send(OutboundMessage message);
}