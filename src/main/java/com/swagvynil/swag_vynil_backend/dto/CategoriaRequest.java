package com.swagvynil.swag_vynil_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(min = 3, max = 50) String nombre,
        @Size(max = 255) String descripcion
) {
}