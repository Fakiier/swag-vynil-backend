package com.swagvynil.swag_vynil_backend.dto;

public record AuthResponse(
        String token,
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        String rol
) {}