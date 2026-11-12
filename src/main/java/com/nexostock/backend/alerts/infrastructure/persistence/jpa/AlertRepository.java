package com.nexostock.backend.alerts.infrastructure.persistence.jpa;

import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.domain.model.aggregates.AlertType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findByBusinessIdAndProductIdAndTypeAndActiveTrue(
            Long businessId,
            Long productId,
            AlertType type);

    List<Alert> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);
}