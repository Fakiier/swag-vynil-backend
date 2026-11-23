package com.swagvynil.swag_vynil_backend;

import com.jayway.jsonpath.JsonPath;
import com.swagvynil.swag_vynil_backend.model.Categoria;
import com.swagvynil.swag_vynil_backend.model.Producto;
import com.swagvynil.swag_vynil_backend.model.Rol;
import com.swagvynil.swag_vynil_backend.model.Usuario;
import com.swagvynil.swag_vynil_backend.repository.CategoriaRepository;
import com.swagvynil.swag_vynil_backend.repository.ProductoRepository;
import com.swagvynil.swag_vynil_backend.repository.UsuarioRepository;
import com.swagvynil.swag_vynil_backend.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Lógica de negocio de las boletas (precio, stock, estado) y quién puede ver cada boleta
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BoletaIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarioRepo;
    @Autowired ProductoRepository productoRepo;
    @Autowired CategoriaRepository categoriaRepo;

    // ---------- utilidades ----------

    // Producto de 20000 con 10% de descuento (precio final 18000) y 5 unidades de stock
    private Producto crearProducto() {
        Categoria categoria = categoriaRepo.findAll().get(0);
        Producto p = new Producto();
        p.setNombre("Test-Disco");
        p.setDescripcion("Prueba");
        p.setPrecio(20000);
        p.setDescuento(10);
        p.setStock(5);
        p.setStockCritico(2);
        p.setCategoria(categoria);
        return productoRepo.save(p);
    }

    private Usuario crearCliente(String run, String correo) {
        Usuario u = new Usuario();
        u.setRun(run);
        u.setNombre("Cliente");
        u.setApellidos("Prueba");
        u.setCorreo(correo);
        u.setContrasena("no-se-usa");
        u.setRol(Rol.CLIENTE);
        return usuarioRepo.save(u);
    }

    private String token(Usuario u) {
        return "Bearer " + jwtService.generarToken(u);
    }

    private Usuario admin() {
        return usuarioRepo.findByCorreo("admin@duoc.cl").orElseThrow();
    }

    private Usuario vendedor() {
        return usuarioRepo.findByCorreo("vendedor@duoc.cl").orElseThrow();
    }

    private String cuerpo(Long productoId, int cantidad, boolean pagoRechazado) {
        return """
            {"items":[{"productoId":%d,"cantidad":%d}],
             "calle":"Calle 1","region":"Región Metropolitana de Santiago","comuna":"Santiago",
             "pagoRechazado":%b}
            """.formatted(productoId, cantidad, pagoRechazado);
    }

    // crea una boleta como el usuario indicado y devuelve su id
    private int comprar(Usuario comprador, Long productoId, int cantidad) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", token(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(productoId, cantidad, false)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).intValue();
    }

    // ---------- lógica de negocio ----------

    @Test
    @DisplayName("Crear boleta: el total usa el precio con descuento y se descuenta el stock")
    void crearBoletaCalculaTotalYDescuentaStock() throws Exception {
        Producto p = crearProducto();
        Usuario cliente = crearCliente("90000001", "bol1@gmail.com");

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(p.getId(), 2, false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.total").value(36000))
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(18000))
                .andExpect(jsonPath("$.detalles[0].subtotal").value(36000));

        assertEquals(3, productoRepo.findById(p.getId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("Crear boleta: un pago rechazado queda FALLIDA y no toca el stock")
    void pagoRechazadoNoDescuentaStock() throws Exception {
        Producto p = crearProducto();
        Usuario cliente = crearCliente("90000002", "bol2@gmail.com");

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(p.getId(), 2, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("FALLIDA"));

        assertEquals(5, productoRepo.findById(p.getId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("Crear boleta: sin stock suficiente responde 409 y no cambia el stock")
    void stockInsuficiente() throws Exception {
        Producto p = crearProducto();
        Usuario cliente = crearCliente("90000003", "bol3@gmail.com");

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(p.getId(), 99, false)))
                .andExpect(status().isConflict());

        assertEquals(5, productoRepo.findById(p.getId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("Crear boleta: un producto inexistente responde 400")
    void productoInexistente() throws Exception {
        Usuario cliente = crearCliente("90000004", "bol4@gmail.com");

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(999999L, 1, false)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Crear boleta: sin productos responde 400")
    void boletaSinProductos() throws Exception {
        Usuario cliente = crearCliente("90000005", "bol5@gmail.com");

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"items":[],"calle":"Calle 1","region":"R","comuna":"C"}
                            """))
                .andExpect(status().isBadRequest());
    }

    // ---------- seguridad ----------

    @Test
    @DisplayName("Seguridad: comprar sin sesión responde 401")
    void comprarSinToken() throws Exception {
        Producto p = crearProducto();

        mockMvc.perform(post("/api/v1/boletas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(p.getId(), 1, false)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Seguridad: un Cliente no puede listar todas las boletas (403)")
    void clienteNoListaTodas() throws Exception {
        Usuario cliente = crearCliente("90000006", "bol6@gmail.com");

        mockMvc.perform(get("/api/v1/boletas").header("Authorization", token(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seguridad: el Vendedor puede listar las boletas (200)")
    void vendedorListaBoletas() throws Exception {
        mockMvc.perform(get("/api/v1/boletas").header("Authorization", token(vendedor())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Seguridad: un Cliente solo ve sus propias boletas")
    void clienteSoloVeSusBoletas() throws Exception {
        Producto p = crearProducto();
        Usuario dueno = crearCliente("90000007", "dueno@gmail.com");
        Usuario otro = crearCliente("90000008", "otro@gmail.com");
        int id = comprar(dueno, p.getId(), 1);

        mockMvc.perform(get("/api/v1/boletas/" + id).header("Authorization", token(dueno)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/boletas/" + id).header("Authorization", token(otro)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seguridad: el Vendedor puede ver el detalle de cualquier boleta")
    void vendedorVeDetalle() throws Exception {
        Producto p = crearProducto();
        Usuario cliente = crearCliente("90000009", "bol9@gmail.com");
        int id = comprar(cliente, p.getId(), 1);

        mockMvc.perform(get("/api/v1/boletas/" + id).header("Authorization", token(vendedor())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("bol9@gmail.com"));
    }

    @Test
    @DisplayName("Boleta inexistente responde 404")
    void boletaInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/boletas/999999").header("Authorization", token(admin())))
                .andExpect(status().isNotFound());
    }

    // ---------- consultas ----------

    @Test
    @DisplayName("Mis boletas: devuelve solo las compras del usuario con sesión")
    void misBoletas() throws Exception {
        Producto p = crearProducto();
        Usuario cliente = crearCliente("90000010", "bol10@gmail.com");
        Usuario otro = crearCliente("90000011", "bol11@gmail.com");
        comprar(cliente, p.getId(), 1);
        comprar(otro, p.getId(), 1);

        mockMvc.perform(get("/api/v1/boletas/mis-boletas").header("Authorization", token(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].correo").value("bol10@gmail.com"));
    }

    @Test
    @DisplayName("Historial: el Administrador ve las boletas de un usuario, el Vendedor no (403)")
    void historialPorUsuario() throws Exception {
        Producto p = crearProducto();
        Usuario cliente = crearCliente("90000012", "bol12@gmail.com");
        comprar(cliente, p.getId(), 1);

        mockMvc.perform(get("/api/v1/boletas/usuario/" + cliente.getId())
                        .header("Authorization", token(admin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/v1/boletas/usuario/" + cliente.getId())
                        .header("Authorization", token(vendedor())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Crear boleta: si no se indica pagoRechazado, el pago se considera exitoso")
    void sinPagoRechazadoEsCompletada() throws Exception {
        Producto p = crearProducto();
        Usuario cliente = crearCliente("90000013", "bol13@gmail.com");

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"items":[{"productoId":%d,"cantidad":1}],
                             "calle":"Calle 1","region":"R","comuna":"C"}
                            """.formatted(p.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }
    @Test
@DisplayName("Reportes: el vendedor puede consultar las estadísticas")
void vendedorConsultaReportes() throws Exception {
    mockMvc.perform(get("/api/v1/boletas/reportes")
                    .header("Authorization", token(vendedor())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ventasTotales").exists())
            .andExpect(jsonPath("$.cantidadBoletas").exists())
            .andExpect(jsonPath("$.ticketPromedio").exists())
            .andExpect(jsonPath("$.productosMasVendidos").isArray());
}

@Test
@DisplayName("Reportes: el administrador puede consultar las estadísticas")
void adminConsultaReportes() throws Exception {
    mockMvc.perform(get("/api/v1/boletas/reportes")
                    .header("Authorization", token(admin())))
            .andExpect(status().isOk());
}

@Test
@DisplayName("Reportes: un cliente no puede consultar las estadísticas")
void clienteNoConsultaReportes() throws Exception {
    Usuario cliente = crearCliente("90000014", "reportes@gmail.com");

    mockMvc.perform(get("/api/v1/boletas/reportes")
                    .header("Authorization", token(cliente)))
            .andExpect(status().isForbidden());
}

}