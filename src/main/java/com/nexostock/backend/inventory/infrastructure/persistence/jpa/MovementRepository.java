
package com.nexostock.backend.inventory.infrastructure.persistence.jpa;

import com.nexostock.backend.inventory.domain.model.aggregates.Movement;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    List<Movement> findAllByBusinessIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long businessId,
            Instant from,
            Instant to
    );

    List<Movement> findAllByBusinessIdAndProductIdOrderByCreatedAtDesc(
            Long businessId,
            Long productId
    );
}