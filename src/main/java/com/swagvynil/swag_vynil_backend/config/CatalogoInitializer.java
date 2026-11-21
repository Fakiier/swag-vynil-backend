package com.swagvynil.swag_vynil_backend.config;

import com.swagvynil.swag_vynil_backend.model.Categoria;
import com.swagvynil.swag_vynil_backend.model.Producto;
import com.swagvynil.swag_vynil_backend.repository.CategoriaRepository;
import com.swagvynil.swag_vynil_backend.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Carga el catálogo inicial (los mismos 12 discos que tenía el front). Solo corre si no hay productos.
@Component
public class CatalogoInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepo;
    private final ProductoRepository productoRepo;

    public CatalogoInitializer(CategoriaRepository categoriaRepo, ProductoRepository productoRepo) {
        this.categoriaRepo = categoriaRepo;
        this.productoRepo = productoRepo;
    }

    @Override
    public void run(String... args) {
        if (productoRepo.count() > 0) return;

        crear("Taylor Swift - 1989", "Este álbum icónico incluye grandes éxitos remasterizados en formato vinilo de alta fidelidad. Ideal para coleccionistas y fanáticos de la música pop.", 25990, "/images/TAYLOR1.webp", "Pop", 5);
        crear("Taylor Swift - Red (TV)", "Edición vinilo de Red (Taylor's Version), con sonido remasterizado y empaque especial.", 25990, "/images/taylor 2.webp", "Pop", 20);
        crear("Taylor Swift - Midnights", "El aclamado álbum Midnights, ahora disponible en vinilo de alta calidad.", 25990, "/images/taylor3.webp", "Pop", 0);
        crear("Taylor Swift - Evermore", "Un viaje sonoro íntimo y folk, ahora en formato vinilo coleccionable.", 25990, "/images/tayy1.webp", "Pop", 0);
        crear("Taylor Swift - Showgirl", "The Life of a Showgirl, la más reciente producción de Taylor Swift en vinilo.", 25990, "/images/taylor5.webp", "Pop", 15);
        crear("Taylor Swift - Folklore", "Folklore en formato vinilo, con su estética indie-folk característica.", 25990, "/images/taylor6.webp", "Pop", 0);
        crear("Justin Bieber - Changes", "Changes marca una etapa madura en la carrera de Justin Bieber, disponible en vinilo.", 25990, "/images/FFE9DFB9-862F-4B67-9F3A-658A4D0C8E20.webp", "Urbano", 0);
        crear("Justin Bieber - Swag", "Swag, la producción más reciente de Justin Bieber en formato vinilo de colección.", 25990, "/images/swag.webp", "Urbano", 0);
        crear("Justin Bieber - Purpose", "Purpose, uno de los álbumes más exitosos de Bieber, ahora en vinilo.", 25990, "/images/purpose.webp", "Urbano", 30);
        crear("Justin Bieber - My World", "El álbum debut de Justin Bieber, disponible en formato vinilo de colección.", 25990, "/images/myworld.webp", "Urbano", 0);
        crear("Justin Bieber - Justice", "Justice, con sus éxitos más recientes, ahora en formato vinilo de alta fidelidad.", 25990, "/images/justice.webp", "Urbano", 0);
        crear("Justin Bieber - Believe", "Believe, el álbum que consolidó a Justin Bieber, disponible en vinilo.", 25990, "/images/believe.webp", "Urbano", 10);
    }

    private void crear(String nombre, String descripcion, int precio, String imagen, String categoria, int descuento) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setImagen(imagen);
        p.setDescuento(descuento);
        p.setStock(20);
        p.setStockCritico(5);
        p.setCategoria(categoriaRepo.findByNombreIgnoreCase(categoria).orElseGet(() -> {
            Categoria c = new Categoria();
            c.setNombre(categoria);
            c.setDescripcion("Discos de música " + categoria.toLowerCase());
            return categoriaRepo.save(c);
        }));
        productoRepo.save(p);
    }
}