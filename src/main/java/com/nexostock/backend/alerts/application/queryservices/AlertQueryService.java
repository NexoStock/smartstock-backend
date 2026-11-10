package com.nexostock.backend.alerts.application.queryservices;

import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.domain.model.aggregates.NotificationChannel;
import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import com.nexostock.backend.alerts.domain.model.queries.GetAlertsQuery;
import com.nexostock.backend.alerts.domain.model.queries.GetChannelsQuery;
import com.nexostock.backend.alerts.domain.model.queries.GetNotificationsQuery;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;

public interface AlertQueryService {

    Result<List<Alert>, ApplicationError> handle(GetAlertsQuery query);

    Result<List<NotificationChannel>, ApplicationError> handle(
            GetChannelsQuery query);

    Result<List<OutboundMessage>, ApplicationError> handle(
            GetNotificationsQuery query);
}