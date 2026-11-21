package com.swagvynil.swag_vynil_backend.repository;

import com.swagvynil.swag_vynil_backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    long countByCategoriaId(Long categoriaId);

    boolean existsByCategoriaId(Long categoriaId);

    Optional<Producto> findFirstByCategoriaIdOrderByIdAsc(Long categoriaId);
}
