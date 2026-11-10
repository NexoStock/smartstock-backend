package com.nexostock.backend.alerts.infrastructure.persistence.jpa;

import com.nexostock.backend.alerts.domain.model.aggregates.NotificationChannel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationChannelRepository
        extends JpaRepository<NotificationChannel, Long> {

    List<NotificationChannel> findAllByBusinessIdOrderById(
            Long businessId);

    List<NotificationChannel> findAllByBusinessIdAndActiveTrueOrderById(
            Long businessId);

    Optional<NotificationChannel> findByIdAndBusinessId(
            Long id, Long businessId);

    Optional<NotificationChannel> findByBusinessIdAndChannel(
            Long businessId, String channel);
}