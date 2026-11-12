package com.nexostock.backend.alerts.domain.model.commands;

import java.util.Objects;

public record RetryNotificationCommand(
        Long businessId,
        Long notificationId) {

    public RetryNotificationCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(
                notificationId, "notificationId is required");
    }
}