package com.nexostock.backend.analytics.interfaces.rest.transform;

import com.nexostock.backend.analytics.domain.model.readmodels.DashboardSummary;
import com.nexostock.backend.analytics.domain.model.readmodels.InventoryComparison;
import com.nexostock.backend.analytics.domain.model.readmodels.ProductSensorDetail;
import com.nexostock.backend.analytics.domain.model.readmodels.Report;
import com.nexostock.backend.analytics.domain.model.readmodels.StockMovementRow;
import com.nexostock.backend.analytics.interfaces.rest.resources.DashboardResource;
import com.nexostock.backend.analytics.interfaces.rest.resources.InventoryComparisonResource;
import com.nexostock.backend.analytics.interfaces.rest.resources.ProductSensorDetailResource;
import com.nexostock.backend.analytics.interfaces.rest.resources.ReportResource;
import com.nexostock.backend.analytics.interfaces.rest.resources.StockMovementResource;

import java.time.LocalDate;

/**
 * Turns the read models of Analytics into the resources of the REST API.
 */
public final class AnalyticsResourceAssembler {

    private AnalyticsResourceAssembler() {
    }

    public static DashboardResource toResource(DashboardSummary s) {
        return new DashboardResource(
                new DashboardResource.TotalsResource(s.salesToday().total(), s.salesToday().count()),
                new DashboardResource.TotalsResource(s.purchasesToday().total(), s.purchasesToday().count()),
                s.lowStockAlerts(),
                s.recentActivity().stream()
                        .map(a -> new DashboardResource.ActivityResource(a.type(), a.id(), a.total(), a.date()))
                        .toList(),
                s.stock().stream()
                        .map(p -> new DashboardResource.StockSummaryResource(
                                p.productId(), p.name(), p.units(), p.level(), p.sensor()))
                        .toList());
    }

    public static ReportResource toResource(Report r, LocalDate from, LocalDate to) {
        return new ReportResource(
                from,
                to,
                r.salesTotal(),
                r.purchasesTotal(),
                r.movements().stream().map(AnalyticsResourceAssembler::toResource).toList(),
                r.pendingPurchases());
    }

    public static StockMovementResource toResource(StockMovementRow m) {
        return new StockMovementResource(
                m.id(), m.productId(), m.productName(), m.direction(), m.source(), m.sourceId(),
                m.quantity(), m.date());
    }

    public static InventoryComparisonResource toResource(InventoryComparison c) {
        return new InventoryComparisonResource(
                c.items().stream()
                        .map(i -> new InventoryComparisonResource.RowResource(
                                i.productId(), i.productName(), i.registeredStock(), i.weightKg(),
                                i.physicalUnits(), i.differencePercent(), i.result()))
                        .toList(),
                c.discrepancies(),
                null);
    }

    public static ProductSensorDetailResource toResource(ProductSensorDetail d) {
        var sensor = d.sensor() == null ? null : new ProductSensorDetailResource.SensorResource(
                d.sensor().id(), d.sensor().code(), d.sensor().status(), d.sensor().weightKg(),
                d.sensor().units(), d.sensor().lastReadingAt());
        return new ProductSensorDetailResource(
                sensor,
                d.readings().stream()
                        .map(r -> new ProductSensorDetailResource.ReadingResource(
                                r.id(), r.measuredAt(), r.weightKg(), r.units()))
                        .toList());
    }
}
