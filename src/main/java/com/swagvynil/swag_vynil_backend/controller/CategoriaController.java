package com.swagvynil.swag_vynil_backend.controller;

import com.swagvynil.swag_vynil_backend.dto.CategoriaRequest;
import com.swagvynil.swag_vynil_backend.dto.CategoriaResponse;
import com.swagvynil.swag_vynil_backend.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "CRUD de categorías (lectura pública, escritura solo Administrador)")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar categorías con cantidad de productos e imagen")
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría por id")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoría (Administrador)")
    @SecurityRequirement(name = "bearerAuth")
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría (Administrador)")
    @SecurityRequirement(name = "bearerAuth")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar categoría (Administrador)")
    @SecurityRequirement(name = "bearerAuth")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}