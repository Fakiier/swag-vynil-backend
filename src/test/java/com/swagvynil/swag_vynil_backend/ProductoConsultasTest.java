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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba las consultas de productos (ofertas, filtro por categoría, detalle) y el borrado
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductoConsultasTest {

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

    private Producto crearProducto(String nombre, int descuento, Categoria categoria) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(10000);
        p.setDescuento(descuento);
        p.setCategoria(categoria);
        return productoRepo.save(p);
    }

    @Test
    void ofertas_soloIncluyeProductosConDescuento() throws Exception {
        Categoria c = crearCategoria("Test-Ofertas");
        crearProducto("Test-ConOferta", 20, c);
        crearProducto("Test-SinOferta", 0, c);

        mockMvc.perform(get("/api/v1/productos/ofertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'Test-ConOferta')]").exists())
                .andExpect(jsonPath("$[?(@.nombre == 'Test-SinOferta')]").doesNotExist());
    }

    @Test
    void filtrarPorCategoria_devuelveSoloLosDeEsaCategoria() throws Exception {
        Categoria a = crearCategoria("Test-Filtro-A");
        Categoria b = crearCategoria("Test-Filtro-B");
        crearProducto("Test-De-A", 0, a);
        crearProducto("Test-De-B", 0, b);

        mockMvc.perform(get("/api/v1/productos").param("categoria", "Test-Filtro-A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Test-De-A"))
                .andExpect(jsonPath("$[0].categoria").value("Test-Filtro-A"));
    }

    @Test
    void obtenerProductoInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/v1/productos/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarProducto_devuelve204YLuegoNoExiste() throws Exception {
        Categoria c = crearCategoria("Test-Eliminar");
        Producto p = crearProducto("Test-Borrar", 0, c);
        String token = TestTokens.de(jwtService, Rol.ADMINISTRADOR);

        mockMvc.perform(delete("/api/v1/productos/" + p.getId()).header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/productos/" + p.getId()))
                .andExpect(status().isNotFound());
    }
}