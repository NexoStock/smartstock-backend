package com.nexostock.backend.alerts.application.internal.commandservices;

import com.nexostock.backend.alerts.application.commandservices.AlertCommandService;
import com.nexostock.backend.alerts.application.internal.services.ChannelConfigurationService;
import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.domain.model.aggregates.AlertType;
import com.nexostock.backend.alerts.domain.model.aggregates.NotificationChannel;
import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import com.nexostock.backend.alerts.domain.model.commands.EvaluateLowStockCommand;
import com.nexostock.backend.alerts.domain.model.commands.UpdateChannelsCommand;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.OutboundMessageRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.nexostock.backend.alerts.domain.model.commands.RetryNotificationCommand;

@Service
public class AlertCommandServiceImpl implements AlertCommandService {

    private final AlertRepository alerts;
    private final OutboundMessageRepository queue;
    private final ChannelConfigurationService channels;

    public AlertCommandServiceImpl(
            AlertRepository alerts,
            OutboundMessageRepository queue,
            ChannelConfigurationService channels) {
        this.alerts = alerts;
        this.queue = queue;
        this.channels = channels;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result<Boolean, ApplicationError> handle(
            EvaluateLowStockCommand command) {

        var open = alerts.findByBusinessIdAndProductIdAndTypeAndActiveTrue(
                command.businessId(),
                command.productId(),
                AlertType.LOW_STOCK);

        boolean low =
                command.referenceStock() <= command.minimumThreshold();

        if (!low) {
            open.ifPresent(alert -> {
                alert.resolve();
                alerts.save(alert);
            });
            return Result.success(false);
        }

        if (open.isPresent()) {
            open.get().refresh(command.referenceStock());
            alerts.save(open.get());
            return Result.success(false);
        }

        var configuration = channels.findOrCreate(command.businessId());

        if (configuration.isFailure()) {
            return Result.failure(
                    configuration.getError().orElseThrow());
        }

        alerts.save(new Alert(
                command.businessId(),
                command.productId(),
                AlertType.LOW_STOCK,
                command.referenceStock()));

        String text = "Low stock: " + command.productName()
                + " has " + command.referenceStock()
                + " units (minimum " + command.minimumThreshold() + ").";

        for (var channel : configuration.getValue().orElseThrow()) {
            if (channel.isActive()) {
                queue.save(new OutboundMessage(
                        command.businessId(),
                        channel.getChannel(),
                        channel.getDestination(),
                        "NexoStock: low stock",
                        text));
            }
        }

        return Result.success(true);
    }

    @Override
    @Transactional
    public Result<List<NotificationChannel>, ApplicationError> handle(
            UpdateChannelsCommand command) {
        return channels.update(command);
    }

    @Override
    @Transactional
    public Result<Boolean, ApplicationError> handle(
            RetryNotificationCommand command) {

        var message = queue.findByIdAndBusinessId(
                command.notificationId(), command.businessId());

        if (message.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("NOTIFICATION_NOT_FOUND"));
        }

        int updated = queue.retryFailed(
                command.businessId(),
                command.notificationId(),
                java.time.Instant.now());

        if (updated == 0) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "ONLY_FAILED_NOTIFICATIONS_CAN_BE_RETRIED"));
        }

        return Result.success(true);
    }
}