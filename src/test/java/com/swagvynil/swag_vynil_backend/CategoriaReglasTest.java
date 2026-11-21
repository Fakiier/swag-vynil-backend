package com.swagvynil.swag_vynil_backend;

import com.swagvynil.swag_vynil_backend.model.Categoria;
import com.swagvynil.swag_vynil_backend.model.Producto;
import com.swagvynil.swag_vynil_backend.model.Rol;
import com.swagvynil.swag_vynil_backend.repository.CategoriaRepository;
import com.swagvynil.swag_vynil_backend.repository.ProductoRepository;
import com.swagvynil.swag_vynil_backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba las reglas de negocio de categorías: nombre repetido, borrado y actualización
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoriaReglasTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @Autowired
    CategoriaRepository categoriaRepo;

    @Autowired
    ProductoRepository productoRepo;

    private Categoria crearCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        return categoriaRepo.save(c);
    }

    @Test
    void crearConNombreRepetido_devuelve409() throws Exception {
        crearCategoria("Test-Repetida");

        // se escribe distinto en mayúsculas: igual cuenta como repetido
        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", TestTokens.de(jwtService, Rol.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"TEST-REPETIDA\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarCategoria_devuelve200() throws Exception {
        Categoria c = crearCategoria("Test-Actualizar");

        mockMvc.perform(put("/api/v1/categorias/" + c.getId())
                        .header("Authorization", TestTokens.de(jwtService, Rol.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Test-Actualizar\",\"descripcion\":\"Nueva descripción\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Nueva descripción"));
    }

    @Test
    void eliminarCategoriaConProductos_devuelve409() throws Exception {
        Categoria c = crearCategoria("Test-ConProductos");
        Producto p = new Producto();
        p.setNombre("Test-Disco");
        p.setPrecio(10000);
        p.setCategoria(c);
        productoRepo.save(p);

        mockMvc.perform(delete("/api/v1/categorias/" + c.getId())
                        .header("Authorization", TestTokens.de(jwtService, Rol.ADMINISTRADOR)))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarCategoriaSinProductos_devuelve204() throws Exception {
        Categoria c = crearCategoria("Test-Vacia");

        mockMvc.perform(delete("/api/v1/categorias/" + c.getId())
                        .header("Authorization", TestTokens.de(jwtService, Rol.ADMINISTRADOR)))
                .andExpect(status().isNoContent());
    }
}