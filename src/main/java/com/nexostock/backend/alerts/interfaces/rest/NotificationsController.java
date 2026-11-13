package com.nexostock.backend.alerts.interfaces.rest;

import com.nexostock.backend.alerts.application.commandservices.AlertCommandService;
import com.nexostock.backend.alerts.application.queryservices.AlertQueryService;
import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import com.nexostock.backend.alerts.domain.model.commands.RetryNotificationCommand;
import com.nexostock.backend.alerts.domain.model.queries.GetNotificationsQuery;
import com.nexostock.backend.shared.interfaces.rest.security.CurrentBusinessProvider;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/api/notificaciones",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications")
public class NotificationsController {

    public record NotificationResource(
            Long id,
            String canal,
            String destino,
            String asunto,
            String contenido,
            String estado,
            int intentos,
            Instant creadaEn) {}

    private final AlertQueryService queries;
    private final AlertCommandService commands;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public NotificationsController(
            AlertQueryService queries,
            AlertCommandService commands,
            CurrentBusinessProvider current,
            ProblemDetailFromApplicationErrorAssembler problems) {
        this.queries = queries;
        this.commands = commands;
        this.current = current;
        this.problems = problems;
    }

    @GetMapping
    @Operation(summary = "List notifications of the business")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications found"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<?> getAll() {
        return queries.handle(
                        new GetNotificationsQuery(current.businessId()))
                .<ResponseEntity<?>>fold(
                        list -> ResponseEntity.ok(
                                list.stream()
                                        .map(this::toResource)
                                        .toList()),
                        problems::toResponseEntity);
    }

    @PostMapping("/{id}/reintentar")
    @Operation(summary = "Requeue a failed notification")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Notification queued"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "422", description = "Notification is not failed")
    })
    public ResponseEntity<?> retry(@PathVariable("id") Long id) {
        return commands.handle(
                        new RetryNotificationCommand(current.businessId(), id))
                .<ResponseEntity<?>>fold(
                        result -> ResponseEntity.status(HttpStatus.ACCEPTED)
                                .body(Map.of(
                                        "id", id,
                                        "estado", "PENDING")),
                        problems::toResponseEntity);
    }

    private NotificationResource toResource(OutboundMessage message) {
        return new NotificationResource(
                message.getId(),
                message.getChannel(),
                message.getDestination(),
                message.getSubject(),
                message.getContent(),
                message.getStatus(),
                message.getAttempts(),
                message.getCreatedAt());
    }
}