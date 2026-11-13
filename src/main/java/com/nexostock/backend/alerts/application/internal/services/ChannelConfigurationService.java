package com.nexostock.backend.alerts.application.internal.services;

import com.nexostock.backend.alerts.domain.model.aggregates.NotificationChannel;
import com.nexostock.backend.alerts.domain.model.commands.UpdateChannelsCommand;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.NotificationChannelRepository;
import com.nexostock.backend.iam.interfaces.acl.IamContextFacade;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChannelConfigurationService {

    private final NotificationChannelRepository channels;
    private final IamContextFacade iam;

    public ChannelConfigurationService(
            NotificationChannelRepository channels,
            IamContextFacade iam) {
        this.channels = channels;
        this.iam = iam;
    }

    public Result<List<NotificationChannel>, ApplicationError> findOrCreate(
            Long businessId) {

        var contact = iam.findContact(businessId);

        if (contact.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("BUSINESS_CONTACT_NOT_FOUND"));
        }

        var owner = contact.get();

        if (channels.findByBusinessIdAndChannel(
                businessId, "email").isEmpty()) {
            channels.save(new NotificationChannel(
                    businessId, "email", owner.email(), true));
        }

        if (owner.phone() != null && !owner.phone().isBlank()
                && channels.findByBusinessIdAndChannel(
                businessId, "whatsapp").isEmpty()) {
            channels.save(new NotificationChannel(
                    businessId, "whatsapp", owner.phone(), true));
        }

        return Result.success(
                channels.findAllByBusinessIdOrderById(businessId));
    }

    public Result<List<NotificationChannel>, ApplicationError> update(
            UpdateChannelsCommand command) {

        var result = findOrCreate(command.businessId());

        if (result.isFailure()) {
            return Result.failure(result.getError().orElseThrow());
        }

        var existing = result.getValue().orElseThrow();
        var requested = new HashMap<Long, Boolean>();

        for (var change : command.changes()) {
            if (requested.putIfAbsent(
                    change.id(), change.active()) != null) {
                return Result.failure(ApplicationError.businessRuleViolation(
                        "DUPLICATE_CHANNEL_CHANGE"));
            }

            boolean belongsToBusiness = existing.stream()
                    .anyMatch(channel -> channel.getId().equals(change.id()));

            if (!belongsToBusiness) {
                return Result.failure(
                        ApplicationError.notFound("CHANNEL_NOT_FOUND"));
            }
        }

        boolean anyActive = existing.stream().anyMatch(channel ->
                requested.getOrDefault(
                        channel.getId(), channel.isActive()));

        if (!anyActive) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "AT_LEAST_ONE_CHANNEL"));
        }

        for (var channel : existing) {
            if (requested.containsKey(channel.getId())) {
                channel.changeActive(requested.get(channel.getId()));
            }
        }

        return Result.success(channels.saveAll(existing));
    }
}