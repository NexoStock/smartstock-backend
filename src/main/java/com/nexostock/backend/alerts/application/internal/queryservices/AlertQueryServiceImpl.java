package com.nexostock.backend.alerts.application.internal.queryservices;

import com.nexostock.backend.alerts.application.queryservices.AlertQueryService;
import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.domain.model.queries.GetAlertsQuery;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alerts;

    public AlertQueryServiceImpl(AlertRepository alerts) {
        this.alerts = alerts;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<Alert>, ApplicationError> handle(GetAlertsQuery query) {
        return Result.success(
                alerts.findAllByBusinessIdOrderByCreatedAtDesc(
                        query.businessId()));
    }
}