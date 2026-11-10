package com.nexostock.backend.devices.application.internal.queryservices;

import com.nexostock.backend.devices.application.queryservices.SensorQueryService;
import com.nexostock.backend.devices.domain.model.aggregates.Sensor;
import com.nexostock.backend.devices.domain.model.aggregates.SensorReading;
import com.nexostock.backend.devices.domain.model.queries.GetLatestReadingsQuery;
import com.nexostock.backend.devices.domain.model.queries.GetSensorByIdQuery;
import com.nexostock.backend.devices.domain.model.queries.GetSensorsQuery;
import com.nexostock.backend.devices.infrastructure.persistence.jpa.SensorReadingRepository;
import com.nexostock.backend.devices.infrastructure.persistence.jpa.SensorRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorQueryServiceImpl implements SensorQueryService {

    private final SensorRepository sensors;
    private final SensorReadingRepository readings;

    public SensorQueryServiceImpl(
            SensorRepository sensors,
            SensorReadingRepository readings) {

        this.sensors = sensors;
        this.readings = readings;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<Sensor>, ApplicationError> handle(GetSensorsQuery query) {
        return Result.success(
                sensors.findAllByBusinessIdOrderByDeviceCodeAsc(query.businessId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Sensor, ApplicationError> handle(GetSensorByIdQuery query) {
        return sensors.findByIdAndBusinessId(query.sensorId(), query.businessId())
                .<Result<Sensor, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("SENSOR_NOT_FOUND")));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<SensorReading>, ApplicationError> handle(
            GetLatestReadingsQuery query) {

        if (sensors.findByIdAndBusinessId(query.sensorId(), query.businessId())
                .isEmpty()) {
            return Result.failure(ApplicationError.notFound("SENSOR_NOT_FOUND"));
        }
        return Result.success(
                readings.findTop10BySensorIdOrderByMeasuredAtDesc(query.sensorId()));
    }
}
