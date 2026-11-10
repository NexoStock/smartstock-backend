
package com.nexostock.backend.purchasing.application.internal.queryservices;

import com.nexostock.backend.purchasing.application.queryservices.PurchasingQueryService;
import com.nexostock.backend.purchasing.domain.model.aggregates.Purchase;
import com.nexostock.backend.purchasing.domain.model.aggregates.Supplier;
import com.nexostock.backend.purchasing.domain.model.queries.GetAllPurchasesQuery;
import com.nexostock.backend.purchasing.domain.model.queries.GetAllSuppliersQuery;
import com.nexostock.backend.purchasing.domain.model.queries.GetPurchaseByIdQuery;
import com.nexostock.backend.purchasing.domain.model.queries.GetSupplierByIdQuery;
import com.nexostock.backend.purchasing.infrastructure.persistence.jpa.PurchaseRepository;
import com.nexostock.backend.purchasing.infrastructure.persistence.jpa.SupplierRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query service implementation for Purchasing.
 *
 * Handles supplier and purchase queries.
 * Every query is restricted to its business.
 */
@Service
@Transactional(readOnly = true)
public class PurchasingQueryServiceImpl
        implements PurchasingQueryService {

    private final SupplierRepository suppliers;
    private final PurchaseRepository purchases;

    public PurchasingQueryServiceImpl(
            SupplierRepository suppliers,
            PurchaseRepository purchases) {

        this.suppliers = suppliers;
        this.purchases = purchases;
    }

    // ==========================================
    // 1. GET ALL SUPPLIERS
    // ==========================================

    @Override
    public Result<List<Supplier>, ApplicationError> handle(
            GetAllSuppliersQuery query) {

        List<Supplier> result =
                suppliers.findAllByBusinessIdAndActiveTrueOrderByNameAsc(
                        query.businessId()
                );

        return Result.success(result);
    }

    // ==========================================
    // 2. GET SUPPLIER BY ID
    // ==========================================

    @Override
    public Result<Supplier, ApplicationError> handle(
            GetSupplierByIdQuery query) {

        var supplierOptional =
                suppliers.findByBusinessIdAndId(
                        query.businessId(),
                        query.supplierId()
                );

        if (supplierOptional.isEmpty()) {

            return Result.failure(
                    ApplicationError.notFound(
                            "SUPPLIER_NOT_FOUND"
                    )
            );
        }

        return Result.success(
                supplierOptional.get()
        );
    }

    // ==========================================
    // 3. GET ALL PURCHASES
    // ==========================================

    @Override
    public Result<List<Purchase>, ApplicationError> handle(
            GetAllPurchasesQuery query) {

        List<Purchase> result =
                purchases.findAllByBusinessIdOrderByIdDesc(
                        query.businessId()
                );

        // Load purchase items while the
        // read-only transaction is still open.
        //
        // This avoids accessing lazy collections
        // after the transaction has closed.

        for (Purchase purchase : result) {
            purchase.getItems().size();
        }

        return Result.success(result);
    }

    // ==========================================
    // 4. GET PURCHASE BY ID
    // ==========================================

    @Override
    public Result<Purchase, ApplicationError> handle(
            GetPurchaseByIdQuery query) {

        var purchaseOptional =
                purchases.findByBusinessIdAndId(
                        query.businessId(),
                        query.purchaseId()
                );

        if (purchaseOptional.isEmpty()) {

            return Result.failure(
                    ApplicationError.notFound(
                            "PURCHASE_NOT_FOUND"
                    )
            );
        }

        Purchase purchase = purchaseOptional.get();

        // Ensure items are available before
        // returning the purchase.
        purchase.getItems().size();

        return Result.success(purchase);
    }
}
