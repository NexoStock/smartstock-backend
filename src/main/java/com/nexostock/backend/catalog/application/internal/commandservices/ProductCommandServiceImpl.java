
package com.nexostock.backend.catalog.application.internal.commandservices;

import com.nexostock.backend.catalog.application.commandservices.ProductCommandService;
import com.nexostock.backend.catalog.domain.model.aggregates.Product;
import com.nexostock.backend.catalog.domain.model.commands.CreateProductCommand;
import com.nexostock.backend.catalog.domain.model.commands.RetireProductCommand;
import com.nexostock.backend.catalog.domain.model.commands.UpdateProductCommand;
import com.nexostock.backend.catalog.domain.model.commands.UpdateThresholdCommand;
import com.nexostock.backend.catalog.domain.model.events.ProductCreatedEvent;
import com.nexostock.backend.catalog.domain.model.events.ThresholdChangedEvent;
import com.nexostock.backend.catalog.infrastructure.persistence.jpa.ProductRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class ProductCommandServiceImpl
        implements ProductCommandService {

    private final ProductRepository products;
    private final ApplicationEventPublisher events;

    public ProductCommandServiceImpl(
            ProductRepository products,
            ApplicationEventPublisher events) {

        this.products = products;
        this.events = events;
    }

    @Override
    @Transactional
    public Result<Product, ApplicationError> handle(
            CreateProductCommand command) {

        if (products.existsByBusinessIdAndSku(
                command.businessId(),
                command.sku())) {

            return Result.failure(
                    ApplicationError.conflict(
                            "DUPLICATE_SKU: " + command.sku().value()
                    )
            );
        }

        try {
            Product saved = products.save(
                    new Product(command)
            );

            events.publishEvent(
                    new ProductCreatedEvent(
                            saved.getId(),
                            saved.getBusinessId(),
                            command.initialStock()
                    )
            );

            return Result.success(saved);

        } catch (IllegalStateException e) {
            return rollback(e);
        }
    }

    @Override
    @Transactional
    public Result<Product, ApplicationError> handle(
            UpdateProductCommand command) {

        var found = products.findByIdAndBusinessIdAndActiveTrue(
                command.productId(),
                command.businessId()
        );

        if (found.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "PRODUCT_NOT_FOUND: " + command.productId()
                    )
            );
        }

        if (command.sku() != null
                && products.existsByBusinessIdAndSkuAndIdNot(
                command.businessId(),
                command.sku(),
                command.productId())) {

            return Result.failure(
                    ApplicationError.conflict(
                            "DUPLICATE_SKU: " + command.sku().value()
                    )
            );
        }

        try {
            Product product = found.get();

            product.update(command);

            Product saved = products.save(product);

            events.publishEvent(
                    new ThresholdChangedEvent(
                            saved.getBusinessId(),
                            saved.getId()
                    )
            );

            return Result.success(saved);

        } catch (IllegalStateException e) {
            return rollback(e);
        }
    }

    @Override
    @Transactional
    public Result<Product, ApplicationError> handle(
            UpdateThresholdCommand command) {

        var found = products.findByIdAndBusinessIdAndActiveTrue(
                command.productId(),
                command.businessId()
        );

        if (found.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "PRODUCT_NOT_FOUND: " + command.productId()
                    )
            );
        }

        try {
            Product product = found.get();

            product.changeThreshold(
                    command.minimumThreshold()
            );

            Product saved = products.save(product);

            events.publishEvent(
                    new ThresholdChangedEvent(
                            saved.getBusinessId(),
                            saved.getId()
                    )
            );

            return Result.success(saved);

        } catch (IllegalStateException e) {
            return rollback(e);
        }
    }

    @Override
    @Transactional
    public Result<Long, ApplicationError> handle(
            RetireProductCommand command) {

        var found = products.findByIdAndBusinessIdAndActiveTrue(
                command.productId(),
                command.businessId()
        );

        if (found.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "PRODUCT_NOT_FOUND: " + command.productId()
                    )
            );
        }

        try {
            Product product = found.get();

            product.retire();

            products.save(product);

            return Result.success(
                    command.productId()
            );

        } catch (IllegalStateException e) {
            return rollback(e);
        }
    }

    /**
     * Roll back the transaction when a business rule fails.
     */
    private static <T> Result<T, ApplicationError> rollback(
            IllegalStateException e) {

        TransactionAspectSupport
                .currentTransactionStatus()
                .setRollbackOnly();

        return Result.failure(
                ApplicationError.businessRuleViolation(
                        e.getMessage()
                )
        );
    }
}
