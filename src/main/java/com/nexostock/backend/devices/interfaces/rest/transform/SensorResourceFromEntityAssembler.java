package com.nexostock.backend.devices.interfaces.rest.transform;

import com.nexostock.backend.devices.application.internal.outboundservices.ExternalCatalogService;
import com.nexostock.backend.devices.application.internal.outboundservices.ExternalCatalogService.LinkedProduct;
import com.nexostock.backend.devices.domain.model.aggregates.Sensor;
import com.nexostock.backend.devices.domain.model.aggregates.SensorReading;
import com.nexostock.backend.devices.interfaces.rest.resources.ReadingHistoryResource;
import com.nexostock.backend.devices.interfaces.rest.resources.SensorResource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Builds the sensor answer with the product name and the units
 * on the scale (weight / unit weight, rounded down).
 */
@Component
public class SensorResourceFromEntityAssembler {

    private final ExternalCatalogService catalog;

    public SensorResourceFromEntityAssembler(ExternalCatalogService catalog) {
        this.catalog = catalog;
    }

    public SensorResource toResource(Sensor sensor, Instant now) {

        LinkedProduct product = catalog
                .findProduct(sensor.getBusinessId(), sensor.getProductId())
                .orElse(null);

        BigDecimal weight = sensor.getLastWeightKg() == null
                ? BigDecimal.ZERO
                : sensor.getLastWeightKg();

        return new SensorResource(
                sensor.getId(),
                sensor.getDeviceCode(),
                sensor.getProductId(),
                product == null ? null : product.name(),
                sensor.status(now).name().toLowerCase(Locale.ROOT),
                sensor.getLastReadingAt(),
                weight,
                product == null || sensor.getLastWeightKg() == null
                        ? null
                        : product.unitsFor(weight)
        );
    }

    public static ReadingHistoryResource toResource(SensorReading reading) {
        return new ReadingHistoryResource(
                reading.getId(),
                reading.getWeightKg(),
                reading.getMeasuredAt());
    }
}
