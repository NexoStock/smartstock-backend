package com.nexostock.backend.devices.application.queryservices;

import com.nexostock.backend.devices.domain.model.aggregates.Sensor;
import com.nexostock.backend.devices.domain.model.aggregates.SensorReading;
import com.nexostock.backend.devices.domain.model.queries.GetLatestReadingsQuery;
import com.nexostock.backend.devices.domain.model.queries.GetSensorByIdQuery;
import com.nexostock.backend.devices.domain.model.queries.GetSensorsQuery;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;

/**
 * Three queries of the Devices context.
 * Las tres consultas del contexto de dispositivos.
 */
public interface SensorQueryService {

    Result<List<Sensor>, ApplicationError> handle(GetSensorsQuery query);

    Result<Sensor, ApplicationError> handle(GetSensorByIdQuery query);

    Result<List<SensorReading>, ApplicationError> handle(GetLatestReadingsQuery query);
}
