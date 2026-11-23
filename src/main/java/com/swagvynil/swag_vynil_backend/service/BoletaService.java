package com.swagvynil.swag_vynil_backend.service;

import com.swagvynil.swag_vynil_backend.dto.BoletaRequest;
import com.swagvynil.swag_vynil_backend.dto.BoletaResponse;
import com.swagvynil.swag_vynil_backend.dto.ReporteResponse;
import com.swagvynil.swag_vynil_backend.model.*;
import com.swagvynil.swag_vynil_backend.repository.BoletaRepository;
import com.swagvynil.swag_vynil_backend.repository.ProductoRepository;
import com.swagvynil.swag_vynil_backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BoletaService {

    private final BoletaRepository boletaRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    public BoletaService(BoletaRepository boletaRepo, ProductoRepository productoRepo,
                         UsuarioRepository usuarioRepo) {
        this.boletaRepo = boletaRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    public BoletaResponse crear(String correoUsuario, BoletaRequest r) {
        Usuario usuario = usuarioRepo.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Boleta boleta = new Boleta();
        boleta.setUsuario(usuario);
        boleta.setFecha(LocalDateTime.now());
        boleta.setMetodoPago(r.metodoPago() == null || r.metodoPago().isBlank() ? "Tarjeta" : r.metodoPago());
        boleta.setCalle(r.calle());
        boleta.setDepartamento(r.departamento());
        boleta.setRegion(r.region());
        boleta.setComuna(r.comuna());
        boleta.setIndicaciones(r.indicaciones());
        boleta.setEstado(Boolean.TRUE.equals(r.pagoRechazado()) ? EstadoBoleta.FALLIDA : EstadoBoleta.COMPLETADA);

        int total = 0;
        for (BoletaRequest.Item item : r.items()) {
            Producto p = productoRepo.findById(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El producto " + item.productoId() + " no existe"));

            // un pago fallido no consume stock, así que solo se valida si el pago es válido
            if (boleta.getEstado() == EstadoBoleta.COMPLETADA) {
                int stock = p.getStock() == null ? 0 : p.getStock();
                if (stock < item.cantidad()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Stock insuficiente de \"" + p.getNombre() + "\" (quedan " + stock + ")");
                }
                p.setStock(stock - item.cantidad());
                productoRepo.save(p);
            }

            // el precio lo define el servidor, nunca el cliente
            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setBoleta(boleta);
            detalle.setProducto(p);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(p.precioFinal());
            boleta.getDetalles().add(detalle);

            total += p.precioFinal() * item.cantidad();
        }
        boleta.setTotal(total);

        return BoletaResponse.de(boletaRepo.save(boleta));
    }

    public List<BoletaResponse> listar() {
        return boletaRepo.findAllByOrderByFechaDesc().stream().map(BoletaResponse::de).toList();
    }

    public List<BoletaResponse> porUsuario(Long usuarioId) {
        return boletaRepo.findByUsuarioIdOrderByFechaDesc(usuarioId).stream().map(BoletaResponse::de).toList();
    }

    public List<BoletaResponse> mias(String correo) {
        return boletaRepo.findByUsuarioCorreoOrderByFechaDesc(correo).stream().map(BoletaResponse::de).toList();
    }

    public BoletaResponse obtener(Long id, String correoSolicitante, boolean esPersonal) {
        Boleta b = boletaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boleta no encontrada"));
        // un Cliente solo puede ver sus propias boletas
        if (!esPersonal && !b.getUsuario().getCorreo().equals(correoSolicitante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver esta boleta");
        }
        return BoletaResponse.de(b);
    }

    public ReporteResponse generarReporte() {

        List<Boleta> boletasCompletadas = boletaRepo.findAllByOrderByFechaDesc()
                .stream()
                .filter(b -> b.getEstado() == EstadoBoleta.COMPLETADA)
                .toList();

        int ventasTotales = boletasCompletadas.stream()
                .mapToInt(Boleta::getTotal)
                .sum();

        long cantidadBoletas = boletasCompletadas.size();

        double ticketPromedio = cantidadBoletas == 0
                ? 0
                : (double) ventasTotales / cantidadBoletas;

        Map<Long, ReporteResponse.ProductoVendido> productos = new HashMap<>();

        for (Boleta boleta : boletasCompletadas) {
            for (DetalleBoleta detalle : boleta.getDetalles()) {

                Long productoId = detalle.getProducto().getId();
                String nombre = detalle.getProducto().getNombre();
                long cantidad = detalle.getCantidad();
                int total = detalle.getPrecioUnitario() * detalle.getCantidad();

                productos.merge(
                        productoId,
                        new ReporteResponse.ProductoVendido(
                                productoId, nombre, cantidad, total
                        ),
                        (anterior, nuevo) -> new ReporteResponse.ProductoVendido(
                                productoId,
                                nombre,
                                anterior.unidadesVendidas() + nuevo.unidadesVendidas(),
                                anterior.totalVendido() + nuevo.totalVendido()
                        )
                );
            }
        }

        List<ReporteResponse.ProductoVendido> masVendidos = productos.values()
                .stream()
                .sorted(Comparator.comparingLong(
                        ReporteResponse.ProductoVendido::unidadesVendidas
                ).reversed())
                .limit(5)
                .toList();

        return new ReporteResponse(
                ventasTotales,
                cantidadBoletas,
                ticketPromedio,
                masVendidos
        );
    }

}