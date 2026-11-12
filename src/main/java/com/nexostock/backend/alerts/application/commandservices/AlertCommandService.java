package com.nexostock.backend.alerts.application.commandservices;

import com.nexostock.backend.alerts.domain.model.commands.EvaluateLowStockCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

public interface AlertCommandService {

    Result<Boolean, ApplicationError> handle(
            EvaluateLowStockCommand command);
}