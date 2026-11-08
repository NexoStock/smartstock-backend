package com.nexostock.backend.devices.interfaces.rest;

import com.nexostock.backend.devices.application.commandservices.SensorCommandService;
import com.nexostock.backend.devices.domain.model.commands.ReceiveReadingCommand;
import com.nexostock.backend.devices.interfaces.rest.resources.ReadingResource;
import com.nexostock.backend.devices.interfaces.rest.transform.SensorResourceFromEntityAssembler;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point of the devices. /api/device-readings/** has no user
 * token (JWT), but it is NOT anonymous: every request must carry
 * a valid X-Device-Key, otherwise the answer is 401.
 */
@RestController
@RequestMapping(
        value = "/api/device-readings",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Device readings",
        description = "Weight readings sent by the IoT devices"
)
public class DeviceReadingsController {

    public static final String DEVICE_KEY_HEADER = "X-Device-Key";

    private final SensorCommandService commands;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public DeviceReadingsController(
            SensorCommandService commands,
            ProblemDetailFromApplicationErrorAssembler problems) {

        this.commands = commands;
        this.problems = problems;
    }

    @PostMapping(value = "/{deviceCode}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Receive a reading",
            description = "A device sends its weight in kilograms with its key in X-Device-Key"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reading stored"),
            @ApiResponse(responseCode = "400", description = "Invalid weight or future date"),
            @ApiResponse(responseCode = "401", description = "Missing or wrong X-Device-Key"),
            @ApiResponse(responseCode = "409", description = "Duplicate or out-of-order reading")
    })
    public ResponseEntity<?> receive(
            @PathVariable String deviceCode,
            @Parameter(description = "Key shown once when the sensor was registered")
            @RequestHeader(value = DEVICE_KEY_HEADER, required = false)
            String deviceKey,
            @Valid @RequestBody ReadingResource body) {

        return commands
                .handle(new ReceiveReadingCommand(
                        deviceCode, deviceKey, body.weightKg(), body.measuredAt()))
                .<ResponseEntity<?>>fold(
                        r -> ResponseEntity.status(HttpStatus.CREATED)
                                .body(SensorResourceFromEntityAssembler.toResource(r)),
                        problems::toResponseEntity
                );
    }
}
