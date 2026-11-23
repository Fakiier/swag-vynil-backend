package com.swagvynil.swag_vynil_backend.dto;

import com.swagvynil.swag_vynil_backend.model.Categoria;

public record CategoriaResponse(
        Long id, String nombre, String descripcion, long cantidad, String imagen
) {
    public static CategoriaResponse de(Categoria c, long cantidad, String imagen) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), cantidad, imagen);
    }
}