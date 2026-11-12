package com.nexostock.backend.alerts.interfaces.rest;

import com.nexostock.backend.alerts.application.queryservices.AlertQueryService;
import com.nexostock.backend.alerts.domain.model.queries.GetAlertsQuery;
import com.nexostock.backend.alerts.interfaces.rest.transform.AlertResourceFromEntityAssembler;
import com.nexostock.backend.shared.interfaces.rest.security.CurrentBusinessProvider;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "/api/alertas",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Alerts", description = "Low-stock alerts")
public class AlertsController {

    private final AlertQueryService queries;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public AlertsController(
            AlertQueryService queries,
            CurrentBusinessProvider current,
            ProblemDetailFromApplicationErrorAssembler problems) {
        this.queries = queries;
        this.current = current;
        this.problems = problems;
    }

    @GetMapping
    @Operation(
            summary = "List alerts",
            description = "Active alerts and the latest resolved ones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts found")
    })
    public ResponseEntity<?> getAll() {
        return queries
                .handle(new GetAlertsQuery(current.businessId()))
                .<ResponseEntity<?>>fold(
                        list -> ResponseEntity.ok(
                                list.stream()
                                        .map(AlertResourceFromEntityAssembler::toResource)
                                        .toList()),
                        problems::toResponseEntity);
    }
}