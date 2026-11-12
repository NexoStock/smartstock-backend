package com.nexostock.backend.alerts.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record AlertResource(
        Long id,
        @JsonProperty("productoId") Long productId,
        @JsonProperty("tipo") String type,
        @JsonProperty("estado") String status,
        @JsonProperty("stockReferencia") int referenceStock,
        @JsonProperty("creadaEn") Instant createdAt,
        @JsonProperty("resueltaEn") Instant resolvedAt) {
}