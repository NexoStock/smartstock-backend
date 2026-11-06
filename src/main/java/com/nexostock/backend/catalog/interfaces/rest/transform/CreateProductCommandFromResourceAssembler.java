
package com.nexostock.backend.catalog.interfaces.rest.transform;

import com.nexostock.backend.catalog.domain.model.commands.CreateProductCommand;
import com.nexostock.backend.catalog.domain.model.valueobjects.Money;
import com.nexostock.backend.catalog.domain.model.valueobjects.ProductName;
import com.nexostock.backend.catalog.domain.model.valueobjects.Sku;
import com.nexostock.backend.catalog.interfaces.rest.resources.SaveProductResource;

/**
 * Converts a product request into a create command.
 * Convierte la solicitud en un comando para crear productos.
 */
public class CreateProductCommandFromResourceAssembler {

    private CreateProductCommandFromResourceAssembler() {
    }

    public static CreateProductCommand toCommand(
            Long businessId,
            SaveProductResource r) {

        return new CreateProductCommand(
                businessId,
                new ProductName(r.name()),
                r.sku() == null || r.sku().isBlank()
                        ? Sku.generate()
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
                r.initialStock() == null
                        ? 0
                        : r.initialStock(),
                r.minimumThreshold(),
                r.maximumCapacity() == null
                        ? 100
                        : r.maximumCapacity()
        );
    }
}
