
package com.nexostock.backend.purchasing.interfaces.rest;

import com.nexostock.backend.purchasing.application.commandservices.PurchasingCommandService;
import com.nexostock.backend.purchasing.application.queryservices.PurchasingQueryService;

import com.nexostock.backend.purchasing.domain.model.commands.DeactivateSupplierCommand;
import com.nexostock.backend.purchasing.domain.model.queries.GetAllSuppliersQuery;
import com.nexostock.backend.purchasing.domain.model.queries.GetSupplierByIdQuery;

import com.nexostock.backend.purchasing.interfaces.rest.resources.SaveSupplierResource;

import com.nexostock.backend.purchasing.interfaces.rest.transform.CreateSupplierCommandFromResourceAssembler;
import com.nexostock.backend.purchasing.interfaces.rest.transform.UpdateSupplierCommandFromResourceAssembler;
import com.nexostock.backend.purchasing.interfaces.rest.transform.SupplierResourceFromEntityAssembler;

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

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for supplier management.
 *
 * Controlador REST para gestionar proveedores.
 *
 * All operations are restricted to the current business.
 */
@RestController
@RequestMapping(
        value = "/api/proveedores",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Purchasing - Suppliers",
        description = "Gestión de proveedores de NexoStock"
)
public class SuppliersController {

    private final PurchasingCommandService commands;
    private final PurchasingQueryService queries;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public SuppliersController(
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
    // 1. CREATE SUPPLIER
    // POST /api/proveedores
    // ==========================================

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Registrar proveedor",
            description = "Registra un nuevo proveedor en el negocio"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Proveedor registrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Documento tributario duplicado"
            )
    })
    public ResponseEntity<?> create(
            @Valid @RequestBody SaveSupplierResource resource) {

        var command =
                CreateSupplierCommandFromResourceAssembler.toCommand(
                        current.businessId(),
                        resource
                );

        return commands.handle(command)
                .<ResponseEntity<?>>fold(
                        supplier -> ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                        SupplierResourceFromEntityAssembler
                                                .toResource(supplier)
                                ),
                        problems::toResponseEntity
                );
    }

    // ==========================================
    // 2. GET ALL SUPPLIERS
    // GET /api/proveedores
    // ==========================================

    @GetMapping
    @Operation(
            summary = "Listar proveedores",
            description = "Obtiene los proveedores activos del negocio"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de proveedores"
            )
    })
    public ResponseEntity<?> getAll() {

        var query = new GetAllSuppliersQuery(
                current.businessId()
        );

        return queries.handle(query)
                .<ResponseEntity<?>>fold(
                        suppliers -> ResponseEntity.ok(
                                suppliers.stream()
                                        .map(
                                                SupplierResourceFromEntityAssembler
                                                        ::toResource
                                        )
                                        .toList()
                        ),
                        problems::toResponseEntity
                );
    }

    // ==========================================
    // 3. GET SUPPLIER BY ID
    // GET /api/proveedores/{id}
    // ==========================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar proveedor",
            description = "Obtiene un proveedor por su ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Proveedor encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado"
            )
    })
    public ResponseEntity<?> getById(
            @PathVariable Long id) {

        var query = new GetSupplierByIdQuery(
                current.businessId(),
                id
        );

        return queries.handle(query)
                .<ResponseEntity<?>>fold(
                        supplier -> ResponseEntity.ok(
                                SupplierResourceFromEntityAssembler
                                        .toResource(supplier)
                        ),
                        problems::toResponseEntity
                );
    }

    // ==========================================
    // 4. UPDATE SUPPLIER
    // PUT /api/proveedores/{id}
    // ==========================================

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Actualizar proveedor",
            description = "Actualiza la información de un proveedor"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Proveedor actualizado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Documento tributario duplicado"
            )
    })
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody SaveSupplierResource resource) {

        var command =
                UpdateSupplierCommandFromResourceAssembler.toCommand(
                        current.businessId(),
                        id,
                        resource
                );

        return commands.handle(command)
                .<ResponseEntity<?>>fold(
                        supplier -> ResponseEntity.ok(
                                SupplierResourceFromEntityAssembler
                                        .toResource(supplier)
                        ),
                        problems::toResponseEntity
                );
    }

    // ==========================================
    // 5. DEACTIVATE SUPPLIER
    // DELETE /api/proveedores/{id}
    // ==========================================

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Desactivar proveedor",
            description = "Desactiva un proveedor sin borrar su historial"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Proveedor desactivado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado"
            )
    })
    public ResponseEntity<?> deactivate(
            @PathVariable Long id) {

        var command = new DeactivateSupplierCommand(
                current.businessId(),
                id
        );

        return commands.handle(command)
                .<ResponseEntity<?>>fold(
                        supplier -> ResponseEntity.ok(
                                SupplierResourceFromEntityAssembler
                                        .toResource(supplier)
                        ),
                        problems::toResponseEntity
                );
    }
}
