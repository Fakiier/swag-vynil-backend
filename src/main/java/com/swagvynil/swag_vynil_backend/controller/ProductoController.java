package com.swagvynil.swag_vynil_backend.controller;

import com.swagvynil.swag_vynil_backend.dto.ProductoRequest;
import com.swagvynil.swag_vynil_backend.dto.ProductoResponse;
import com.swagvynil.swag_vynil_backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "CRUD de productos (lectura pública, escritura solo Administrador)")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar productos, opcionalmente filtrados por nombre de categoría")
    public List<ProductoResponse> listar(@RequestParam(required = false) String categoria) {
        return service.listar(categoria);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Listar productos con descuento")
    public List<ProductoResponse> ofertas() {
        return service.ofertas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por id")
    public ProductoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear producto (Administrador)")
    @SecurityRequirement(name = "bearerAuth")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto (Administrador)")
    @SecurityRequirement(name = "bearerAuth")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar producto (Administrador)")
    @SecurityRequirement(name = "bearerAuth")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}