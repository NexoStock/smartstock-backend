package com.nexostock.backend.alerts.application.commandservices;

import com.nexostock.backend.alerts.domain.model.aggregates.NotificationChannel;
import com.nexostock.backend.alerts.domain.model.commands.EvaluateLowStockCommand;
import com.nexostock.backend.alerts.domain.model.commands.RetryNotificationCommand;
import com.nexostock.backend.alerts.domain.model.commands.UpdateChannelsCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;

public interface AlertCommandService {

    Result<Boolean, ApplicationError> handle(
            EvaluateLowStockCommand command);

    Result<List<NotificationChannel>, ApplicationError> handle(
            UpdateChannelsCommand command);

    Result<Boolean, ApplicationError> handle(
            RetryNotificationCommand command);
}