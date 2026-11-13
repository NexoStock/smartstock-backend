
package com.nexostock.backend.inventory.application.internal.queryservices;

import com.nexostock.backend.catalog.interfaces.acl.CatalogContextFacade;
import com.nexostock.backend.inventory.application.queryservices.MovementQueryService;
import com.nexostock.backend.inventory.domain.model.aggregates.Movement;
import com.nexostock.backend.inventory.domain.model.queries.GetMovementsByProductQuery;
import com.nexostock.backend.inventory.infrastructure.persistence.jpa.MovementRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovementQueryServiceImpl
        implements MovementQueryService {

    private final CatalogContextFacade catalog;
    private final MovementRepository movements;

    public MovementQueryServiceImpl(
            CatalogContextFacade catalog,
            MovementRepository movements) {
        this.catalog = catalog;
        this.movements = movements;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<Movement>, ApplicationError> handle(
            GetMovementsByProductQuery query) {

        // Verificar que el producto pertenezca
        // a la empresa autenticada y esté activo.
        var product = catalog.findProduct(
                query.businessId(),
                query.productId()
        );

        if (product.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "PRODUCT_NOT_FOUND: "
                                    + query.productId()
                    )
            );
        }

        // Obtener historial de movimientos.
        List<Movement> history =
                movements
                        .findAllByBusinessIdAndProductIdOrderByCreatedAtDesc(
                                query.businessId(),
                                query.productId()
                        );

        return Result.success(history);
    }
}
