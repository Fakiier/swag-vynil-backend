package com.swagvynil.swag_vynil_backend.dto;

import com.swagvynil.swag_vynil_backend.model.Usuario;

import java.time.LocalDate;

public record UsuarioResponse(
        Long id, String run, String nombre, String apellidos, String correo,
        String rol, String region, String comuna, String direccion, LocalDate fechaNacimiento
) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(),
                u.getCorreo(), u.getRol().name(), u.getRegion(), u.getComuna(), u.getDireccion(),
                u.getFechaNacimiento());
    }
}