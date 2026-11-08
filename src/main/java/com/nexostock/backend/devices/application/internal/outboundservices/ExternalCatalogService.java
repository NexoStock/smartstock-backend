package com.nexostock.backend.devices.application.internal.outboundservices;

import com.nexostock.backend.catalog.interfaces.acl.CatalogContextFacade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Devices talks to Catalog only through its facade (ACL).
 * Devices solo habla con Catalog por su fachada.
 */
@Service
public class ExternalCatalogService {

    /** Product data that the sensor screen needs. */
    public record LinkedProduct(Long id, String name, BigDecimal unitWeightKg) {

        /** Whole units on the scale; null when the unit weight is unknown. */
        public Integer unitsFor(BigDecimal weightKg) {
            if (weightKg == null
                    || unitWeightKg == null
                    || unitWeightKg.signum() <= 0) {
                return null;
            }
            return weightKg
                    .divide(unitWeightKg, 0, RoundingMode.FLOOR)
                    .intValue();
        }
    }

    private final CatalogContextFacade catalog;

    public ExternalCatalogService(CatalogContextFacade catalog) {
        this.catalog = catalog;
    }

    public Optional<LinkedProduct> findProduct(Long businessId, Long productId) {
        if (productId == null) {
            return Optional.empty();
        }
        return catalog.findProduct(businessId, productId)
                .map(p -> new LinkedProduct(
                        p.id(),
                        p.name(),
                        catalog.findUnitWeightKg(businessId, productId)
                                .orElse(null)));
    }
}
