package com.swagvynil.swag_vynil_backend.service;

import com.swagvynil.swag_vynil_backend.dto.UsuarioRequest;
import com.swagvynil.swag_vynil_backend.dto.UsuarioResponse;
import com.swagvynil.swag_vynil_backend.model.Usuario;
import com.swagvynil.swag_vynil_backend.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public List<UsuarioResponse> listar() {
        return repo.findAll().stream().map(UsuarioResponse::de).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    public UsuarioResponse crear(UsuarioRequest r) {
        if (r.contrasena() == null || r.contrasena().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        if (repo.existsByCorreo(r.correo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (repo.existsByRun(r.run())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUN ya está registrado");
        }
        Usuario u = new Usuario();
        copiar(u, r);
        u.setContrasena(encoder.encode(r.contrasena()));
        return UsuarioResponse.de(repo.save(u));
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest r) {
        Usuario u = buscar(id);

        repo.findByCorreo(r.correo())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
                });

        copiar(u, r);
        if (r.contrasena() != null && !r.contrasena().isBlank()) {
            u.setContrasena(encoder.encode(r.contrasena()));
        }
        return UsuarioResponse.de(repo.save(u));
    }

    public void eliminar(Long id, String correoActual) {
        Usuario u = buscar(id);
        if (u.getCorreo().equals(correoActual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta");
        }
        try {
            repo.delete(u);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario tiene boletas asociadas");
        }
    }

    private Usuario buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void copiar(Usuario u, UsuarioRequest r) {
        u.setRun(r.run());
        u.setNombre(r.nombre());
        u.setApellidos(r.apellidos());
        u.setCorreo(r.correo());
        u.setRol(r.rol());
        u.setRegion(r.region());
        u.setComuna(r.comuna());
        u.setDireccion(r.direccion());
        u.setFechaNacimiento(r.fechaNacimiento());
    }
}