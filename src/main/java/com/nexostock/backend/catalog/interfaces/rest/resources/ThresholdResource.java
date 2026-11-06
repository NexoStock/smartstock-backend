
package com.nexostock.backend.catalog.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request to update the minimum stock threshold.
 * Solicitud para actualizar el umbral mínimo.
 */
@Schema(description = "New minimum stock threshold")
public record ThresholdResource(

        @JsonProperty("umbralMinimo")
        @NotNull
        @Min(1)
        @Schema(example = "12")
        Integer minimumThreshold

) {
}
