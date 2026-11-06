
package com.nexostock.backend.catalog.application.commandservices;

import com.nexostock.backend.catalog.domain.model.aggregates.Product;
import com.nexostock.backend.catalog.domain.model.commands.CreateProductCommand;
import com.nexostock.backend.catalog.domain.model.commands.RetireProductCommand;
import com.nexostock.backend.catalog.domain.model.commands.UpdateProductCommand;
import com.nexostock.backend.catalog.domain.model.commands.UpdateThresholdCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

/**
 * Product command service.
 * Four commands, four handle methods.
 */
public interface ProductCommandService {

    Result<Product, ApplicationError> handle(
            CreateProductCommand command
    );

    Result<Product, ApplicationError> handle(
            UpdateProductCommand command
    );

    Result<Product, ApplicationError> handle(
            UpdateThresholdCommand command
    );

    Result<Long, ApplicationError> handle(
            RetireProductCommand command
    );
}
