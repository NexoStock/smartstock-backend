package com.nexostock.backend.devices.infrastructure.persistence.jpa;

import com.nexostock.backend.devices.domain.model.aggregates.Sensor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findByDeviceCode(String deviceCode);

    Optional<Sensor> findByIdAndBusinessId(Long id, Long businessId);

    List<Sensor> findAllByBusinessIdOrderByDeviceCodeAsc(Long businessId);

    boolean existsByBusinessIdAndProductId(Long businessId, Long productId);
}
