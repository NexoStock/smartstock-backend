package com.nexostock.backend.devices.infrastructure.persistence.jpa;

import com.nexostock.backend.devices.domain.model.aggregates.SensorReading;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorReadingRepository
        extends JpaRepository<SensorReading, Long> {

    boolean existsBySensorIdAndMeasuredAt(Long sensorId, Instant measuredAt);

    List<SensorReading> findTop10BySensorIdOrderByMeasuredAtDesc(Long sensorId);

    void deleteAllBySensorId(Long sensorId);
}
