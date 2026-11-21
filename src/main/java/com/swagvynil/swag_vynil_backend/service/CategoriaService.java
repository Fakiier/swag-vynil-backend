package com.swagvynil.swag_vynil_backend.service;

import com.swagvynil.swag_vynil_backend.dto.CategoriaRequest;
import com.swagvynil.swag_vynil_backend.dto.CategoriaResponse;
import com.swagvynil.swag_vynil_backend.model.Categoria;
import com.swagvynil.swag_vynil_backend.model.Producto;
import com.swagvynil.swag_vynil_backend.repository.CategoriaRepository;
import com.swagvynil.swag_vynil_backend.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repo;
    private final ProductoRepository productoRepo;

    public CategoriaService(CategoriaRepository repo, ProductoRepository productoRepo) {
        this.repo = repo;
        this.productoRepo = productoRepo;
    }

    public List<CategoriaResponse> listar() {
        return repo.findAll().stream().map(this::aRespuesta).toList();
    }

    public CategoriaResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public CategoriaResponse crear(CategoriaRequest r) {
        String nombre = r.nombre().trim();
        if (repo.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        Categoria c = new Categoria();
        copiar(c, r);
        return aRespuesta(repo.save(c));
    }

    public CategoriaResponse actualizar(Long id, CategoriaRequest r) {
        Categoria c = buscar(id);
        String nombre = r.nombre().trim();
        if (repo.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        copiar(c, r);
        return aRespuesta(repo.save(c));
    }

    public void eliminar(Long id) {
        Categoria c = buscar(id);
        if (productoRepo.existsByCategoriaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría tiene productos asociados");
        }
        repo.delete(c);
    }

    private Categoria buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    private void copiar(Categoria c, CategoriaRequest r) {
        c.setNombre(r.nombre().trim());
        c.setDescripcion(r.descripcion() == null ? null : r.descripcion().trim());
    }

    // Agrega la cantidad de productos y la imagen del primero (lo que muestra el front)
    private CategoriaResponse aRespuesta(Categoria c) {
        long cantidad = productoRepo.countByCategoriaId(c.getId());
        String imagen = productoRepo.findFirstByCategoriaIdOrderByIdAsc(c.getId())
                .map(Producto::getImagen)
                .orElse(null);
        return CategoriaResponse.de(c, cantidad, imagen);
    }
}