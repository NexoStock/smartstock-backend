package com.nexostock.backend.sales.interfaces.rest;

import com.nexostock.backend.sales.application.commandservices.SaleCommandService;
import com.nexostock.backend.sales.application.queryservices.SaleQueryService;
import com.nexostock.backend.sales.domain.model.aggregates.Sale;
import com.nexostock.backend.sales.domain.model.commands.CreateSaleCommand;
import com.nexostock.backend.sales.domain.model.queries.GetSaleByCodeQuery;
import com.nexostock.backend.sales.domain.model.queries.GetSalesQuery;
import com.nexostock.backend.sales.infrastructure.persistence.jpa.SaleItemRepository;
import com.nexostock.backend.shared.interfaces.rest.security.CurrentBusinessProvider;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/api/ventas",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sales", description = "Sale registration and history")
public class SalesController {

    public record SaleLineRequest(
            @NotNull Long productoId,
            @Min(1) int cantidad,
            BigDecimal precioUnitario) {}

    public record RegisterSaleRequest(
            @NotEmpty List<@NotNull @Valid SaleLineRequest> items) {}

    public record SaleItemResource(
            Long productoId,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal) {}

    public record SaleResource(
            String id,
            Instant fecha,
            String estado,
            BigDecimal total,
            List<SaleItemResource> items) {}

    public record SaleListResource(
            List<SaleResource> ventas,
            BigDecimal total) {}

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("America/Lima");

    private final SaleCommandService commands;
    private final SaleQueryService queries;
    private final SaleItemRepository items;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public SalesController(
            SaleCommandService commands,
            SaleQueryService queries,
            SaleItemRepository items,
            CurrentBusinessProvider current,
            ProblemDetailFromApplicationErrorAssembler problems) {
        this.commands = commands;
        this.queries = queries;
        this.items = items;
        this.current = current;
        this.problems = problems;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a sale")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sale registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterSaleRequest body,
            @RequestHeader(
                    value = "Idempotency-Key",
                    required = false) String requestKey) {

        if (requestKey != null
                && (requestKey.isBlank() || requestKey.length() > 64)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Idempotency-Key must contain 1 to 64 characters"));
        }

        var lines = body.items().stream()
                .map(line -> new CreateSaleCommand.Line(
                        line.productoId(), line.cantidad()))
                .toList();

        return commands.handle(new CreateSaleCommand(
                        current.businessId(), requestKey, lines))
                .<ResponseEntity<?>>fold(
                        sale -> ResponseEntity.status(HttpStatus.CREATED)
                                .body(Map.of("id", sale.getCode())),
                        problems::toResponseEntity);
    }

    @GetMapping
    @Operation(summary = "List sales by date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales found"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<?> list(
            @RequestParam(value = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "desde must not be after hasta"));
        }

        Instant from = desde == null ? null
                : desde.atStartOfDay(BUSINESS_ZONE).toInstant();

        Instant to = hasta == null ? null
                : hasta.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

        return queries.handle(new GetSalesQuery(
                        current.businessId(), from, to))
                .<ResponseEntity<?>>fold(
                        sales -> ResponseEntity.ok(new SaleListResource(
                                sales.stream().map(this::toResource).toList(),
                                sales.stream()
                                        .map(Sale::getTotal)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add))),
                        problems::toResponseEntity);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get sale details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sale found"),
            @ApiResponse(responseCode = "404", description = "Sale not found")
    })
    public ResponseEntity<?> getByCode(@PathVariable("code") String code) {
        return queries.handle(new GetSaleByCodeQuery(
                        current.businessId(), code))
                .<ResponseEntity<?>>fold(
                        sale -> ResponseEntity.ok(toResource(sale)),
                        problems::toResponseEntity);
    }

    private SaleResource toResource(Sale sale) {
        var lines = items.findAllBySaleId(sale.getId()).stream()
                .map(item -> new SaleItemResource(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        return new SaleResource(
                sale.getCode(),
                sale.getCreatedAt(),
                "COMPLETED",
                sale.getTotal(),
                lines);
    }
}