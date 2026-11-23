package com.swagvynil.swag_vynil_backend;

import com.swagvynil.swag_vynil_backend.model.Rol;
import com.swagvynil.swag_vynil_backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba quién puede leer y quién puede crear categorías (cada prueba deshace sus cambios)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoriaSeguridadTest {

    private static final String CUERPO = "{\"nombre\":\"Test-Jazz\",\"descripcion\":\"Discos de jazz\"}";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @Test
    void listarSinToken_devuelve200() throws Exception {
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk());
    }

    @Test
    void crearSinToken_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearComoVendedor_devuelve403() throws Exception {
        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", TestTokens.de(jwtService, Rol.VENDEDOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearComoCliente_devuelve403() throws Exception {
        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", TestTokens.de(jwtService, Rol.CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearComoAdministrador_devuelve201() throws Exception {
        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", TestTokens.de(jwtService, Rol.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Test-Jazz"))
                .andExpect(jsonPath("$.cantidad").value(0));
    }
}