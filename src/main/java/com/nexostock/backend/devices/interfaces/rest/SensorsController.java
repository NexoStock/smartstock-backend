package com.nexostock.backend.devices.interfaces.rest;

import com.nexostock.backend.devices.application.commandservices.SensorCommandService;
import com.nexostock.backend.devices.application.queryservices.SensorQueryService;
import com.nexostock.backend.devices.domain.model.commands.DeleteSensorCommand;
import com.nexostock.backend.devices.domain.model.commands.LinkSensorCommand;
import com.nexostock.backend.devices.domain.model.commands.RegisterSensorCommand;
import com.nexostock.backend.devices.domain.model.commands.RotateDeviceKeyCommand;
import com.nexostock.backend.devices.domain.model.commands.UnlinkSensorCommand;
import com.nexostock.backend.devices.domain.model.queries.GetLatestReadingsQuery;
import com.nexostock.backend.devices.domain.model.queries.GetSensorByIdQuery;
import com.nexostock.backend.devices.domain.model.queries.GetSensorsQuery;
import com.nexostock.backend.devices.interfaces.rest.resources.LinkSensorResource;
import com.nexostock.backend.devices.interfaces.rest.resources.RegisterSensorResource;
import com.nexostock.backend.devices.interfaces.rest.resources.SensorKeyResource;
import com.nexostock.backend.devices.interfaces.rest.transform.SensorResourceFromEntityAssembler;
import com.nexostock.backend.shared.interfaces.rest.security.CurrentBusinessProvider;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "/api/sensores",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Sensors",
        description = "IoT weight sensors of the business"
)
public class SensorsController {

    private final SensorCommandService commands;
    private final SensorQueryService queries;
    private final CurrentBusinessProvider current;
    private final SensorResourceFromEntityAssembler sensorAssembler;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public SensorsController(
            SensorCommandService commands,
            SensorQueryService queries,
            CurrentBusinessProvider current,
            SensorResourceFromEntityAssembler sensorAssembler,
            ProblemDetailFromApplicationErrorAssembler problems) {

        this.commands = commands;
        this.queries = queries;
        this.current = current;
        this.sensorAssembler = sensorAssembler;
        this.problems = problems;
    }

    // ========================================
    // GET - LISTAR SENSORES
    // ========================================

    @GetMapping
    @Operation(
            summary = "List sensors",
            description = "Sensors of the business with their status (rule R11)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensors found")
    })
    public ResponseEntity<?> getAll() {

        Instant now = Instant.now();

        return queries
                .handle(new GetSensorsQuery(current.businessId()))
                .<ResponseEntity<?>>fold(
                        list -> ResponseEntity.ok(list.stream()
                                .map(s -> sensorAssembler.toResource(s, now))
                                .toList()),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // GET - DETALLE DE UN SENSOR
    // ========================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a sensor",
            description = "Sensor with its status, last weight and units"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensor found"),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<?> getById(@PathVariable Long id) {

        return queries
                .handle(new GetSensorByIdQuery(current.businessId(), id))
                .<ResponseEntity<?>>fold(
                        s -> ResponseEntity.ok(
                                sensorAssembler.toResource(s, Instant.now())),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // GET - ÚLTIMAS LECTURAS
    // ========================================

    @GetMapping("/{id}/lecturas")
    @Operation(
            summary = "Latest readings",
            description = "Last 10 readings of the sensor, newest first"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Readings found"),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<?> getReadings(@PathVariable Long id) {

        return queries
                .handle(new GetLatestReadingsQuery(current.businessId(), id))
                .<ResponseEntity<?>>fold(
                        list -> ResponseEntity.ok(list.stream()
                                .map(SensorResourceFromEntityAssembler::toResource)
                                .toList()),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // POST - REGISTRAR SENSOR
    // ========================================

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Register a sensor",
            description = "Returns the device key ONCE; the device sends it in X-Device-Key"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sensor registered"),
            @ApiResponse(responseCode = "400", description = "Invalid code"),
            @ApiResponse(responseCode = "409", description = "Code already registered")
    })
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterSensorResource body) {

        return commands
                .handle(new RegisterSensorCommand(current.businessId(), body.code()))
                .<ResponseEntity<?>>fold(
                        r -> ResponseEntity.status(HttpStatus.CREATED)
                                .body(new SensorKeyResource(
                                        r.id(), r.deviceCode(), r.deviceKey())),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // POST - VINCULAR SENSOR A PRODUCTO
    // ========================================

    @PostMapping(value = "/vincular", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Link a sensor to a product",
            description = "The sensor must be available and the product must not have another sensor"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensor linked"),
            @ApiResponse(responseCode = "404", description = "Sensor or product not found"),
            @ApiResponse(responseCode = "409", description = "Sensor or product already linked")
    })
    public ResponseEntity<?> link(@Valid @RequestBody LinkSensorResource body) {

        return commands
                .handle(new LinkSensorCommand(
                        current.businessId(), body.sensorId(), body.productId()))
                .<ResponseEntity<?>>fold(
                        s -> ResponseEntity.ok(
                                sensorAssembler.toResource(s, Instant.now())),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // POST - DESVINCULAR SENSOR
    // ========================================

    @PostMapping("/{id}/desvincular")
    @Operation(
            summary = "Unlink a sensor",
            description = "The sensor becomes available again"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensor unlinked"),
            @ApiResponse(responseCode = "404", description = "Sensor not found"),
            @ApiResponse(responseCode = "409", description = "Sensor was not linked")
    })
    public ResponseEntity<?> unlink(@PathVariable Long id) {

        return commands
                .handle(new UnlinkSensorCommand(current.businessId(), id))
                .<ResponseEntity<?>>fold(
                        s -> ResponseEntity.ok(
                                sensorAssembler.toResource(s, Instant.now())),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // POST - CAMBIAR CLAVE DEL DISPOSITIVO
    // ========================================

    @PostMapping("/{id}/rotar-clave")
    @Operation(
            summary = "Rotate the device key",
            description = "Returns a new key ONCE; the previous key stops working"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New key generated"),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<?> rotateKey(@PathVariable Long id) {

        return commands
                .handle(new RotateDeviceKeyCommand(current.businessId(), id))
                .<ResponseEntity<?>>fold(
                        r -> ResponseEntity.ok(new SensorKeyResource(
                                r.id(), r.deviceCode(), r.deviceKey())),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // DELETE - ELIMINAR SENSOR
    // ========================================

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a sensor",
            description = "Removes the sensor and its readings"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sensor deleted"),
            @ApiResponse(responseCode = "404", description = "Sensor not found")
    })
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return commands
                .handle(new DeleteSensorCommand(current.businessId(), id))
                .<ResponseEntity<?>>fold(
                        ok -> ResponseEntity.noContent().build(),
                        problems::toResponseEntity
                );
    }
}
