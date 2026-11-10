package com.nexostock.backend.devices.application.internal.commandservices;

import com.nexostock.backend.devices.application.commandservices.SensorCommandService;
import com.nexostock.backend.devices.application.internal.outboundservices.ExternalCatalogService;
import com.nexostock.backend.devices.domain.model.aggregates.Sensor;
import com.nexostock.backend.devices.domain.model.aggregates.SensorReading;
import com.nexostock.backend.devices.domain.model.commands.DeleteSensorCommand;
import com.nexostock.backend.devices.domain.model.commands.LinkSensorCommand;
import com.nexostock.backend.devices.domain.model.commands.ReceiveReadingCommand;
import com.nexostock.backend.devices.domain.model.commands.RegisterSensorCommand;
import com.nexostock.backend.devices.domain.model.commands.RotateDeviceKeyCommand;
import com.nexostock.backend.devices.domain.model.commands.UnlinkSensorCommand;
import com.nexostock.backend.devices.infrastructure.persistence.jpa.SensorReadingRepository;
import com.nexostock.backend.devices.infrastructure.persistence.jpa.SensorRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorCommandServiceImpl implements SensorCommandService {

    private final SensorRepository sensors;
    private final SensorReadingRepository readings;
    private final ExternalCatalogService catalog;

    public SensorCommandServiceImpl(
            SensorRepository sensors,
            SensorReadingRepository readings,
            ExternalCatalogService catalog) {

        this.sensors = sensors;
        this.readings = readings;
        this.catalog = catalog;
    }

    @Override
    @Transactional
    public Result<SensorWithKey, ApplicationError> handle(
            RegisterSensorCommand command) {

        if (sensors.findByDeviceCode(command.deviceCode()).isPresent()) {
            return Result.failure(ApplicationError.conflict(
                    "DEVICE_CODE_ALREADY_REGISTERED"));
        }

        String plainKey = DeviceKeys.newKey();
        Sensor saved = sensors.save(new Sensor(
                command.businessId(),
                command.deviceCode(),
                DeviceKeys.sha256(plainKey)));

        return Result.success(new SensorWithKey(
                saved.getId(), saved.getDeviceCode(), plainKey));
    }

    @Override
    @Transactional
    public Result<SensorReading, ApplicationError> handle(
            ReceiveReadingCommand command) {

        var found = sensors.findByDeviceCode(command.deviceCode());

        // Same answer for an unknown device and a wrong key:
        // the code alone proves nothing.
        if (found.isEmpty() || !DeviceKeys.matches(
                command.deviceKey(),
                found.get().getDeviceKeyHash())) {

            return Result.failure(ApplicationError.unauthorized(
                    "INVALID_DEVICE_CREDENTIALS"));
        }

        Sensor sensor = found.get();

        if (readings.existsBySensorIdAndMeasuredAt(
                sensor.getId(), command.measuredAt())) {
            // a retry of the device does not duplicate the reading
            return Result.failure(ApplicationError.conflict(
                    "DUPLICATE_READING"));
        }

        if (sensor.getLastReadingAt() != null
                && command.measuredAt().isBefore(sensor.getLastReadingAt())) {
            return Result.failure(ApplicationError.conflict(
                    "OUT_OF_ORDER_READING"));
        }

        sensor.registerReading(command.measuredAt(), command.weightKg());
        sensors.save(sensor);

        return Result.success(readings.save(new SensorReading(
                sensor.getId(),
                command.weightKg(),
                command.measuredAt())));
    }

    @Override
    @Transactional
    public Result<Sensor, ApplicationError> handle(LinkSensorCommand command) {

        var found = sensors.findByIdAndBusinessId(
                command.sensorId(), command.businessId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("SENSOR_NOT_FOUND"));
        }

        if (catalog.findProduct(command.businessId(), command.productId())
                .isEmpty()) {
            return Result.failure(ApplicationError.notFound("PRODUCT_NOT_FOUND"));
        }

        Sensor sensor = found.get();
        if (sensor.isLinked()) {
            return Result.failure(ApplicationError.conflict(
                    "SENSOR_ALREADY_LINKED"));
        }

        // one sensor per product, so the weight is not counted twice
        if (sensors.existsByBusinessIdAndProductId(
                command.businessId(), command.productId())) {
            return Result.failure(ApplicationError.conflict(
                    "PRODUCT_ALREADY_HAS_SENSOR"));
        }

        sensor.linkTo(command.productId());
        return Result.success(sensors.save(sensor));
    }

    @Override
    @Transactional
    public Result<Sensor, ApplicationError> handle(UnlinkSensorCommand command) {

        var found = sensors.findByIdAndBusinessId(
                command.sensorId(), command.businessId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("SENSOR_NOT_FOUND"));
        }

        Sensor sensor = found.get();
        if (!sensor.isLinked()) {
            return Result.failure(ApplicationError.conflict("SENSOR_NOT_LINKED"));
        }

        sensor.unlink();
        return Result.success(sensors.save(sensor));
    }

    @Override
    @Transactional
    public Result<SensorWithKey, ApplicationError> handle(
            RotateDeviceKeyCommand command) {

        var found = sensors.findByIdAndBusinessId(
                command.sensorId(), command.businessId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("SENSOR_NOT_FOUND"));
        }

        Sensor sensor = found.get();
        String plainKey = DeviceKeys.newKey();
        sensor.replaceKeyHash(DeviceKeys.sha256(plainKey));
        sensors.save(sensor);

        return Result.success(new SensorWithKey(
                sensor.getId(), sensor.getDeviceCode(), plainKey));
    }

    @Override
    @Transactional
    public Result<Void, ApplicationError> handle(DeleteSensorCommand command) {

        var found = sensors.findByIdAndBusinessId(
                command.sensorId(), command.businessId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("SENSOR_NOT_FOUND"));
        }

        readings.deleteAllBySensorId(command.sensorId());
        sensors.delete(found.get());
        return Result.success(null);
    }
}
