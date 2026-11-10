
package com.nexostock.backend.purchasing.application.queryservices;

import com.nexostock.backend.purchasing.domain.model.aggregates.Purchase;
import com.nexostock.backend.purchasing.domain.model.aggregates.Supplier;
import com.nexostock.backend.purchasing.domain.model.queries.GetAllPurchasesQuery;
import com.nexostock.backend.purchasing.domain.model.queries.GetAllSuppliersQuery;
import com.nexostock.backend.purchasing.domain.model.queries.GetPurchaseByIdQuery;
import com.nexostock.backend.purchasing.domain.model.queries.GetSupplierByIdQuery;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;

/**
 * Application query service for the Purchasing context.
 *
 * Servicio de consultas para proveedores y compras.
 *
 * Every query must be restricted to the requested business.
 */
public interface PurchasingQueryService {

    /**
     * Returns the active suppliers of a business.
     */
    Result<List<Supplier>, ApplicationError> handle(
            GetAllSuppliersQuery query
    );

    /**
     * Finds a supplier by ID within its business.
     */
    Result<Supplier, ApplicationError> handle(
            GetSupplierByIdQuery query
    );

    /**
     * Returns the purchases of a business.
     */
    Result<List<Purchase>, ApplicationError> handle(
            GetAllPurchasesQuery query
    );

    /**
     * Finds a purchase by ID within its business.
     */
    Result<Purchase, ApplicationError> handle(
            GetPurchaseByIdQuery query
    );
}
