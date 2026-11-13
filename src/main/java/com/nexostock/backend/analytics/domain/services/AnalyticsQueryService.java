package com.nexostock.backend.analytics.domain.services;

import com.nexostock.backend.analytics.domain.model.queries.GetDashboardQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetInventoryComparisonQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetProductSensorDetailQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetReportQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetStockMovementsQuery;
import com.nexostock.backend.analytics.domain.model.readmodels.DashboardSummary;
import com.nexostock.backend.analytics.domain.model.readmodels.InventoryComparison;
import com.nexostock.backend.analytics.domain.model.readmodels.ProductSensorDetail;
import com.nexostock.backend.analytics.domain.model.readmodels.Report;
import com.nexostock.backend.analytics.domain.model.readmodels.StockMovementRow;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

import java.util.List;

/**
 * Read-only queries of Analytics and Reporting and of Inventory Monitoring.
 * They read the data of the other contexts and never change it.
 */
public interface AnalyticsQueryService {

    DashboardSummary handle(GetDashboardQuery query);

    Result<Report, ApplicationError> handle(GetReportQuery query);

    InventoryComparison handle(GetInventoryComparisonQuery query);

    Result<ProductSensorDetail, ApplicationError> handle(GetProductSensorDetailQuery query);

    Result<List<StockMovementRow>, ApplicationError> handle(GetStockMovementsQuery query);
}
