package com.swagvynil.swag_vynil_backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthUsuarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- utilidades ----------

    private String cuerpoRegistro(String run, String correo, String clave) {
        return """
            {"run":"%s","nombre":"Test","apellidos":"Prueba","correo":"%s","contrasena":"%s"}
            """.formatted(run, correo, clave);
    }

    private String cuerpoUsuario(String run, String correo, String rol) {
        return """
            {"run":"%s","nombre":"Nuevo","apellidos":"Usuario","correo":"%s","contrasena":"1234","rol":"%s"}
            """.formatted(run, correo, rol);
    }

    private String login(String correo, String clave) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"%s\",\"contrasena\":\"%s\"}".formatted(correo, clave)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String tokenAdmin() throws Exception {
        return JsonPath.read(login("admin@duoc.cl", "admin123"), "$.token");
    }

    private String tokenVendedor() throws Exception {
        return JsonPath.read(login("vendedor@duoc.cl", "vend1234"), "$.token");
    }

    private String tokenCliente(String run, String correo) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(run, correo, "1234")))
                .andExpect(status().isCreated());
        return JsonPath.read(login(correo, "1234"), "$.token");
    }

    // ---------- registro ----------

    @Test
    @DisplayName("Registro: crea un usuario con rol CLIENTE y entrega un token")
    void registroCreaCliente() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro("111111111", "nuevo1@gmail.com", "1234")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Registro: un correo repetido responde 409")
    void registroCorreoDuplicado() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro("222222222", "repetido@gmail.com", "1234")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro("333333333", "repetido@gmail.com", "1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El correo ya está registrado"));
    }

    @Test
    @DisplayName("Registro: un RUN repetido responde 409")
    void registroRunDuplicado() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro("444444444", "run1@gmail.com", "1234")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro("444444444", "run2@gmail.com", "1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El RUN ya está registrado"));
    }

    @Test
    @DisplayName("Registro: una contraseña fuera de 4 a 10 caracteres responde 400")
    void registroContrasenaInvalida() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro("555555555", "corta@gmail.com", "1")))
                .andExpect(status().isBadRequest());
    }

    // ---------- login ----------

    @Test
    @DisplayName("Login: credenciales correctas entregan un token con el rol")
    void loginCorrecto() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"contrasena\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Login: una contraseña incorrecta responde 401")
    void loginContrasenaIncorrecta() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"contrasena\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- autorización por rol ----------

    @Test
    @DisplayName("Seguridad: sin token no se accede a /usuarios (401)")
    void usuariosSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Seguridad: un token manipulado es rechazado (401)")
    void usuariosTokenManipulado() throws Exception {
        String token = tokenAdmin();
        String adulterado = token.substring(0, token.length() - 3) + "abc";

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Seguridad: un Cliente no puede listar usuarios (403)")
    void usuariosComoCliente() throws Exception {
        String token = tokenCliente("666666666", "cliente1@gmail.com");

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seguridad: un Vendedor no puede listar usuarios (403)")
    void usuariosComoVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seguridad: un Administrador sí puede listar usuarios (200)")
    void usuariosComoAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- CRUD de usuarios ----------

    @Test
    @DisplayName("CRUD: el Administrador crea un usuario con rol VENDEDOR")
    void adminCreaVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoUsuario("777777777", "vend2@duoc.cl", "VENDEDOR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("VENDEDOR"));
    }

    @Test
    @DisplayName("CRUD: un usuario inexistente responde 404")
    void usuarioInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/99999")
                        .header("Authorization", "Bearer " + tokenAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CRUD: el Administrador no puede eliminar su propia cuenta (400)")
    void adminNoSeElimina() throws Exception {
        String respuesta = login("admin@duoc.cl", "admin123");
        String token = JsonPath.read(respuesta, "$.token");
        Integer id = JsonPath.read(respuesta, "$.id");

        mockMvc.perform(delete("/api/v1/usuarios/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}