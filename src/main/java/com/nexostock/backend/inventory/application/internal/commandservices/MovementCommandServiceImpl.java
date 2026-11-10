package com.nexostock.backend.inventory.application.internal.commandservices;

import com.nexostock.backend.catalog.interfaces.acl.CatalogContextFacade;
import com.nexostock.backend.catalog.interfaces.acl.ProductSnapshot;
import com.nexostock.backend.inventory.application.commandservices.MovementCommandService;
import com.nexostock.backend.inventory.domain.model.aggregates.Movement;
import com.nexostock.backend.inventory.domain.model.aggregates.MovementType;
import com.nexostock.backend.inventory.domain.model.commands.AdjustStockCommand;
import com.nexostock.backend.inventory.domain.model.commands.RegisterMovementCommand;
import com.nexostock.backend.inventory.domain.model.events.StockChangedEvent;
import com.nexostock.backend.inventory.infrastructure.persistence.jpa.MovementRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class MovementCommandServiceImpl implements MovementCommandService {

  private final CatalogContextFacade catalog;
  private final MovementRepository movements;
  private final ApplicationEventPublisher events;

  public MovementCommandServiceImpl(
      CatalogContextFacade catalog, MovementRepository movements, ApplicationEventPublisher events) {
    this.catalog = catalog;
    this.movements = movements;
    this.events = events;
  }

  @Override
  @Transactional
  public Result<Movement, ApplicationError> handle(RegisterMovementCommand command) {
    var product = catalog.findProductForUpdate(command.businessId(), command.productId());
    if (product.isEmpty()) {
      return Result.failure(ApplicationError.notFound("PRODUCT_NOT_FOUND: " + command.productId()));
    }
    int newStock = product.get().registeredStock() + command.quantity();
    return apply(command.businessId(), product.get(), command.type(), command.quantity(), newStock,
        command.reference(), command.reason());
  }

  @Override
  @Transactional
  public Result<Movement, ApplicationError> handle(AdjustStockCommand command) {
    var product = catalog.findProductForUpdate(command.businessId(), command.productId());
    if (product.isEmpty()) {
      return Result.failure(ApplicationError.notFound("PRODUCT_NOT_FOUND: " + command.productId()));
    }
    int delta = command.newStock() - product.get().registeredStock();
    if (delta == 0) {
      return Result.failure(ApplicationError.businessRuleViolation("NO_STOCK_CHANGE"));
    }
    return apply(command.businessId(), product.get(), MovementType.ADJUSTMENT, delta, command.newStock(),
        "ADJ-" + System.currentTimeMillis(), command.reason());
  }

  private Result<Movement, ApplicationError> apply(
      Long businessId, ProductSnapshot product, MovementType type, int delta, int newStock,
      String reference, String reason) {
    try {
      ProductSnapshot updated = catalog.applyStock(businessId, product.id(), newStock).orElseThrow();
      Movement saved =
          movements.save(new Movement(businessId, product.id(), type, delta, newStock, reference, reason));
      events.publishEvent(
          new StockChangedEvent(businessId, product.id(), newStock, updated.minimumThreshold()));
      return Result.success(saved);
    } catch (IllegalStateException e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return Result.failure(ApplicationError.businessRuleViolation(e.getMessage()));
    }
  }
}