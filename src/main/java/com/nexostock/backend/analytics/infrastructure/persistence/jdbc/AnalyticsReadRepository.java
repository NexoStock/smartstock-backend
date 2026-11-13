package com.nexostock.backend.analytics.infrastructure.persistence.jdbc;

import com.nexostock.backend.analytics.domain.model.readmodels.DashboardSummary.Activity;
import com.nexostock.backend.analytics.domain.model.readmodels.ProductSensorDetail.Reading;
import com.nexostock.backend.analytics.domain.model.readmodels.ProductStock;
import com.nexostock.backend.analytics.domain.model.readmodels.StockMovementRow;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Read model of Analytics and Reporting. It only runs SELECT statements over the tables
 * of Catalog, Devices, Inventory, Sales, Purchasing and Alerts; it never writes.
 * Dates are stored in UTC (hibernate.jdbc.time_zone=UTC).
 */
@Repository
public class AnalyticsReadRepository {

    /** Purchase with the total of its lines. */
    public record PurchaseRow(Long id, String status, BigDecimal total, Instant createdAt) {
    }

    private static final String PRODUCT_STOCK_SQL = """
            SELECT p.id, p.name, p.registered_stock, p.minimum_threshold, p.unit_weight_kg,
                   s.id AS sensor_id, s.device_code, s.last_weight_kg, s.last_reading_at
            FROM products p
            LEFT JOIN sensors s ON s.product_id = p.id AND s.business_id = p.business_id
            WHERE p.business_id = :businessId AND p.active = TRUE
            """;

    private final JdbcClient jdbc;

    public AnalyticsReadRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProductStock> findProductStock(Long businessId) {
        return jdbc.sql(PRODUCT_STOCK_SQL + " ORDER BY p.name")
                .param("businessId", businessId)
                .query(this::toProductStock)
                .list();
    }

    public Optional<ProductStock> findProductStock(Long businessId, Long productId) {
        return jdbc.sql(PRODUCT_STOCK_SQL + " AND p.id = :productId")
                .param("businessId", businessId)
                .param("productId", productId)
                .query(this::toProductStock)
                .optional();
    }

    public List<Reading> findLatestReadings(Long sensorId, BigDecimal unitWeightKg, int limit) {
        return jdbc.sql("""
                        SELECT id, measured_at, weight_kg FROM sensor_readings
                        WHERE sensor_id = :sensorId
                        ORDER BY measured_at DESC LIMIT :limit
                        """)
                .param("sensorId", sensorId)
                .param("limit", limit)
                .query((rs, n) -> new Reading(
                        rs.getLong("id"),
                        instant(rs, "measured_at"),
                        rs.getBigDecimal("weight_kg"),
                        ProductStock.toUnits(rs.getBigDecimal("weight_kg"), unitWeightKg)))
                .list();
    }

    public List<Activity> findSales(Long businessId, Instant from, Instant to) {
        return jdbc.sql("""
                        SELECT code, total, created_at FROM sales
                        WHERE business_id = :businessId AND created_at >= :from AND created_at < :to
                        ORDER BY created_at DESC
                        """)
                .param("businessId", businessId)
                .param("from", utc(from))
                .param("to", utc(to))
                .query((rs, n) -> new Activity(
                        "SALE", rs.getString("code"), rs.getBigDecimal("total"), instant(rs, "created_at")))
                .list();
    }

    public List<PurchaseRow> findPurchases(Long businessId, Instant from, Instant to) {
        return jdbc.sql("""
                        SELECT p.id, p.status, p.created_at,
                               COALESCE(SUM(i.quantity * i.unit_price), 0) AS total
                        FROM purchases p
                        LEFT JOIN purchase_items i ON i.purchase_id = p.id
                        WHERE p.business_id = :businessId AND p.created_at >= :from AND p.created_at < :to
                        GROUP BY p.id, p.status, p.created_at
                        ORDER BY p.created_at DESC
                        """)
                .param("businessId", businessId)
                .param("from", utc(from))
                .param("to", utc(to))
                .query((rs, n) -> new PurchaseRow(
                        rs.getLong("id"),
                        rs.getString("status"),
                        rs.getBigDecimal("total"),
                        instant(rs, "created_at")))
                .list();
    }

    public long countActiveLowStockAlerts(Long businessId) {
        return jdbc.sql("""
                        SELECT COUNT(*) FROM alerts
                        WHERE business_id = :businessId AND active = TRUE AND type = 'LOW_STOCK'
                        """)
                .param("businessId", businessId)
                .query(Long.class)
                .single();
    }

    public List<StockMovementRow> findMovements(Long businessId, Instant from, Instant to) {
        return jdbc.sql("""
                        SELECT m.id, m.product_id, p.name, m.type, m.quantity, m.reference, m.created_at
                        FROM movements m
                        JOIN products p ON p.id = m.product_id
                        WHERE m.business_id = :businessId AND m.created_at >= :from AND m.created_at < :to
                        ORDER BY m.created_at DESC, m.id DESC
                        """)
                .param("businessId", businessId)
                .param("from", utc(from))
                .param("to", utc(to))
                .query((rs, n) -> {
                    int quantity = rs.getInt("quantity");
                    return new StockMovementRow(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getString("name"),
                            quantity >= 0 ? "IN" : "OUT",
                            source(rs.getString("type")),
                            rs.getString("reference"),
                            Math.abs(quantity),
                            instant(rs, "created_at"));
                })
                .list();
    }

    private ProductStock toProductStock(ResultSet rs, int rowNum) throws SQLException {
        return new ProductStock(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getInt("registered_stock"),
                rs.getInt("minimum_threshold"),
                rs.getBigDecimal("unit_weight_kg"),
                rs.getObject("sensor_id", Long.class),
                rs.getString("device_code"),
                rs.getBigDecimal("last_weight_kg"),
                instant(rs, "last_reading_at"));
    }

    /** Inventory MovementType to the source names of the Angular contract. */
    private static String source(String type) {
        return switch (type) {
            case "SALE" -> "SALE";
            case "PURCHASE_RECEIPT" -> "PURCHASE";
            default -> "ADJUSTMENT";
        };
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        LocalDateTime value = rs.getObject(column, LocalDateTime.class);
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }

    private static LocalDateTime utc(Instant value) {
        return LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
