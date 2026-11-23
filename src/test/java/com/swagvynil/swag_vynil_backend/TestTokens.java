package com.swagvynil.swag_vynil_backend;

import com.swagvynil.swag_vynil_backend.model.Rol;
import com.swagvynil.swag_vynil_backend.model.Usuario;
import com.swagvynil.swag_vynil_backend.security.JwtService;

// Genera un token JWT real para el rol indicado, para usarlo en las pruebas
class TestTokens {

    static String de(JwtService jwtService, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(rol.name().toLowerCase() + "@test.cl");
        usuario.setRol(rol);
        return "Bearer " + jwtService.generarToken(usuario);
    }
}