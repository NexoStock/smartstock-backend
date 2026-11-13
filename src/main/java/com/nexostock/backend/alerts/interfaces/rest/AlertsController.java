package com.nexostock.backend.alerts.interfaces.rest;

import com.nexostock.backend.alerts.application.queryservices.AlertQueryService;
import com.nexostock.backend.alerts.domain.model.queries.GetAlertsQuery;
import com.nexostock.backend.alerts.interfaces.rest.transform.AlertResourceFromEntityAssembler;
import com.nexostock.backend.catalog.interfaces.acl.CatalogContextFacade;
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
    private final CatalogContextFacade catalog;

    public AlertsController(
            AlertQueryService queries,
            CurrentBusinessProvider current,
            ProblemDetailFromApplicationErrorAssembler problems,
            CatalogContextFacade catalog) {
        this.queries = queries;
        this.current = current;
        this.problems = problems;
        this.catalog = catalog;
    }

    @GetMapping
    @Operation(summary = "List alerts of the business")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts found"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<?> getAll() {
        Long businessId = current.businessId();

        return queries.handle(new GetAlertsQuery(businessId))
                .<ResponseEntity<?>>fold(
                        list -> ResponseEntity.ok(
                                list.stream()
                                        .map(alert ->
                                                AlertResourceFromEntityAssembler.toResource(
                                                        alert,
                                                        catalog.findProduct(
                                                                        businessId,
                                                                        alert.getProductId())
                                                                .orElse(null)))
                                        .toList()),
                        problems::toResponseEntity);
    }
}