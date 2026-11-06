
package com.nexostock.backend.catalog.interfaces.rest;

import com.nexostock.backend.catalog.application.commandservices.ProductCommandService;
import com.nexostock.backend.catalog.application.queryservices.ProductQueryService;
import com.nexostock.backend.catalog.domain.model.commands.RetireProductCommand;
import com.nexostock.backend.catalog.domain.model.commands.UpdateThresholdCommand;
import com.nexostock.backend.catalog.domain.model.queries.GetAllProductsQuery;
import com.nexostock.backend.catalog.domain.model.queries.GetProductByIdQuery;
import com.nexostock.backend.catalog.interfaces.rest.resources.SaveProductResource;
import com.nexostock.backend.catalog.interfaces.rest.resources.ThresholdResource;
import com.nexostock.backend.catalog.interfaces.rest.transform.CreateProductCommandFromResourceAssembler;
import com.nexostock.backend.catalog.interfaces.rest.transform.ProductResourceFromEntityAssembler;
import com.nexostock.backend.catalog.interfaces.rest.transform.UpdateProductCommandFromResourceAssembler;
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

@RestController
@RequestMapping(
        value = "/api/productos",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Products",
        description = "Product catalog endpoints"
)
public class ProductsController {

    private final ProductCommandService commands;
    private final ProductQueryService queries;
    private final CurrentBusinessProvider current;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public ProductsController(
            ProductCommandService commands,
            ProductQueryService queries,
            CurrentBusinessProvider current,
            ProblemDetailFromApplicationErrorAssembler problems) {

        this.commands = commands;
        this.queries = queries;
        this.current = current;
        this.problems = problems;
    }

    // ========================================
    // GET - LISTAR PRODUCTOS
    // ========================================

    @GetMapping
    @Operation(
            summary = "List products",
            description = "Active products of the authenticated business"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Products found"
            )
    })
    public ResponseEntity<?> getAll() {

        return queries
                .handle(
                        new GetAllProductsQuery(
                                current.businessId()
                        )
                )
                .<ResponseEntity<?>>fold(
                        list -> ResponseEntity.ok(
                                list.stream()
                                        .map(
                                                ProductResourceFromEntityAssembler::toResource
                                        )
                                        .toList()
                        ),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // GET - BUSCAR PRODUCTO POR ID
    // ========================================

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    public ResponseEntity<?> getById(
            @PathVariable Long id) {

        return queries
                .handle(
                        new GetProductByIdQuery(
                                current.businessId(),
                                id
                        )
                )
                .<ResponseEntity<?>>fold(
                        product -> ResponseEntity.ok(
                                ProductResourceFromEntityAssembler
                                        .toResource(product)
                        ),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // POST - CREAR PRODUCTO
    // ========================================

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Create a product",
            description = "Creates the product; initial stock is registered by Inventory"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate SKU"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Threshold exceeds capacity"
            )
    })
    public ResponseEntity<?> create(
            @Valid @RequestBody SaveProductResource body) {

        var command =
                CreateProductCommandFromResourceAssembler
                        .toCommand(
                                current.businessId(),
                                body
                        );

        return commands
                .handle(command)
                .<ResponseEntity<?>>fold(
                        product -> ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                        ProductResourceFromEntityAssembler
                                                .toResource(product)
                                ),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // PUT - ACTUALIZAR PRODUCTO
    // ========================================

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Update a product",
            description = "Stock changes only through adjustments"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate SKU"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Threshold exceeds capacity"
            )
    })
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody SaveProductResource body) {

        var command =
                UpdateProductCommandFromResourceAssembler
                        .toCommand(
                                current.businessId(),
                                id,
                                body
                        );

        return commands
                .handle(command)
                .<ResponseEntity<?>>fold(
                        product -> ResponseEntity.ok(
                                ProductResourceFromEntityAssembler
                                        .toResource(product)
                        ),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // PUT - ACTUALIZAR UMBRAL
    // ========================================

    @PutMapping(
            value = "/{id}/umbral",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Change the minimum stock threshold"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Threshold updated"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Threshold exceeds capacity"
            )
    })
    public ResponseEntity<?> updateThreshold(
            @PathVariable Long id,
            @Valid @RequestBody ThresholdResource body) {

        var command = new UpdateThresholdCommand(
                current.businessId(),
                id,
                body.minimumThreshold()
        );

        return commands
                .handle(command)
                .<ResponseEntity<?>>fold(
                        product -> ResponseEntity.ok(
                                ProductResourceFromEntityAssembler
                                        .toResource(product)
                        ),
                        problems::toResponseEntity
                );
    }

    // ========================================
    // DELETE - RETIRAR PRODUCTO
    // ========================================

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Retire a product",
            description = "Only products with zero stock can be retired"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Product retired"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Product still has stock"
            )
    })
    public ResponseEntity<?> retire(
            @PathVariable Long id) {

        return commands
                .handle(
                        new RetireProductCommand(
                                current.businessId(),
                                id
                        )
                )
                .<ResponseEntity<?>>fold(
                        result -> ResponseEntity
                                .noContent()
                                .build(),
                        problems::toResponseEntity
                );
    }
}
