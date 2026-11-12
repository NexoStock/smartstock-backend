package com.nexostock.backend.alerts.infrastructure.notifications;

import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.OutboundMessageRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.notifications.enabled",
        havingValue = "true")
public class NotificationDispatcher {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationDispatcher.class);

    private final OutboundMessageRepository queue;
    private final List<NotificationSender> senders;

    public NotificationDispatcher(
            OutboundMessageRepository queue,
            List<NotificationSender> senders) {
        this.queue = queue;
        this.senders = senders;
    }

    @Scheduled(fixedDelayString = "${app.notifications.interval-ms:10000}")
    public void dispatchPending() {
        Instant now = Instant.now();
        queue.releaseStale(now.minusSeconds(300), now);

        var candidates =
                queue.findTop20ByStatusAndNextAttemptAtLessThanEqualOrderById(
                        "PENDING", now);

        for (OutboundMessage candidate : candidates) {
            if (queue.claim(candidate.getId(), Instant.now()) != 1) {
                continue;
            }

            OutboundMessage message =
                    queue.findById(candidate.getId()).orElseThrow();

            try {
                var sender = senders.stream()
                        .filter(item -> item.supports(message.getChannel()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "No sender for " + message.getChannel()));

                sender.send(message);
                message.markSent();

                log.info("Notification {} accepted by provider via {}",
                        message.getId(), message.getChannel());
            } catch (RuntimeException exception) {
                message.markFailedAttempt();

                log.warn("Notification {} failed; attempts: {}",
                        message.getId(), message.getAttempts(), exception);
            }

            queue.save(message);
        }
    }
}