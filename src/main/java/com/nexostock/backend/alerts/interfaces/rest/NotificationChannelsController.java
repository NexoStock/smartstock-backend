package com.nexostock.backend.alerts.interfaces.rest;

import com.nexostock.backend.alerts.application.commandservices.AlertCommandService;
import com.nexostock.backend.alerts.application.queryservices.AlertQueryService;
import com.nexostock.backend.alerts.domain.model.aggregates.NotificationChannel;
import com.nexostock.backend.alerts.domain.model.commands.UpdateChannelsCommand;
import com.nexostock.backend.alerts.domain.model.queries.GetChannelsQuery;
import com.nexostock.backend.shared.interfaces.rest.security.CurrentBusinessProvider;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/api/canales-notificacion",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notification channels")
public class NotificationChannelsController {

    public record ChannelResource(
            Long id, String canal, String destino, boolean activo) {}

    public record ChannelChangeResource(
            @NotNull Long id,
            @NotNull Boolean activo) {}

    private final AlertQueryService queries;
    private final AlertCommandService commands;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public NotificationChannelsController(
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
    @Operation(summary = "List notification channels of the business")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Channels found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Business contact not found")
    })
    public ResponseEntity<?> getAll() {
        return queries.handle(new GetChannelsQuery(current.businessId()))
                .<ResponseEntity<?>>fold(
                        list -> ResponseEntity.ok(
                                list.stream()
                                        .map(this::toResource)
                                        .toList()),
                        problems::toResponseEntity);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Activate or deactivate notification channels")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Channels updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Channel not found"),
            @ApiResponse(responseCode = "422", description = "At least one channel must remain active")
    })
    public ResponseEntity<?> update(
            @Valid @RequestBody
            List<@NotNull @Valid ChannelChangeResource> body) {

        var changes = body.stream()
                .map(item -> new UpdateChannelsCommand.Change(
                        item.id(), item.activo()))
                .toList();

        return commands.handle(new UpdateChannelsCommand(
                        current.businessId(), changes))
                .<ResponseEntity<?>>fold(
                        list -> ResponseEntity.ok(
                                list.stream()
                                        .map(this::toResource)
                                        .toList()),
                        problems::toResponseEntity);
    }

    private ChannelResource toResource(NotificationChannel channel) {
        return new ChannelResource(
                channel.getId(),
                channel.getChannel(),
                channel.getDestination(),
                channel.isActive());
    }
}