package com.nexostock.backend.devices.application.commandservices;

import com.nexostock.backend.devices.domain.model.aggregates.Sensor;
import com.nexostock.backend.devices.domain.model.aggregates.SensorReading;
import com.nexostock.backend.devices.domain.model.commands.DeleteSensorCommand;
import com.nexostock.backend.devices.domain.model.commands.LinkSensorCommand;
import com.nexostock.backend.devices.domain.model.commands.ReceiveReadingCommand;
import com.nexostock.backend.devices.domain.model.commands.RegisterSensorCommand;
import com.nexostock.backend.devices.domain.model.commands.RotateDeviceKeyCommand;
import com.nexostock.backend.devices.domain.model.commands.UnlinkSensorCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

/**
 * Six commands of the Devices context.
 * Los seis comandos del contexto de dispositivos.
 */
public interface SensorCommandService {

    /** The plain key travels only in this answer; only its hash is stored. */
    record SensorWithKey(Long id, String deviceCode, String deviceKey) {
    }

    Result<SensorWithKey, ApplicationError> handle(RegisterSensorCommand command);

    Result<SensorReading, ApplicationError> handle(ReceiveReadingCommand command);

    Result<Sensor, ApplicationError> handle(LinkSensorCommand command);

    Result<Sensor, ApplicationError> handle(UnlinkSensorCommand command);

    Result<SensorWithKey, ApplicationError> handle(RotateDeviceKeyCommand command);

    Result<Void, ApplicationError> handle(DeleteSensorCommand command);
}
