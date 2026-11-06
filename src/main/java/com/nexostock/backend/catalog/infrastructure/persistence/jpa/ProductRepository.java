
package com.nexostock.backend.catalog.infrastructure.persistence.jpa;

import com.nexostock.backend.catalog.domain.model.aggregates.Product;
import com.nexostock.backend.catalog.domain.model.valueobjects.Sku;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByBusinessIdAndActiveTrue(
            Long businessId
    );

    Optional<Product> findByIdAndBusinessIdAndActiveTrue(
            Long id,
            Long businessId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from Product p
            where p.id = :id
            and p.businessId = :businessId
            and p.active = true
            """)
    Optional<Product> findForUpdate(
            @Param("id") Long id,
            @Param("businessId") Long businessId
    );

    boolean existsByBusinessIdAndSku(
            Long businessId,
            Sku sku
    );

    boolean existsByBusinessIdAndSkuAndIdNot(
            Long businessId,
            Sku sku,
            Long id
    );
}
