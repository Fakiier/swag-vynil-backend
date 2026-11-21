package com.swagvynil.swag_vynil_backend.controller;

import com.swagvynil.swag_vynil_backend.dto.UsuarioRequest;
import com.swagvynil.swag_vynil_backend.dto.UsuarioResponse;
import com.swagvynil.swag_vynil_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "CRUD de usuarios (solo Administrador)")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, Authentication auth) {
        service.eliminar(id, auth.getName());
    }
}