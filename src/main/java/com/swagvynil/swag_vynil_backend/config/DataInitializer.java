package com.swagvynil.swag_vynil_backend.config;

import com.swagvynil.swag_vynil_backend.model.Rol;
import com.swagvynil.swag_vynil_backend.model.Usuario;
import com.swagvynil.swag_vynil_backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("221231239", "Javier", "Romero", "admin@duoc.cl", "admin123", Rol.ADMINISTRADOR);
        crearSiNoExiste("211231239", "Vendedor", "Prueba", "vendedor@duoc.cl", "vend1234", Rol.VENDEDOR);
    }

    private void crearSiNoExiste(String run, String nombre, String apellidos,
                                 String correo, String clave, Rol rol) {
        if (usuarioRepository.existsByCorreo(correo)) return;

        Usuario u = new Usuario();
        u.setRun(run);
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setContrasena(passwordEncoder.encode(clave));
        u.setRol(rol);
        usuarioRepository.save(u);
    }
}