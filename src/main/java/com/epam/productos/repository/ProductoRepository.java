package com.epam.productos.repository;

import com.epam.productos.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    Optional<Producto> findByIdAndEliminadoFalse(UUID id);

    List<Producto> findByEliminadoFalseOrderByNombreAsc();

    Page<Producto> findByEliminadoFalse(Pageable pageable);

    List<Producto> findByEliminadoFalseAndNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    Page<Producto> findByEliminadoFalseAndNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
