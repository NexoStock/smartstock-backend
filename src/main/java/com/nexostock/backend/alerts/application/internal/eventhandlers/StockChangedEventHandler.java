package com.nexostock.backend.alerts.application.internal.eventhandlers;

import com.nexostock.backend.alerts.application.commandservices.AlertCommandService;
import com.nexostock.backend.alerts.domain.model.commands.EvaluateLowStockCommand;
import com.nexostock.backend.catalog.domain.model.events.ProductCreatedEvent;
import com.nexostock.backend.catalog.domain.model.events.ThresholdChangedEvent;
import com.nexostock.backend.catalog.interfaces.acl.CatalogContextFacade;
import com.nexostock.backend.inventory.domain.model.events.StockChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class StockChangedEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(StockChangedEventHandler.class);

    private final AlertCommandService alerts;
    private final CatalogContextFacade catalog;

    public StockChangedEventHandler(
            AlertCommandService alerts,
            CatalogContextFacade catalog) {
        this.alerts = alerts;
        this.catalog = catalog;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(StockChangedEvent event) {
        evaluate(event.businessId(), event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProductCreatedEvent event) {
        evaluate(event.businessId(), event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ThresholdChangedEvent event) {
        evaluate(event.businessId(), event.productId());
    }

    private void evaluate(Long businessId, Long productId) {
        catalog.findProduct(businessId, productId).ifPresent(product -> {
            try {
                alerts.handle(new EvaluateLowStockCommand(
                        businessId,
                        productId,
                        product.name(),
                        product.registeredStock(),
                        product.minimumThreshold()));
            } catch (DataIntegrityViolationException exception) {
                log.debug(
                        "Alert already opened by a concurrent event (product {})",
                        productId);
            }
        });
    }
}