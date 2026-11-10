package com.nexostock.backend.inventory.interfaces.rest;

import com.nexostock.backend.inventory.application.commandservices.MovementCommandService;
import com.nexostock.backend.inventory.domain.model.commands.AdjustStockCommand;
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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/productos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Inventory", description = "Stock movements and adjustments")
public class StockController {

  /** Request: new accounting stock + reason. */
  public record AdjustStockResource(
      @JsonProperty("stockRegistrado") @NotNull @PositiveOrZero @Schema(example = "35") Integer newStock,
      @JsonProperty("motivo") @NotBlank @Schema(example = "Conteo físico") String reason) {}

  public record MovementResource(Long id, int quantity, int stockAfter, String type) {}

  private final MovementCommandService commands;
  private final CurrentBusinessProvider current;
  private final ProblemDetailFromApplicationErrorAssembler problems;

  public StockController(
      MovementCommandService commands,
      CurrentBusinessProvider current,
      ProblemDetailFromApplicationErrorAssembler problems) {
    this.commands = commands;
    this.current = current;
    this.problems = problems;
  }

  @PostMapping(value = "/{id}/ajustes", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Adjust stock", description = "Sets the accounting stock and records an audited movement")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Stock adjusted"),
    @ApiResponse(responseCode = "400", description = "Invalid data"),
    @ApiResponse(responseCode = "404", description = "Product not found"),
    @ApiResponse(responseCode = "422", description = "Nothing to change")
  })
  public ResponseEntity<?> adjust(@PathVariable Long id, @Valid @RequestBody AdjustStockResource body) {
    var command = new AdjustStockCommand(current.businessId(), id, body.newStock(), body.reason());
    return commands
        .handle(command)
        .<ResponseEntity<?>>fold(
            m -> ResponseEntity.ok(new MovementResource(m.getId(), m.getQuantity(), m.getStockAfter(), m.getType().name())),
            problems::toResponseEntity);
  }
}