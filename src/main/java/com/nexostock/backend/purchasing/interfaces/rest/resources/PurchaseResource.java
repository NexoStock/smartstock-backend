
package com.nexostock.backend.purchasing.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Información de una compra registrada")
public record PurchaseResource(

        @JsonProperty("id")
        Long id,

        @JsonProperty("proveedorId")
        Long supplierId,

        @JsonProperty("estado")
        String status,

        @JsonProperty("productos")
        List<PurchaseItemResource> items,

        @JsonProperty("total")
        BigDecimal total

) {
}
