package com.nexostock.backend.analytics.domain.model.readmodels;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/**
 * Read model of a product with the sensor linked to it.
 * Holds the rules that turn a weight into stock: R11, R12, R18 and R20.
 */
public record ProductStock(
        Long productId,
        String name,
        int registeredStock,
        int minimumThreshold,
        BigDecimal unitWeightKg,
        Long sensorId,
        String sensorCode,
        BigDecimal lastWeightKg,
        Instant lastReadingAt) {

    /** R11: a linked sensor without readings in the last 5 minutes is disconnected. */
    public static final Duration ONLINE_WINDOW = Duration.ofMinutes(5);

    /** R20: a difference above 10 percent is a discrepancy. */
    public static final BigDecimal DISCREPANCY_PERCENT = BigDecimal.TEN;

    public String sensorStatus(Instant now) {
        if (sensorId == null) return "none";
        if (lastReadingAt != null && !lastReadingAt.isBefore(now.minus(ONLINE_WINDOW))) return "online";
        return "disconnected";
    }

    public boolean sensorOnline(Instant now) {
        return "online".equals(sensorStatus(now)) && lastWeightKg != null;
    }

    /** R12: weight to whole units with the unit weight of the product. */
    public static int toUnits(BigDecimal weightKg, BigDecimal unitWeightKg) {
        if (weightKg == null || unitWeightKg == null || unitWeightKg.signum() <= 0) return 0;
        return weightKg.divide(unitWeightKg, 0, RoundingMode.HALF_UP).intValue();
    }

    public Integer sensorUnits(Instant now) {
        return sensorOnline(now) ? toUnits(lastWeightKg, unitWeightKg) : null;
    }

    /** R18: the sensor only verifies; when it is not online the registered stock is the reference. */
    public int referenceUnits(Instant now) {
        Integer units = sensorUnits(now);
        return units != null ? units : registeredStock;
    }

    public String level(Instant now) {
        return referenceUnits(now) <= minimumThreshold ? "lowStock" : "healthy";
    }

    public ComparisonRow compare(Instant now) {
        if (!sensorOnline(now)) {
            return new ComparisonRow(
                    productId, name, registeredStock, null, null, null, ComparisonRow.REGISTERED_ONLY);
        }
        int physical = toUnits(lastWeightKg, unitWeightKg);
        BigDecimal pct = registeredStock > 0
                ? BigDecimal.valueOf(physical - registeredStock)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(registeredStock), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(1);
        String result = pct.abs().compareTo(DISCREPANCY_PERCENT) > 0
                ? ComparisonRow.DISCREPANCY
                : ComparisonRow.MATCH;
        return new ComparisonRow(productId, name, registeredStock, lastWeightKg, physical, pct, result);
    }
}
