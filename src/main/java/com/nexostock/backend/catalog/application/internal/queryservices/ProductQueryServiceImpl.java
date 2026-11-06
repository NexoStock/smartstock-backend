
package com.nexostock.backend.catalog.application.internal.queryservices;

import com.nexostock.backend.catalog.application.queryservices.ProductQueryService;
import com.nexostock.backend.catalog.domain.model.aggregates.Product;
import com.nexostock.backend.catalog.domain.model.queries.GetAllProductsQuery;
import com.nexostock.backend.catalog.domain.model.queries.GetProductByIdQuery;
import com.nexostock.backend.catalog.infrastructure.persistence.jpa.ProductRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductQueryServiceImpl
        implements ProductQueryService {

    private final ProductRepository products;

    public ProductQueryServiceImpl(
            ProductRepository products) {
        this.products = products;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<Product>, ApplicationError> handle(
            GetAllProductsQuery query) {

        return Result.success(
                products.findAllByBusinessIdAndActiveTrue(
                        query.businessId()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Product, ApplicationError> handle(
            GetProductByIdQuery query) {

        return products
                .findByIdAndBusinessIdAndActiveTrue(
                        query.productId(),
                        query.businessId()
                )
                .map(p ->
                        Result.<Product, ApplicationError>success(p)
                )
                .orElseGet(() ->
                        Result.failure(
                                ApplicationError.notFound(
                                        "PRODUCT_NOT_FOUND: "
                                                + query.productId()
                                )
                        )
                );
    }
}
