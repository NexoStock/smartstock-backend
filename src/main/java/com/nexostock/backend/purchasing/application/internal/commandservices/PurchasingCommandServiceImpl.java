
package com.nexostock.backend.purchasing.application.internal.commandservices;

import com.nexostock.backend.catalog.interfaces.acl.CatalogContextFacade;
import com.nexostock.backend.inventory.interfaces.acl.InventoryContextFacade;
import com.nexostock.backend.purchasing.application.commandservices.PurchasingCommandService;
import com.nexostock.backend.purchasing.domain.model.aggregates.Purchase;
import com.nexostock.backend.purchasing.domain.model.aggregates.PurchaseItem;
import com.nexostock.backend.purchasing.domain.model.aggregates.PurchaseStatus;
import com.nexostock.backend.purchasing.domain.model.aggregates.Supplier;
import com.nexostock.backend.purchasing.domain.model.commands.CancelPurchaseCommand;
import com.nexostock.backend.purchasing.domain.model.commands.CreatePurchaseCommand;
import com.nexostock.backend.purchasing.domain.model.commands.CreateSupplierCommand;
import com.nexostock.backend.purchasing.domain.model.commands.DeactivateSupplierCommand;
import com.nexostock.backend.purchasing.domain.model.commands.ReceivePurchaseCommand;
import com.nexostock.backend.purchasing.domain.model.commands.UpdateSupplierCommand;
import com.nexostock.backend.purchasing.infrastructure.persistence.jpa.PurchaseRepository;
import com.nexostock.backend.purchasing.infrastructure.persistence.jpa.SupplierRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
@Transactional
public class PurchasingCommandServiceImpl
        implements PurchasingCommandService {

    private final SupplierRepository suppliers;
    private final PurchaseRepository purchases;
    private final CatalogContextFacade catalog;
    private final InventoryContextFacade inventory;

    public PurchasingCommandServiceImpl(
            SupplierRepository suppliers,
            PurchaseRepository purchases,
            CatalogContextFacade catalog,
            InventoryContextFacade inventory) {

        this.suppliers = suppliers;
        this.purchases = purchases;
        this.catalog = catalog;
        this.inventory = inventory;
    }

    // ==========================================
    // 1. CREATE SUPPLIER
    // ==========================================

    @Override
    public Result<Supplier, ApplicationError> handle(
            CreateSupplierCommand command) {

        if (suppliers.existsByBusinessIdAndTaxId(
                command.businessId(),
                command.taxId())) {

            return Result.failure(
                    ApplicationError.conflict(
                            "SUPPLIER_TAX_ID_ALREADY_EXISTS"
                    )
            );
        }

        Supplier supplier = new Supplier(
                command.businessId(),
                command.name(),
                command.taxId(),
                command.email(),
                command.phone(),
                command.address()
        );

        return Result.success(
                suppliers.save(supplier)
        );
    }

    // ==========================================
    // 2. UPDATE SUPPLIER
    // ==========================================

    @Override
    public Result<Supplier, ApplicationError> handle(
            UpdateSupplierCommand command) {

        var supplierOptional =
                suppliers.findByBusinessIdAndIdAndActiveTrue(
                        command.businessId(),
                        command.supplierId()
                );

        if (supplierOptional.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "SUPPLIER_NOT_FOUND"
                    )
            );
        }

        if (suppliers.existsByBusinessIdAndTaxIdAndIdNot(
                command.businessId(),
                command.taxId(),
                command.supplierId())) {

            return Result.failure(
                    ApplicationError.conflict(
                            "SUPPLIER_TAX_ID_ALREADY_EXISTS"
                    )
            );
        }

        Supplier supplier = supplierOptional.get();

        supplier.update(
                command.name(),
                command.taxId(),
                command.email(),
                command.phone(),
                command.address()
        );

        return Result.success(
                suppliers.save(supplier)
        );
    }

    // ==========================================
    // 3. DEACTIVATE SUPPLIER
    // ==========================================

    @Override
    public Result<Supplier, ApplicationError> handle(
            DeactivateSupplierCommand command) {

        var supplierOptional =
                suppliers.findByBusinessIdAndId(
                        command.businessId(),
                        command.supplierId()
                );

        if (supplierOptional.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "SUPPLIER_NOT_FOUND"
                    )
            );
        }

        Supplier supplier = supplierOptional.get();

        if (supplier.isActive()) {
            supplier.deactivate();
            supplier = suppliers.save(supplier);
        }

        return Result.success(supplier);
    }

    // ==========================================
    // 4. CREATE PURCHASE
    // ==========================================

    @Override
    public Result<Purchase, ApplicationError> handle(
            CreatePurchaseCommand command) {

        var supplierOptional =
                suppliers.findByBusinessIdAndIdAndActiveTrue(
                        command.businessId(),
                        command.supplierId()
                );

        if (supplierOptional.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "SUPPLIER_NOT_FOUND_OR_INACTIVE"
                    )
            );
        }

        Set<Long> productIds = new HashSet<>();

        for (PurchaseItem item : command.items()) {

            if (!productIds.add(item.getProductId())) {
                return Result.failure(
                        ApplicationError.validationError(
                                "DUPLICATED_PRODUCT_IN_PURCHASE"
                        )
                );
            }

            if (catalog.findProduct(
                    command.businessId(),
                    item.getProductId()
            ).isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "PRODUCT_NOT_FOUND: "
                                        + item.getProductId()
                        )
                );
            }
        }

        Purchase purchase = new Purchase(
                command.businessId(),
                command.supplierId(),
                command.items()
        );

        return Result.success(
                purchases.save(purchase)
        );
    }

    // ==========================================
    // 5. RECEIVE PURCHASE
    // ==========================================

    @Override
    public Result<Purchase, ApplicationError> handle(
            ReceivePurchaseCommand command) {

        // Bloqueamos la compra para evitar
        // recepciones simultaneas de la misma compra.

        var purchaseOptional =
                purchases.findForReceiving(
                        command.businessId(),
                        command.purchaseId()
                );

        if (purchaseOptional.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "PURCHASE_NOT_FOUND"
                    )
            );
        }

        Purchase purchase = purchaseOptional.get();

        // Una compra recibida no debe
        // aumentar nuevamente el inventario.

        if (purchase.getStatus() == PurchaseStatus.RECEIVED) {
            return Result.success(purchase);
        }

        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "CANCELLED_PURCHASE_CANNOT_BE_RECEIVED"
                    )
            );
        }

        // Comprobamos que todos los productos
        // pertenezcan al negocio correspondiente.

        for (PurchaseItem item : purchase.getItems()) {

            if (catalog.findProduct(
                    command.businessId(),
                    item.getProductId()
            ).isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "PRODUCT_NOT_FOUND: "
                                        + item.getProductId()
                        )
                );
            }
        }

        // Procesamos siempre en el mismo orden
        // para reducir conflictos de bloqueo
        // entre compras concurrentes.

        List<PurchaseItem> orderedItems =
                purchase.getItems()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        PurchaseItem::getProductId
                                )
                        )
                        .toList();

        for (PurchaseItem item : orderedItems) {

            String reference =
                    "PURCHASE-"
                            + purchase.getId()
                            + "-PRODUCT-"
                            + item.getProductId();

            // Inventory es el unico contexto
            // que debe modificar el stock.
            //
            // Purchasing utiliza su fachada ACL.

            var stockResult =
                    inventory.addStockForPurchase(
                            command.businessId(),
                            item.getProductId(),
                            item.getQuantity(),
                            reference
                    );

            if (stockResult.isFailure()) {

                // Si falla cualquiera de las entradas
                // de stock, revertimos toda la
                // transaccion de recepcion.

                TransactionAspectSupport
                        .currentTransactionStatus()
                        .setRollbackOnly();

                ApplicationError error =
                        stockResult.getError().orElse(
                                ApplicationError.unexpectedError(
                                        "PURCHASE_STOCK_UPDATE_FAILED"
                                )
                        );

                return Result.failure(error);
            }
        }

        // Solo marcamos la compra como recibida
        // despues de registrar todo el stock.

        purchase.receive();

        return Result.success(
                purchases.save(purchase)
        );
    }

    // ==========================================
    // 6. CANCEL PURCHASE
    // ==========================================

    @Override
    public Result<Purchase, ApplicationError> handle(
            CancelPurchaseCommand command) {

        var purchaseOptional =
                purchases.findForReceiving(
                        command.businessId(),
                        command.purchaseId()
                );

        if (purchaseOptional.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "PURCHASE_NOT_FOUND"
                    )
            );
        }

        Purchase purchase = purchaseOptional.get();

        if (purchase.getStatus() == PurchaseStatus.RECEIVED) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "RECEIVED_PURCHASE_CANNOT_BE_CANCELLED"
                    )
            );
        }

        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            return Result.success(purchase);
        }

        purchase.cancel();

        return Result.success(
                purchases.save(purchase)
        );
    }
}
