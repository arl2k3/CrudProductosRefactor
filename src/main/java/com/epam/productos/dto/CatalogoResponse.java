package com.epam.productos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resultado de la consulta del catálogo. page, size, totalElementos y totalPaginas solo vienen cuando se pide paginación")
public record CatalogoResponse(
        List<ProductoResponse> productos,
        @Schema(description = "True cuando la consulta incluyó page o size")
        boolean paginado,
        Integer page,
        Integer size,
        Long totalElementos,
        Integer totalPaginas
) {
}
