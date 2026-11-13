package com.nexostock.backend.sales.infrastructure.persistence.jpa;

import com.nexostock.backend.sales.domain.model.aggregates.Sale;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);

    Optional<Sale> findByBusinessIdAndRequestKey(
            Long businessId, String requestKey);

    Optional<Sale> findByBusinessIdAndCode(
            Long businessId, String code);

    @Query("""
        select s from Sale s
        where s.businessId = :businessId
          and (:fromDate is null or s.createdAt >= :fromDate)
          and (:toDate is null or s.createdAt < :toDate)
        order by s.createdAt desc
        """)
    List<Sale> findByBusinessAndDates(
            @Param("businessId") Long businessId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);
}