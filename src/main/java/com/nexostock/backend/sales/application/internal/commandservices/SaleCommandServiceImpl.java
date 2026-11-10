package com.nexostock.backend.sales.application.internal.commandservices;

import com.nexostock.backend.catalog.interfaces.acl.CatalogContextFacade;
import com.nexostock.backend.inventory.interfaces.acl.InventoryContextFacade;
import com.nexostock.backend.sales.application.commandservices.SaleCommandService;
import com.nexostock.backend.sales.domain.model.aggregates.Sale;
import com.nexostock.backend.sales.domain.model.aggregates.SaleItem;
import com.nexostock.backend.sales.domain.model.commands.CreateSaleCommand;
import com.nexostock.backend.sales.infrastructure.persistence.jpa.SaleItemRepository;
import com.nexostock.backend.sales.infrastructure.persistence.jpa.SaleRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class SaleCommandServiceImpl implements SaleCommandService {

    private final SaleRepository sales;
    private final SaleItemRepository items;
    private final CatalogContextFacade catalog;
    private final InventoryContextFacade inventory;

    public SaleCommandServiceImpl(
            SaleRepository sales,
            SaleItemRepository items,
            CatalogContextFacade catalog,
            InventoryContextFacade inventory) {
        this.sales = sales;
        this.items = items;
        this.catalog = catalog;
        this.inventory = inventory;
    }

    @Override
    @Transactional
    public Result<Sale, ApplicationError> handle(CreateSaleCommand command) {

        if (command.requestKey() != null) {
            var existing = sales.findByBusinessIdAndRequestKey(
                    command.businessId(), command.requestKey());

            if (existing.isPresent()) {
                return Result.success(existing.get());
            }
        }

        Sale sale = sales.save(new Sale(
                command.businessId(),
                "V-" + UUID.randomUUID().toString().substring(0, 8),
                command.requestKey()));

        for (CreateSaleCommand.Line line : command.lines()) {
            var product = catalog.findProduct(
                    command.businessId(), line.productId());

            if (product.isEmpty()) {
                return rollback(ApplicationError.notFound(
                        "PRODUCT_NOT_FOUND: " + line.productId()));
            }

            BigDecimal price = product.get().salePrice();

            if (price == null || price.signum() <= 0) {
                return rollback(ApplicationError.businessRuleViolation(
                        "PRODUCT_WITHOUT_SALE_PRICE: " + line.productId()));
            }

            var movement = inventory.removeStockForSale(
                    command.businessId(),
                    line.productId(),
                    line.quantity(),
                    sale.getCode());

            if (movement.isFailure()) {
                return rollback(movement.getError().orElseThrow());
            }

            items.save(new SaleItem(
                    sale.getId(),
                    line.productId(),
                    line.quantity(),
                    price));

            sale.addToTotal(
                    price.multiply(BigDecimal.valueOf(line.quantity())));
        }

        return Result.success(sales.save(sale));
    }

    private Result<Sale, ApplicationError> rollback(ApplicationError error) {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return Result.failure(error);
    }
}