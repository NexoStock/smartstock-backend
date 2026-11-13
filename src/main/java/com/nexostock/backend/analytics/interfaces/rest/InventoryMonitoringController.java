package com.nexostock.backend.analytics.interfaces.rest;

import com.nexostock.backend.analytics.domain.model.queries.GetInventoryComparisonQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetProductSensorDetailQuery;
import com.nexostock.backend.analytics.domain.model.queries.GetStockMovementsQuery;
import com.nexostock.backend.analytics.domain.services.AnalyticsQueryService;
import com.nexostock.backend.analytics.interfaces.rest.resources.InventoryComparisonResource;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Inventory monitoring", description = "Physical stock against registered stock and stock movements")
public class InventoryMonitoringController {

    private final AnalyticsQueryService queries;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public InventoryMonitoringController(
            AnalyticsQueryService queries,
            CurrentBusinessProvider current,
            ProblemDetailFromApplicationErrorAssembler problems) {
        this.queries = queries;
        this.current = current;
        this.problems = problems;
    }

    @GetMapping("/inventario/comparacion")
    @Operation(
            summary = "Compare physical and registered stock",
            description = "For each product, the units measured by its online sensor against the registered "
                    + "stock; a difference above 10 percent is a discrepancy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comparison retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<InventoryComparisonResource> comparison() {
        var comparison = queries.handle(new GetInventoryComparisonQuery(current.businessId()));
        return ResponseEntity.ok(AnalyticsResourceAssembler.toResource(comparison));
    }

    @GetMapping("/productos/{id}/detalle")
    @Operation(
            summary = "Get the sensor detail of a product",
            description = "Sensor linked to the product with its current weight, units and latest five readings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detail retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<?> productDetail(@PathVariable Long id) {
        return queries.handle(new GetProductSensorDetailQuery(current.businessId(), id))
                .<ResponseEntity<?>>fold(
                        detail -> ResponseEntity.ok(AnalyticsResourceAssembler.toResource(detail)),
                        problems::toResponseEntity);
    }

    @GetMapping("/movimientos-stock")
    @Operation(
            summary = "List stock movements",
            description = "Stock movements of every product of the business between two dates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movements retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<?> movements(
            @Parameter(example = "2026-11-01")
            @RequestParam(value = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(example = "2026-11-30")
            @RequestParam(value = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return queries.handle(new GetStockMovementsQuery(current.businessId(), from, to))
                .<ResponseEntity<?>>fold(
                        list -> ResponseEntity.ok(list.stream().map(AnalyticsResourceAssembler::toResource).toList()),
                        problems::toResponseEntity);
    }
}
