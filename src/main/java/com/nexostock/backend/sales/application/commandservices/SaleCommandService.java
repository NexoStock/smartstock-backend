package com.nexostock.backend.sales.application.commandservices;

import com.nexostock.backend.sales.domain.model.aggregates.Sale;
import com.nexostock.backend.sales.domain.model.commands.CreateSaleCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

public interface SaleCommandService {

    Result<Sale, ApplicationError> handle(CreateSaleCommand command);
}