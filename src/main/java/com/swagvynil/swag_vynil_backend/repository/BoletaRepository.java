package com.swagvynil.swag_vynil_backend.repository;

import com.swagvynil.swag_vynil_backend.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Boleta> findByUsuarioCorreoOrderByFechaDesc(String correo);
}