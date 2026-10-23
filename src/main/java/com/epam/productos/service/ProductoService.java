package com.epam.productos.service;

import com.epam.productos.dto.ActualizarPrecioRequest;
import com.epam.productos.dto.CatalogoResponse;
import com.epam.productos.dto.ProductoRequest;
import com.epam.productos.dto.ProductoResponse;

import java.util.UUID;

public interface ProductoService {

    ProductoResponse registrar(ProductoRequest request);

    CatalogoResponse consultarCatalogo(Integer page, Integer size, String nombre);

    ProductoResponse obtener(UUID id);

    ProductoResponse actualizarPrecio(UUID id, ActualizarPrecioRequest request);

    void eliminar(UUID id);
}
