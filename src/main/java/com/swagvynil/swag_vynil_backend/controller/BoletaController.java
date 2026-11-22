package com.swagvynil.swag_vynil_backend.controller;

import com.swagvynil.swag_vynil_backend.dto.BoletaRequest;
import com.swagvynil.swag_vynil_backend.dto.BoletaResponse;
import com.swagvynil.swag_vynil_backend.service.BoletaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Compras: crear boleta, listar y consultar")
@SecurityRequirement(name = "bearerAuth")
public class BoletaController {

    private final BoletaService service;

    public BoletaController(BoletaService service) {
        this.service = service;
    }

    // Cualquier usuario con sesión puede comprar
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoletaResponse crear(@Valid @RequestBody BoletaRequest request, Authentication auth) {
        return service.crear(auth.getName(), request);
    }

    // Administrador y Vendedor
    @GetMapping
    public List<BoletaResponse> listar() {
        return service.listar();
    }

    // Las compras del usuario con sesión
    @GetMapping("/mis-boletas")
    public List<BoletaResponse> mias(Authentication auth) {
        return service.mias(auth.getName());
    }

    // Historial de un usuario (solo Administrador)
    @GetMapping("/usuario/{usuarioId}")
    public List<BoletaResponse> porUsuario(@PathVariable Long usuarioId) {
        return service.porUsuario(usuarioId);
    }

    // Detalle: el personal ve cualquiera; un Cliente solo las suyas
    @GetMapping("/{id}")
    public BoletaResponse obtener(@PathVariable Long id, Authentication auth) {
        boolean esPersonal = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR") || a.getAuthority().equals("ROLE_VENDEDOR"));
        return service.obtener(id, auth.getName(), esPersonal);
    }
}