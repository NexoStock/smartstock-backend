
package com.nexostock.backend.catalog.interfaces.rest.transform;

import com.nexostock.backend.catalog.domain.model.commands.UpdateProductCommand;
import com.nexostock.backend.catalog.domain.model.valueobjects.Money;
import com.nexostock.backend.catalog.domain.model.valueobjects.ProductName;
import com.nexostock.backend.catalog.domain.model.valueobjects.Sku;
import com.nexostock.backend.catalog.interfaces.rest.resources.SaveProductResource;

/**
 * Converts a product request into an update command.
 * Convierte la solicitud en un comando para actualizar productos.
 */
public class UpdateProductCommandFromResourceAssembler {

    private UpdateProductCommandFromResourceAssembler() {
    }

    public static UpdateProductCommand toCommand(
            Long businessId,
            Long productId,
            SaveProductResource r) {

        return new UpdateProductCommand(
                businessId,
                productId,
                new ProductName(r.name()),
                r.sku() == null || r.sku().isBlank()
                        ? null
                        : new Sku(r.sku()),
                r.category(),
                r.unitWeightKg(),
                r.salePrice() == null
                        ? Money.zero()
                        : new Money(r.salePrice()),
                r.purchaseCost() == null
                        ? Money.zero()
                        : new Money(r.purchaseCost()),
                r.usualSupplier() == null
                        ? ""
                        : r.usualSupplier(),
                r.minimumThreshold(),
                r.maximumCapacity() == null
                        ? 100
                        : r.maximumCapacity()
        );
    }
}
