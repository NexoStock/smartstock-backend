package com.nexostock.backend.alerts.application.internal.queryservices;

import com.nexostock.backend.alerts.application.internal.services.ChannelConfigurationService;
import com.nexostock.backend.alerts.application.queryservices.AlertQueryService;
import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.domain.model.aggregates.NotificationChannel;
import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import com.nexostock.backend.alerts.domain.model.queries.GetAlertsQuery;
import com.nexostock.backend.alerts.domain.model.queries.GetChannelsQuery;
import com.nexostock.backend.alerts.domain.model.queries.GetNotificationsQuery;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.OutboundMessageRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alerts;
    private final ChannelConfigurationService channels;
    private final OutboundMessageRepository queue;

    public AlertQueryServiceImpl(
            AlertRepository alerts,
            ChannelConfigurationService channels,
            OutboundMessageRepository queue) {
        this.alerts = alerts;
        this.channels = channels;
        this.queue = queue;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<Alert>, ApplicationError> handle(GetAlertsQuery query) {
        return Result.success(
                alerts.findAllByBusinessIdOrderByCreatedAtDesc(
                        query.businessId()));
    }

    @Override
    @Transactional
    public Result<List<NotificationChannel>, ApplicationError> handle(
            GetChannelsQuery query) {
        return channels.findOrCreate(query.businessId());
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<OutboundMessage>, ApplicationError> handle(
            GetNotificationsQuery query) {
        return Result.success(
                queue.findAllByBusinessIdOrderByCreatedAtDesc(
                        query.businessId()));
    }
}