package com.nexostock.backend.alerts.domain.model.commands;

import java.util.List;
import java.util.Objects;

public record UpdateChannelsCommand(
        Long businessId,
        List<Change> changes) {

    public record Change(Long id, boolean active) {

        public Change {
            Objects.requireNonNull(id, "channel id is required");
        }
    }

    public UpdateChannelsCommand {
        Objects.requireNonNull(businessId, "businessId is required");
        Objects.requireNonNull(changes, "changes are required");
        changes = List.copyOf(changes);
    }
}