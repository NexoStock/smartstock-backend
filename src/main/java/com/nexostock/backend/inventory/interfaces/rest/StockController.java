
package com.nexostock.backend.inventory.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexostock.backend.inventory.application.commandservices.MovementCommandService;
import com.nexostock.backend.inventory.application.queryservices.MovementQueryService;
import com.nexostock.backend.inventory.domain.model.commands.AdjustStockCommand;
import com.nexostock.backend.inventory.domain.model.queries.GetMovementsByProductQuery;
import com.nexostock.backend.shared.interfaces.rest.security.CurrentBusinessProvider;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/productos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Inventory", description = "Stock movements and adjustments")
public class StockController {

    public record AdjustStockResource(
            @JsonProperty("stockRegistrado")
            @NotNull
            @PositiveOrZero
            @Schema(example = "35")
            Integer newStock,

            @JsonProperty("motivo")
            @NotBlank
            @Schema(example = "Conteo fisico")
            String reason) {}

    public record MovementResource(
            Long id,
            int quantity,
            int stockAfter,
            String type) {}

    private final MovementCommandService commands;
    private final MovementQueryService queries;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public StockController(
            MovementCommandService commands,
            MovementQueryService queries,
            CurrentBusinessProvider current,
            ProblemDetailFromApplicationErrorAssembler problems) {

        this.commands = commands;
        this.queries = queries;
        this.current = current;
        this.problems = problems;
    }

    @GetMapping("/{id}/movimientos")
    @Operation(
            summary = "Get product movements",
            description = "Returns the stock movement history of a product"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movement history retrieved"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<?> getMovements(
            @PathVariable Long id) {

        var query = new GetMovementsByProductQuery(
                current.businessId(),
                id
        );

        return queries
                .handle(query)
                .<ResponseEntity<?>>fold(
                        history -> ResponseEntity.ok(
                                history.stream()
                                        .map(m -> new MovementResource(
                                                m.getId(),
                                                m.getQuantity(),
                                                m.getStockAfter(),
                                                m.getType().name()
                                        ))
                                        .toList()
                        ),
                        problems::toResponseEntity
                );
    }

    @PostMapping(
            value = "/{id}/ajustes",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Adjust stock",
            description = "Sets the accounting stock and records an audited movement"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock adjusted"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "422", description = "Nothing to change")
    })
    public ResponseEntity<?> adjust(
            @PathVariable Long id,
            @Valid @RequestBody AdjustStockResource body) {

        var command = new AdjustStockCommand(
                current.businessId(),
                id,
                body.newStock(),
                body.reason()
        );

        return commands
                .handle(command)
                .<ResponseEntity<?>>fold(
                        m -> ResponseEntity.ok(
                                new MovementResource(
                                        m.getId(),
                                        m.getQuantity(),
                                        m.getStockAfter(),
                                        m.getType().name()
                                )
                        ),
                        problems::toResponseEntity
                );
    }
}
