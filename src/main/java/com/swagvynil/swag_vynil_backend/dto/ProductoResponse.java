package com.swagvynil.swag_vynil_backend.dto;

import com.swagvynil.swag_vynil_backend.model.Producto;

// Misma forma que usa el front: precio = precio final, precioOriginal solo si hay oferta
public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        int precio,
        Integer precioOriginal,
        int descuento,
        String imagen,
        String categoria,
        Long categoriaId,
        int stock,
        int stockCritico
) {
    public static ProductoResponse de(Producto p) {
        int desc = p.getDescuento() == null ? 0 : p.getDescuento();
        return new ProductoResponse(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.precioFinal(),
                desc > 0 ? p.getPrecio() : null,
                desc,
                p.getImagen(),
                p.getCategoria().getNombre(),
                p.getCategoria().getId(),
                p.getStock() == null ? 0 : p.getStock(),
                p.getStockCritico() == null ? 5 : p.getStockCritico()
        );
    }
}