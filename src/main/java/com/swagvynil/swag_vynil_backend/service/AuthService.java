package com.swagvynil.swag_vynil_backend.service;

import com.swagvynil.swag_vynil_backend.dto.AuthResponse;
import com.swagvynil.swag_vynil_backend.dto.LoginRequest;
import com.swagvynil.swag_vynil_backend.dto.RegisterRequest;
import com.swagvynil.swag_vynil_backend.model.Rol;
import com.swagvynil.swag_vynil_backend.model.Usuario;
import com.swagvynil.swag_vynil_backend.repository.UsuarioRepository;
import com.swagvynil.swag_vynil_backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse registrar(RegisterRequest r) {
        if (usuarioRepository.existsByCorreo(r.correo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (usuarioRepository.existsByRun(r.run())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUN ya está registrado");
        }

        Usuario u = new Usuario();
        u.setRun(r.run());
        u.setNombre(r.nombre());
        u.setApellidos(r.apellidos());
        u.setCorreo(r.correo());
        u.setContrasena(passwordEncoder.encode(r.contrasena()));
        u.setRegion(r.region());
        u.setComuna(r.comuna());
        u.setDireccion(r.direccion());
        u.setFechaNacimiento(r.fechaNacimiento());
        u.setRol(Rol.CLIENTE); // quien se registra solo es Cliente

        usuarioRepository.save(u);
        return respuesta(u);
    }

    public AuthResponse login(LoginRequest r) {
        Usuario u = usuarioRepository.findByCorreo(r.correo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(r.contrasena(), u.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return respuesta(u);
    }

    private AuthResponse respuesta(Usuario u) {
        return new AuthResponse(jwtService.generarToken(u), u.getId(), u.getRun(),
                u.getNombre(), u.getApellidos(), u.getCorreo(), u.getRol().name());
    }
}