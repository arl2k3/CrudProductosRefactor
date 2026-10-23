package com.epam.productos.service;

import com.epam.productos.dto.ActualizarPrecioRequest;
import com.epam.productos.dto.CatalogoResponse;
import com.epam.productos.dto.ProductoRequest;
import com.epam.productos.dto.ProductoResponse;
import com.epam.productos.entity.Producto;
import com.epam.productos.exception.InventarioInvalidoException;
import com.epam.productos.exception.PaginacionInvalidaException;
import com.epam.productos.exception.PrecioInvalidoException;
import com.epam.productos.exception.ProductoNoEncontradoException;
import com.epam.productos.mapper.ProductoMapper;
import com.epam.productos.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private static final int TAMANO_PAGINA_POR_DEFECTO = 20;

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public ProductoResponse registrar(ProductoRequest request) {
        validarPrecio(request.precio());
        validarInventario(request.stock());
        Producto producto = productoRepository.save(ProductoMapper.toEntity(request));
        return ProductoMapper.toResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogoResponse consultarCatalogo(Integer page, Integer size, String nombre) {
        String filtro = normalizarFiltro(nombre);
        if (page == null && size == null) {
            List<ProductoResponse> productos = listar(filtro).stream()
                    .map(ProductoMapper::toResponse)
                    .toList();
            return new CatalogoResponse(productos, false, null, null, null, null);
        }

        int numeroPagina = page == null ? 0 : page;
        int tamano = size == null ? TAMANO_PAGINA_POR_DEFECTO : size;
        validarPaginacion(numeroPagina, tamano);

        Pageable pageable = PageRequest.of(numeroPagina, tamano, Sort.by("nombre").ascending());
        Page<Producto> resultado = filtro == null
                ? productoRepository.findByEliminadoFalse(pageable)
                : productoRepository.findByEliminadoFalseAndNombreContainingIgnoreCase(filtro, pageable);
        List<ProductoResponse> productos = resultado.getContent().stream()
                .map(ProductoMapper::toResponse)
                .toList();
        return new CatalogoResponse(
                productos,
                true,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtener(UUID id) {
        return ProductoMapper.toResponse(buscarActivo(id));
    }

    @Override
    public ProductoResponse actualizarPrecio(UUID id, ActualizarPrecioRequest request) {
        validarPrecio(request.precio());
        Producto producto = buscarActivo(id);
        producto.setPrecio(request.precio());
        return ProductoMapper.toResponse(productoRepository.save(producto));
    }

    @Override
    public void eliminar(UUID id) {
        Producto producto = buscarActivo(id);
        producto.setEliminado(true);
        productoRepository.save(producto);
    }

    private List<Producto> listar(String filtro) {
        if (filtro == null) {
            return productoRepository.findByEliminadoFalseOrderByNombreAsc();
        }
        return productoRepository.findByEliminadoFalseAndNombreContainingIgnoreCaseOrderByNombreAsc(filtro);
    }

    private Producto buscarActivo(UUID id) {
        return productoRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));
    }

    private void validarPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PrecioInvalidoException("El precio debe ser mayor que cero");
        }
    }

    private void validarInventario(Integer stock) {
        if (stock == null || stock < 0) {
            throw new InventarioInvalidoException("El stock no puede ser negativo");
        }
    }

    private void validarPaginacion(int page, int size) {
        if (page < 0) {
            throw new PaginacionInvalidaException("page: La página no puede ser negativa");
        }
        if (size < 1) {
            throw new PaginacionInvalidaException("size: El tamaño de página debe ser mayor que cero");
        }
    }

    private String normalizarFiltro(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return nombre.trim();
    }
}
