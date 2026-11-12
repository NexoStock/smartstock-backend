package com.nexostock.backend.alerts.infrastructure.persistence.jpa;

import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OutboundMessageRepository
        extends JpaRepository<OutboundMessage, Long> {

    List<OutboundMessage>
    findTop20ByStatusAndNextAttemptAtLessThanEqualOrderById(
            String status, Instant now);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
        update OutboundMessage m
        set m.status = 'PROCESSING', m.updatedAt = :now
        where m.id = :id and m.status = 'PENDING'
        """)
    int claim(@Param("id") Long id, @Param("now") Instant now);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
        update OutboundMessage m
        set m.status = 'PENDING', m.updatedAt = :now
        where m.status = 'PROCESSING' and m.updatedAt < :limit
        """)
    int releaseStale(
            @Param("limit") Instant limit,
            @Param("now") Instant now);
}