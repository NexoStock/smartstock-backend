
package com.nexostock.backend.catalog.interfaces.rest.transform;

import com.nexostock.backend.catalog.domain.model.aggregates.Product;
import com.nexostock.backend.catalog.interfaces.rest.resources.ProductResource;

/**
 * Converts a Product entity into an API response.
 * Convierte un producto en una respuesta de la API.
 */
public class ProductResourceFromEntityAssembler {

    private ProductResourceFromEntityAssembler() {
    }

    public static ProductResource toResource(Product p) {

        return new ProductResource(
                p.getId(),
                p.getName().value(),
                p.getSku().value(),
                p.getCategory(),
                p.getUnitWeightKg(),
                p.getSalePrice().value(),
                p.getPurchaseCost().value(),
                p.getUsualSupplier(),
                p.getRegisteredStock(),
                p.getMinimumThreshold(),
                p.getMaximumCapacity()
        );
    }
}
