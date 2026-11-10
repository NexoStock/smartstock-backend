package com.nexostock.backend.inventory.application.commandservices;

import com.nexostock.backend.inventory.domain.model.aggregates.Movement;
import com.nexostock.backend.inventory.domain.model.commands.AdjustStockCommand;
import com.nexostock.backend.inventory.domain.model.commands.RegisterMovementCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

/** 2 commands defined = 2 handled. */
public interface MovementCommandService {
  Result<Movement, ApplicationError> handle(RegisterMovementCommand command);

  Result<Movement, ApplicationError> handle(AdjustStockCommand command);
}