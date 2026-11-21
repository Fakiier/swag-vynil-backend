package com.swagvynil.swag_vynil_backend.controller;

import com.swagvynil.swag_vynil_backend.dto.AuthResponse;
import com.swagvynil.swag_vynil_backend.dto.LoginRequest;
import com.swagvynil.swag_vynil_backend.dto.RegisterRequest;
import com.swagvynil.swag_vynil_backend.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @SecurityRequirement(name = "bearerAuth")
    public AuthResponse refresh(Authentication authentication) {
        return authService.renovar(authentication.getName());
    }

}