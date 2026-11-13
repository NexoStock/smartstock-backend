package com.nexostock.backend.analytics.application.internal.queryservices;

import com.nexostock.backend.analytics.domain.model.queries.GetDashboardQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetInventoryComparisonQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetProductSensorDetailQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetReportQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetStockMovementsQuery;
import com.nexostock.backend.analytics.domain.model.readmodels.ComparisonRow;
import com.nexostock.backend.analytics.domain.model.readmodels.DashboardSummary;
import com.nexostock.backend.analytics.domain.model.readmodels.DashboardSummary.Activity;
import com.nexostock.backend.analytics.domain.model.readmodels.DashboardSummary.StockSummary;
import com.nexostock.backend.analytics.domain.model.readmodels.DashboardSummary.Totals;
import com.nexostock.backend.analytics.domain.model.readmodels.InventoryComparison;
import com.nexostock.backend.analytics.domain.model.readmodels.ProductSensorDetail;
import com.nexostock.backend.analytics.domain.model.readmodels.ProductStock;
import com.nexostock.backend.analytics.domain.model.readmodels.Report;
import com.nexostock.backend.analytics.domain.model.readmodels.StockMovementRow;
import com.nexostock.backend.analytics.domain.services.AnalyticsQueryService;
import com.nexostock.backend.analytics.infrastructure.persistence.jdbc.AnalyticsReadRepository;
import com.nexostock.backend.analytics.infrastructure.persistence.jdbc.AnalyticsReadRepository.PurchaseRow;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Builds the dashboard, the reports and the inventory comparison from the read model.
 * "Today" and the report dates follow the time zone of the businesses (Lima).
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");
    private static final Instant MIN = Instant.parse("2000-01-01T00:00:00Z");
    private static final Instant MAX = Instant.parse("2100-01-01T00:00:00Z");
    private static final int RECENT_ACTIVITY = 5;
    private static final int LATEST_READINGS = 5;

    private final AnalyticsReadRepository readModel;

    public AnalyticsQueryServiceImpl(AnalyticsReadRepository readModel) {
        this.readModel = readModel;
    }

    @Override
    public DashboardSummary handle(GetDashboardQuery query) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        Instant from = startOf(today);
        Instant to = startOf(today.plusDays(1));

        List<Activity> sales = readModel.findSales(query.businessId(), from, to);
        List<Activity> purchases = readModel.findPurchases(query.businessId(), from, to).stream()
                .filter(p -> !"CANCELLED".equals(p.status()))
                .map(p -> new Activity("PURCHASE", String.valueOf(p.id()), p.total(), p.createdAt()))
                .toList();

        List<Activity> recent = Stream.concat(sales.stream(), purchases.stream())
                .sorted(Comparator.comparing(Activity::date).reversed())
                .limit(RECENT_ACTIVITY)
                .toList();

        List<StockSummary> stock = readModel.findProductStock(query.businessId()).stream()
                .map(p -> new StockSummary(
                        p.productId(), p.name(), p.referenceUnits(now), p.level(now), p.sensorStatus(now)))
                .toList();

        return new DashboardSummary(
                totals(sales),
                totals(purchases),
                readModel.countActiveLowStockAlerts(query.businessId()),
                recent,
                stock);
    }

    @Override
    public Result<Report, ApplicationError> handle(GetReportQuery query) {
        if (invalidRange(query.from(), query.to())) {
            return Result.failure(ApplicationError.validationError("INVALID_DATE_RANGE"));
        }
        Instant from = query.from() == null ? MIN : startOf(query.from());
        Instant to = query.to() == null ? MAX : startOf(query.to().plusDays(1));

        BigDecimal salesTotal = sum(readModel.findSales(query.businessId(), from, to).stream()
                .map(Activity::total));
        List<PurchaseRow> purchases = readModel.findPurchases(query.businessId(), from, to).stream()
                .filter(p -> !"CANCELLED".equals(p.status()))
                .toList();

        return Result.success(new Report(
                salesTotal,
                sum(purchases.stream().map(PurchaseRow::total)),
                readModel.findMovements(query.businessId(), from, to),
                purchases.stream()
                        .filter(p -> "PENDING".equals(p.status()))
                        .map(p -> String.valueOf(p.id()))
                        .toList()));
    }

    @Override
    public InventoryComparison handle(GetInventoryComparisonQuery query) {
        Instant now = Instant.now();
        List<ComparisonRow> items = readModel.findProductStock(query.businessId()).stream()
                .map(p -> p.compare(now))
                .toList();
        long discrepancies = items.stream()
                .filter(i -> ComparisonRow.DISCREPANCY.equals(i.result()))
                .count();
        return new InventoryComparison(items, discrepancies);
    }

    @Override
    public Result<ProductSensorDetail, ApplicationError> handle(GetProductSensorDetailQuery query) {
        Instant now = Instant.now();
        return readModel.findProductStock(query.businessId(), query.productId())
                .<Result<ProductSensorDetail, ApplicationError>>map(p -> Result.success(detail(p, now)))
                .orElseGet(() -> Result.failure(ApplicationError.notFound("PRODUCT_NOT_FOUND")));
    }

    @Override
    public Result<List<StockMovementRow>, ApplicationError> handle(GetStockMovementsQuery query) {
        if (invalidRange(query.from(), query.to())) {
            return Result.failure(ApplicationError.validationError("INVALID_DATE_RANGE"));
        }
        Instant from = query.from() == null ? MIN : startOf(query.from());
        Instant to = query.to() == null ? MAX : startOf(query.to().plusDays(1));
        return Result.success(readModel.findMovements(query.businessId(), from, to));
    }

    private ProductSensorDetail detail(ProductStock p, Instant now) {
        if (p.sensorId() == null) {
            return new ProductSensorDetail(null, List.of());
        }
        var sensor = new ProductSensorDetail.SensorInfo(
                p.sensorId(),
                p.sensorCode(),
                p.sensorStatus(now),
                p.lastWeightKg(),
                ProductStock.toUnits(p.lastWeightKg(), p.unitWeightKg()),
                p.lastReadingAt());
        return new ProductSensorDetail(
                sensor,
                readModel.findLatestReadings(p.sensorId(), p.unitWeightKg(), LATEST_READINGS));
    }

    private static Totals totals(List<Activity> activity) {
        return new Totals(sum(activity.stream().map(Activity::total)), activity.size());
    }

    private static BigDecimal sum(Stream<BigDecimal> values) {
        return values.filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean invalidRange(LocalDate from, LocalDate to) {
        return from != null && to != null && from.isAfter(to);
    }

    private static Instant startOf(LocalDate day) {
        return day.atStartOfDay(BUSINESS_ZONE).toInstant();
    }
}
