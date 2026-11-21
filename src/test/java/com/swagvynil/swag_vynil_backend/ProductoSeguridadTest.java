package com.swagvynil.swag_vynil_backend;

import com.swagvynil.swag_vynil_backend.model.Categoria;
import com.swagvynil.swag_vynil_backend.model.Rol;
import com.swagvynil.swag_vynil_backend.repository.CategoriaRepository;
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

// Prueba quién puede leer y quién puede crear productos, y el cálculo del precio con descuento
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductoSeguridadTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @Autowired
    CategoriaRepository categoriaRepo;

    // JSON de un producto de 20000 con 10% de descuento
    private String cuerpo(Long categoriaId) {
        return "{\"nombre\":\"Test-Disco\",\"descripcion\":\"Prueba\",\"precio\":20000,"
                + "\"imagen\":\"/images/test.webp\",\"descuento\":10,\"stock\":5,\"stockCritico\":2,"
                + "\"categoriaId\":" + categoriaId + "}";
    }

    private Categoria crearCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        return categoriaRepo.save(c);
    }

    @Test
    void listarSinToken_devuelve200() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void crearSinToken_devuelve401() throws Exception {
        Categoria c = crearCategoria("Test-Seg-1");

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(c.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearComoVendedor_devuelve403() throws Exception {
        Categoria c = crearCategoria("Test-Seg-2");

        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", TestTokens.de(jwtService, Rol.VENDEDOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(c.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearComoAdministrador_devuelve201ConPrecioFinal() throws Exception {
        Categoria c = crearCategoria("Test-Seg-3");

        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", TestTokens.de(jwtService, Rol.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(c.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precio").value(18000))
                .andExpect(jsonPath("$.precioOriginal").value(20000))
                .andExpect(jsonPath("$.descuento").value(10))
                .andExpect(jsonPath("$.categoria").value("Test-Seg-3"));
    }

    @Test
    void crearConCategoriaInexistente_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", TestTokens.de(jwtService, Rol.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(999999L)))
                .andExpect(status().isBadRequest());
    }
}