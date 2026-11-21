package com.swagvynil.swag_vynil_backend.dto;

import jakarta.validation.constraints.*;

public record ProductoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 1000) String descripcion,
        @NotNull @Positive Integer precio,
        @Size(max = 255) String imagen,
        @Min(0) @Max(100) Integer descuento,
        @Min(0) Integer stock,
        @Min(0) Integer stockCritico,
        @NotNull Long categoriaId
) {}