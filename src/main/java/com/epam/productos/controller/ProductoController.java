package com.epam.productos.controller;

import com.epam.productos.dto.ActualizarPrecioRequest;
import com.epam.productos.dto.CatalogoResponse;
import com.epam.productos.dto.ErrorResponse;
import com.epam.productos.dto.ProductoRequest;
import com.epam.productos.dto.ProductoResponse;
import com.epam.productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
@Validated
@Tag(name = "Productos", description = "Catálogo de productos con baja lógica y actualización exclusiva de precio")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un producto",
            description = "Crea un producto activo. El precio debe ser mayor que cero. "
                    + "El stock define el estado de inventario: DISPONIBLE si es mayor que cero y AGOTADO si es cero.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto registrado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nombre, precio o stock inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductoResponse registrar(@Valid @RequestBody ProductoRequest request) {
        return productoService.registrar(request);
    }

    @GetMapping
    @Operation(summary = "Consultar el catálogo",
            description = "Devuelve los productos activos. Sin page ni size responde el catálogo completo. "
                    + "Con page o size pagina el resultado: page empieza en 0 y size vale 20 si se omite. "
                    + "nombre filtra por coincidencia parcial sin distinguir mayúsculas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catálogo activo",
                    content = @Content(schema = @Schema(implementation = CatalogoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CatalogoResponse consultarCatalogo(
            @Parameter(description = "Número de página, desde 0. Omitir junto con size para traer todo el catálogo")
            @RequestParam(required = false)
            @Min(value = 0, message = "La página no puede ser negativa")
            Integer page,
            @Parameter(description = "Tamaño de página. Por defecto 20 cuando se pagina")
            @RequestParam(required = false)
            @Min(value = 1, message = "El tamaño de página debe ser mayor que cero")
            Integer size,
            @Parameter(description = "Filtro opcional por nombre")
            @RequestParam(required = false)
            String nombre) {
        return productoService.consultarCatalogo(page, size, nombre);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto activo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto inexistente o dado de baja",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductoResponse obtener(
            @Parameter(description = "Identificador del producto")
            @PathVariable UUID id) {
        return productoService.obtener(id);
    }

    @PatchMapping("/{id}/precio")
    @Operation(summary = "Actualizar el precio",
            description = "Recurso exclusivo para modificar el precio. El nombre y el stock no cambian. "
                    + "El precio debe ser mayor que cero.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Precio actualizado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "El precio no es mayor que cero",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto inexistente o dado de baja",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductoResponse actualizarPrecio(
            @Parameter(description = "Identificador del producto")
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarPrecioRequest request) {
        return productoService.actualizarPrecio(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Dar de baja un producto",
            description = "Baja lógica. El producto deja de aparecer en el catálogo y en las demás operaciones.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto dado de baja"),
            @ApiResponse(responseCode = "404", description = "Producto inexistente o ya dado de baja",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void eliminar(
            @Parameter(description = "Identificador del producto")
            @PathVariable UUID id) {
        productoService.eliminar(id);
    }
}
