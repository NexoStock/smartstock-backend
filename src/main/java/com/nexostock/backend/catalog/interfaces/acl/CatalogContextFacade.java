
package com.nexostock.backend.catalog.interfaces.acl;

import com.nexostock.backend.catalog.domain.model.aggregates.Product;
import com.nexostock.backend.catalog.infrastructure.persistence.jpa.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Door of the Catalog context for Inventory,
 * Sales, Purchasing and Alerts.
 */
@Service
public class CatalogContextFacade {

    private final ProductRepository products;

    public CatalogContextFacade(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> findProduct(
            Long businessId,
            Long productId) {

        return products
                .findByIdAndBusinessIdAndActiveTrue(
                        productId,
                        businessId
                )
                .map(this::toSnapshot);
    }

    /**
     * Finds the product and locks the row
     * until the transaction ends.
     */
    @Transactional
    public Optional<ProductSnapshot> findProductForUpdate(
            Long businessId,
            Long productId) {

        return products
                .findForUpdate(productId, businessId)
                .map(this::toSnapshot);
    }

    /**
     * Updates the registered stock.
     * Returns empty if the product does not exist.
     */
    @Transactional
    public Optional<ProductSnapshot> applyStock(
            Long businessId,
            Long productId,
            int newStock) {

        return products
                .findByIdAndBusinessIdAndActiveTrue(
                        productId,
                        businessId
                )
                .map(p -> {
                    p.applyStock(newStock);
                    return toSnapshot(products.save(p));
                });
    }

    /**
     * Unit weight in kilograms, used by Devices to turn
     * a weight reading into units.
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> findUnitWeightKg(
            Long businessId,
            Long productId) {

        return products
                .findByIdAndBusinessIdAndActiveTrue(
                        productId,
                        businessId
                )
                .map(Product::getUnitWeightKg);
    }

    private ProductSnapshot toSnapshot(Product p) {

        return new ProductSnapshot(
                p.getId(),
                p.getBusinessId(),
                p.getName().value(),
                p.getRegisteredStock(),
                p.getMinimumThreshold(),
                p.getSalePrice() == null
                        ? BigDecimal.ZERO
                        : p.getSalePrice().value()
        );
    }
}
