package com.epam.productos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Error de la API")
public record ErrorResponse(
        Instant timestamp,
        @Schema(example = "400")
        int status,
        @Schema(example = "Bad Request")
        String error,
        List<String> mensajes
) {
}
