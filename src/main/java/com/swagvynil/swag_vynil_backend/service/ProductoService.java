package com.swagvynil.swag_vynil_backend.service;

import com.swagvynil.swag_vynil_backend.dto.ProductoRequest;
import com.swagvynil.swag_vynil_backend.dto.ProductoResponse;
import com.swagvynil.swag_vynil_backend.model.Producto;
import com.swagvynil.swag_vynil_backend.repository.CategoriaRepository;
import com.swagvynil.swag_vynil_backend.repository.ProductoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repo;
    private final CategoriaRepository categoriaRepo;

    public ProductoService(ProductoRepository repo, CategoriaRepository categoriaRepo) {
        this.repo = repo;
        this.categoriaRepo = categoriaRepo;
    }

    public List<ProductoResponse> listar(String categoria) {
        List<Producto> lista = (categoria == null || categoria.isBlank())
                ? repo.findAll()
                : repo.findByCategoriaNombreIgnoreCase(categoria.trim());
        return lista.stream().map(ProductoResponse::de).toList();
    }

    public List<ProductoResponse> ofertas() {
        return repo.findByDescuentoGreaterThan(0).stream().map(ProductoResponse::de).toList();
    }

    public ProductoResponse obtener(Long id) {
        return ProductoResponse.de(buscar(id));
    }

    public ProductoResponse crear(ProductoRequest r) {
        Producto p = new Producto();
        copiar(p, r);
        return ProductoResponse.de(repo.save(p));
    }

    public ProductoResponse actualizar(Long id, ProductoRequest r) {
        Producto p = buscar(id);
        copiar(p, r);
        return ProductoResponse.de(repo.save(p));
    }

    public void eliminar(Long id) {
        Producto p = buscar(id);
        try {
            repo.delete(p);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto está en boletas y no se puede eliminar");
        }
    }

    private Producto buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private void copiar(Producto p, ProductoRequest r) {
        p.setNombre(r.nombre().trim());
        p.setDescripcion(r.descripcion());
        p.setPrecio(r.precio());
        p.setImagen(r.imagen());
        p.setDescuento(r.descuento() == null ? 0 : r.descuento());
        p.setStock(r.stock() == null ? 0 : r.stock());
        p.setStockCritico(r.stockCritico() == null ? 5 : r.stockCritico());
        p.setCategoria(categoriaRepo.findById(r.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría no existe")));
    }
}