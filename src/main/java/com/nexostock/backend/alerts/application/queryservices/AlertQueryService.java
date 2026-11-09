package com.nexostock.backend.alerts.application.queryservices;

import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.domain.model.queries.GetAlertsQuery;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;

public interface AlertQueryService {

    Result<List<Alert>, ApplicationError> handle(GetAlertsQuery query);
}