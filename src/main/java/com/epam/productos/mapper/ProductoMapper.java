package com.epam.productos.mapper;

import com.epam.productos.dto.ProductoRequest;
import com.epam.productos.dto.ProductoResponse;
import com.epam.productos.entity.EstadoInventario;
import com.epam.productos.entity.Producto;

public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static Producto toEntity(ProductoRequest request) {
        Producto producto = new Producto();
        producto.setNombre(request.nombre().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        return producto;
    }

    public static ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                EstadoInventario.desde(producto.getStock())
        );
    }
}
