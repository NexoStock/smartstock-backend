package com.nexostock.backend.sales.infrastructure.persistence.jpa;

import com.nexostock.backend.sales.domain.model.aggregates.Sale;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);

    Optional<Sale> findByBusinessIdAndRequestKey(
            Long businessId, String requestKey);

    Optional<Sale> findByBusinessIdAndCode(
            Long businessId, String code);
}