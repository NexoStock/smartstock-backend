package com.nexostock.backend.analytics.interfaces.rest;

import com.nexostock.backend.analytics.domain.model.queries.GetDashboardQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetReportQuery;
import com.nexostock.backend.analytics.domain.services.AnalyticsQueryService;
import com.nexostock.backend.analytics.interfaces.rest.resources.DashboardResource;
import com.nexostock.backend.analytics.interfaces.rest.transform.AnalyticsResourceAssembler;
import com.nexostock.backend.shared.interfaces.rest.security.CurrentBusinessProvider;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Analytics", description = "Dashboard and reports of the business")
public class AnalyticsController {

    private final AnalyticsQueryService queries;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public AnalyticsController(
            AnalyticsQueryService queries,
            CurrentBusinessProvider current,
            ProblemDetailFromApplicationErrorAssembler problems) {
        this.queries = queries;
        this.current = current;
        this.problems = problems;
    }

    @GetMapping("/dashboard")
    @Operation(
            summary = "Get the dashboard",
            description = "Sales and purchases of the day, active low stock alerts, "
                    + "the five latest operations and the stock level of every product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<DashboardResource> dashboard() {
        var summary = queries.handle(new GetDashboardQuery(current.businessId()));
        return ResponseEntity.ok(AnalyticsResourceAssembler.toResource(summary));
    }

    @GetMapping("/reportes")
    @Operation(
            summary = "Get the report of a period",
            description = "Sales and purchases totals, stock movements and pending purchases between two dates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<?> report(
            @Parameter(example = "2026-11-01")
            @RequestParam(value = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(example = "2026-11-30")
            @RequestParam(value = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return queries.handle(new GetReportQuery(current.businessId(), from, to))
                .<ResponseEntity<?>>fold(
                        report -> ResponseEntity.ok(AnalyticsResourceAssembler.toResource(report, from, to)),
                        problems::toResponseEntity);
    }
}
