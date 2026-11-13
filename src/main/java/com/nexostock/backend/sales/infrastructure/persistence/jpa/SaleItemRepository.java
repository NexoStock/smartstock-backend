package com.nexostock.backend.sales.infrastructure.persistence.jpa;

import com.nexostock.backend.sales.domain.model.aggregates.SaleItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findAllBySaleId(Long saleId);
}