package com.epam.productos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos para registrar un producto")
public record ProductoRequest(
        @Schema(description = "Nombre del producto", example = "Teclado mecanico")
        @NotNull(message = "El nombre es obligatorio")
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(max = 255, message = "El nombre admite como máximo 255 caracteres")
        String nombre,

        @Schema(description = "Precio del producto. Debe ser mayor que cero", example = "120.00",
                minimum = "0", exclusiveMinimum = true)
        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor que cero")
        BigDecimal precio,

        @Schema(description = "Unidades en inventario. Cero deja el producto agotado", example = "8", minimum = "0")
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock
) {
}
