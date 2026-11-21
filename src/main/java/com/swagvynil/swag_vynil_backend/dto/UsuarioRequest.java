package com.swagvynil.swag_vynil_backend.dto;

import com.swagvynil.swag_vynil_backend.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UsuarioRequest(
        @NotBlank String run,
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotBlank @Email String correo,
        @Size(min = 4, max = 10) String contrasena,
        @NotNull Rol rol,
        String region,
        String comuna,
        String direccion,
        LocalDate fechaNacimiento
) {}