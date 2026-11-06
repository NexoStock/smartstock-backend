
package com.nexostock.backend.catalog.application.queryservices;

import com.nexostock.backend.catalog.domain.model.aggregates.Product;
import com.nexostock.backend.catalog.domain.model.queries.GetAllProductsQuery;
import com.nexostock.backend.catalog.domain.model.queries.GetProductByIdQuery;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;

/**
 * Product query service.
 * Two queries, two handle methods.
 */
public interface ProductQueryService {

    Result<List<Product>, ApplicationError> handle(
            GetAllProductsQuery query
    );

    Result<Product, ApplicationError> handle(
            GetProductByIdQuery query
    );
}
