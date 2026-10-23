package com.epam.productos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Nuevo precio del producto. Es el único dato que modifica este recurso")
public record ActualizarPrecioRequest(
        @Schema(description = "Precio del producto. Debe ser mayor que cero", example = "50.00",
                minimum = "0", exclusiveMinimum = true)
        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor que cero")
        BigDecimal precio
) {
}
