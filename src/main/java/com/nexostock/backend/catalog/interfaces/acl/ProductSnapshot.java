
package com.nexostock.backend.catalog.interfaces.acl;

import java.math.BigDecimal;

/**
 * Read-only view of a product for other contexts.
 * Vista de solo lectura para otros módulos.
 */
public record ProductSnapshot(
        Long id,
        Long businessId,
        String name,
        int registeredStock,
        int minimumThreshold,
        BigDecimal salePrice
) {
}
