package com.swagvynil.swag_vynil_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank String run,
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 4, max = 10) String contrasena,
        String region,
        String comuna,
        String direccion,
        LocalDate fechaNacimiento
) {}