package com.swagvynil.swag_vynil_backend.security;

import com.swagvynil.swag_vynil_backend.model.Rol;
import com.swagvynil.swag_vynil_backend.model.Usuario;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "clave-de-pruebas-de-al-menos-32-caracteres-aaaa";
    private static final String OTRA_SECRET = "otra-clave-distinta-de-al-menos-32-caracteres-bbbb";

    private final JwtService jwt = new JwtService(SECRET, 3_600_000);

    private Usuario usuario(Rol rol) {
        Usuario u = new Usuario();
        u.setCorreo("test@duoc.cl");
        u.setRol(rol);
        return u;
    }

    @Test
    @DisplayName("Generación: el token tiene las tres partes de un JWT (cabecera.contenido.firma)")
    void tokenTieneTresPartes() {
        String token = jwt.generarToken(usuario(Rol.CLIENTE));
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Generación: el token contiene el correo y el rol del usuario")
    void tokenContieneCorreoYRol() {
        String token = jwt.generarToken(usuario(Rol.ADMINISTRADOR));
        Claims claims = jwt.leerClaims(token);

        assertEquals("test@duoc.cl", claims.getSubject());
        assertEquals("ADMINISTRADOR", claims.get("rol", String.class));
        assertEquals("test@duoc.cl", jwt.obtenerCorreo(token));
    }

    @Test
    @DisplayName("Generación: cada rol queda reflejado en su propio token")
    void cadaRolQuedaEnSuToken() {
        String tokenVendedor = jwt.generarToken(usuario(Rol.VENDEDOR));
        String tokenCliente = jwt.generarToken(usuario(Rol.CLIENTE));

        assertEquals("VENDEDOR", jwt.leerClaims(tokenVendedor).get("rol", String.class));
        assertEquals("CLIENTE", jwt.leerClaims(tokenCliente).get("rol", String.class));
    }

    @Test
    @DisplayName("Verificación: un token recién generado es válido")
    void tokenRecienGeneradoEsValido() {
        assertTrue(jwt.esValido(jwt.generarToken(usuario(Rol.CLIENTE))));
    }

    @Test
    @DisplayName("Verificación: un token con la firma alterada es rechazado")
    void tokenAlteradoEsRechazado() {
        String[] partes = jwt.generarToken(usuario(Rol.CLIENTE)).split("\\.");
        char primero = partes[2].charAt(0);
        char distinto = primero == 'A' ? 'B' : 'A';
        String adulterado = partes[0] + "." + partes[1] + "." + distinto + partes[2].substring(1);

        assertFalse(jwt.esValido(adulterado));
    }

    @Test
    @DisplayName("Verificación: un token firmado con otra clave es rechazado")
    void tokenConOtraClaveEsRechazado() {
        JwtService otroServidor = new JwtService(OTRA_SECRET, 3_600_000);
        String tokenAjeno = otroServidor.generarToken(usuario(Rol.ADMINISTRADOR));

        assertFalse(jwt.esValido(tokenAjeno));
    }

    @Test
    @DisplayName("Verificación: un token vencido es rechazado")
    void tokenVencidoEsRechazado() {
        // expiración negativa: el token nace ya vencido
        JwtService conExpiracionPasada = new JwtService(SECRET, -1_000);
        String vencido = conExpiracionPasada.generarToken(usuario(Rol.CLIENTE));

        assertFalse(jwt.esValido(vencido));
    }

    @Test
    @DisplayName("Verificación: texto que no es un JWT es rechazado")
    void textoBasuraEsRechazado() {
        assertFalse(jwt.esValido("esto-no-es-un-token"));
        assertFalse(jwt.esValido(""));
    }

    @Test
    @DisplayName("Seguridad: una clave de menos de 256 bits no es aceptada")
    void claveCortaNoEsAceptada() {
        assertThrows(RuntimeException.class, () -> new JwtService("corta", 1_000));
    }
}