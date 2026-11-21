package com.swagvynil.swag_vynil_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String correo,
        @NotBlank String contrasena
) {}