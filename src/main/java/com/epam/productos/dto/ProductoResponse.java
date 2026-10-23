package com.epam.productos.dto;

import com.epam.productos.entity.EstadoInventario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Producto activo del catálogo")
public record ProductoResponse(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(example = "Teclado mecanico")
        String nombre,
        @Schema(description = "Precio mayor que cero", example = "120.00")
        BigDecimal precio,
        @Schema(example = "8")
        Integer stock,
        @Schema(description = "DISPONIBLE si el stock es mayor que cero; AGOTADO si el stock es cero")
        EstadoInventario estadoInventario
) {
}
