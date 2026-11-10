package com.nexostock.backend.alerts.interfaces.rest.resources;

import java.time.Instant;

public record AlertResource(
        Long id,
        Long productoId,
        String productoNombre,
        String tipo,
        String estado,
        int stockReferencia,
        String fuente,
        Long minutosDesdeLectura,
        Integer stockActual,
        Integer umbralMinimo,
        Instant creadaEn,
        Instant resueltaEn) {
}