
package com.nexostock.backend.purchasing.infrastructure.persistence.jpa;

import com.nexostock.backend.purchasing.domain.model.aggregates.Supplier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * Find all active suppliers belonging to a business.
     */
    List<Supplier> findAllByBusinessIdAndActiveTrueOrderByNameAsc(
            Long businessId
    );

    /**
     * Find a supplier by business and supplier ID.
     */
    Optional<Supplier> findByBusinessIdAndId(
            Long businessId,
            Long supplierId
    );

    /**
     * Find an active supplier by business and supplier ID.
     */
    Optional<Supplier> findByBusinessIdAndIdAndActiveTrue(
            Long businessId,
            Long supplierId
    );

    /**
     * Check whether a supplier tax ID already exists
     * in the same business.
     */
    boolean existsByBusinessIdAndTaxId(
            Long businessId,
            String taxId
    );

    /**
     * Check for a duplicated supplier tax ID,
     * excluding the supplier currently being updated.
     */
    boolean existsByBusinessIdAndTaxIdAndIdNot(
            Long businessId,
            String taxId,
            Long supplierId
    );
}
