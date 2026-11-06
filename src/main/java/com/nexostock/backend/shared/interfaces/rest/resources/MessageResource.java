package com.nexostock.backend.shared.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record MessageResource(
        @Schema(example = "Operation completed")
        String message) {
}