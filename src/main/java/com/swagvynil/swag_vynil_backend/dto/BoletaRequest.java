package com.swagvynil.swag_vynil_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BoletaRequest(
        @NotEmpty @Valid List<Item> items,
        @NotBlank String calle,
        String departamento,
        @NotBlank String region,
        @NotBlank String comuna,
        String indicaciones,
        String metodoPago,
        Boolean pagoRechazado
) {
    public record Item(
            @NotNull Long productoId,
            @NotNull @Min(1) Integer cantidad
    ) {}
}