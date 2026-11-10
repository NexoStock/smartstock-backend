
package com.nexostock.backend.purchasing.infrastructure.persistence.jpa;

import com.nexostock.backend.purchasing.domain.model.aggregates.Purchase;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /**
     * List all purchases belonging to a business.
     * The service must map purchase items inside
     * an active transaction.
     */
    List<Purchase> findAllByBusinessIdOrderByIdDesc(
            Long businessId
    );

    /**
     * Get a purchase with its items,
     * restricted to the corresponding business.
     */
    @EntityGraph(attributePaths = {"items"})
    Optional<Purchase> findByBusinessIdAndId(
            Long businessId,
            Long purchaseId
    );

    /**
     * Lock the purchase row before receiving it.
     *
     * The command service must call this method
     * inside a transaction to prevent concurrent
     * receipts from processing the same purchase.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM Purchase p
            WHERE p.businessId = :businessId
              AND p.id = :purchaseId
            """)
    Optional<Purchase> findForReceiving(
            @Param("businessId") Long businessId,
            @Param("purchaseId") Long purchaseId
    );
}
