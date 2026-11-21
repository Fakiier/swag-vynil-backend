package com.swagvynil.swag_vynil_backend.repository;

import com.swagvynil.swag_vynil_backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}