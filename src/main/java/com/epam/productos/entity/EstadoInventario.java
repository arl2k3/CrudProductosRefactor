package com.epam.productos.entity;

public enum EstadoInventario {
    DISPONIBLE,
    AGOTADO;

    public static EstadoInventario desde(int stock) {
        return stock > 0 ? DISPONIBLE : AGOTADO;
    }
}
