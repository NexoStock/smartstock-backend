
package com.nexostock.backend.purchasing.interfaces.rest;

import com.nexostock.backend.purchasing.application.commandservices.PurchasingCommandService;
import com.nexostock.backend.purchasing.application.queryservices.PurchasingQueryService;

import com.nexostock.backend.purchasing.domain.model.commands.CancelPurchaseCommand;
import com.nexostock.backend.purchasing.domain.model.commands.ReceivePurchaseCommand;

import com.nexostock.backend.purchasing.domain.model.queries.GetAllPurchasesQuery;
import com.nexostock.backend.purchasing.domain.model.queries.GetPurchaseByIdQuery;

import com.nexostock.backend.purchasing.interfaces.rest.resources.CreatePurchaseResource;

import com.nexostock.backend.purchasing.interfaces.rest.transform.CreatePurchaseCommandFromResourceAssembler;
import com.nexostock.backend.purchasing.interfaces.rest.transform.PurchaseResourceFromEntityAssembler;

import com.nexostock.backend.shared.interfaces.rest.security.CurrentBusinessProvider;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for purchasing management.
 *
 * Controlador REST para gestionar compras.
 *
 * All operations are restricted to the current business.
 */
@RestController
@RequestMapping(
        value = "/api/compras",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Purchasing - Purchases",
        description = "Gestion de compras de NexoStock"
)
public class PurchasesController {

    private final PurchasingCommandService commands;
    private final PurchasingQueryService queries;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public PurchasesController(
            PurchasingCommandService commands,
            PurchasingQueryService queries,
            CurrentBusinessProvider current,
            ProblemDetailFromApplicationErrorAssembler problems) {

        this.commands = commands;
        this.queries = queries;
        this.current = current;
        this.problems = problems;
    }

    // ==========================================
    // 1. CREATE PURCHASE
    // POST /api/compras
    // ==========================================

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Registrar compra",
            description = "Registra una compra pendiente con sus productos"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Compra registrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos invalidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor o producto no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto al registrar la compra"
            )
    })
    public ResponseEntity<?> create(
            @Valid @RequestBody CreatePurchaseResource resource) {

        Long businessId = current.businessId();

        var command =
                CreatePurchaseCommandFromResourceAssembler.toCommand(
                        businessId,
                        resource
                );

        return commands.handle(command)
                .<ResponseEntity<?>>fold(
                        purchase -> purchaseResponse(
                                businessId,
                                purchase.getId(),
                                HttpStatus.CREATED
                        ),
                        problems::toResponseEntity
                );
    }

    // ==========================================
    // 2. GET ALL PURCHASES
    // GET /api/compras
    // ==========================================

    @GetMapping
    @Operation(
            summary = "Listar compras",
            description = "Obtiene las compras del negocio"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de compras"
            )
    })
    public ResponseEntity<?> getAll() {

        var query = new GetAllPurchasesQuery(
                current.businessId()
        );

        return queries.handle(query)
                .<ResponseEntity<?>>fold(
                        purchases -> ResponseEntity.ok(
                                purchases.stream()
                                        .map(
                                                PurchaseResourceFromEntityAssembler
                                                        ::toResource
                                        )
                                        .toList()
                        ),
                        problems::toResponseEntity
                );
    }

    // ==========================================
    // 3. GET PURCHASE BY ID
    // GET /api/compras/{id}
    // ==========================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar compra",
            description = "Obtiene una compra y sus productos"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Compra encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compra no encontrada"
            )
    })
    public ResponseEntity<?> getById(
            @PathVariable Long id) {

        return purchaseResponse(
                current.businessId(),
                id,
                HttpStatus.OK
        );
    }

    // ==========================================
    // 4. RECEIVE PURCHASE
    // POST /api/compras/{id}/recibir
    // ==========================================

    @PostMapping("/{id}/recibir")
    @Operation(
            summary = "Recibir compra",
            description = "Recibe una compra y registra sus entradas de inventario"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Compra recibida o ya recibida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compra o producto no encontrado"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "No se puede recibir la compra"
            )
    })
    public ResponseEntity<?> receive(
            @PathVariable Long id) {

        Long businessId = current.businessId();

        var command = new ReceivePurchaseCommand(
                businessId,
                id
        );

        return commands.handle(command)
                .<ResponseEntity<?>>fold(
                        purchase -> purchaseResponse(
                                businessId,
                                purchase.getId(),
                                HttpStatus.OK
                        ),
                        problems::toResponseEntity
                );
    }

    // ==========================================
    // 5. CANCEL PURCHASE
    // POST /api/compras/{id}/cancelar
    // ==========================================

    @PostMapping("/{id}/cancelar")
    @Operation(
            summary = "Cancelar compra",
            description = "Cancela una compra que todavia no ha sido recibida"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Compra cancelada o ya cancelada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compra no encontrada"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "La compra ya fue recibida"
            )
    })
    public ResponseEntity<?> cancel(
            @PathVariable Long id) {

        Long businessId = current.businessId();

        var command = new CancelPurchaseCommand(
                businessId,
                id
        );

        return commands.handle(command)
                .<ResponseEntity<?>>fold(
                        purchase -> purchaseResponse(
                                businessId,
                                purchase.getId(),
                                HttpStatus.OK
                        ),
                        problems::toResponseEntity
                );
    }

    // ==========================================
    // HELPER: LOAD PURCHASE WITH ITEMS
    // ==========================================

    /**
     * Reloads the purchase through the query service
     * so its items are initialized before creating
     * the REST response.
     */
    private ResponseEntity<?> purchaseResponse(
            Long businessId,
            Long purchaseId,
            HttpStatus status) {

        var query = new GetPurchaseByIdQuery(
                businessId,
                purchaseId
        );

        return queries.handle(query)
                .<ResponseEntity<?>>fold(
                        purchase -> ResponseEntity
                                .status(status)
                                .body(
                                        PurchaseResourceFromEntityAssembler
                                                .toResource(purchase)
                                ),
                        problems::toResponseEntity
                );
    }
}
