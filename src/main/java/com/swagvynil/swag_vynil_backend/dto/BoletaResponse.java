package com.swagvynil.swag_vynil_backend.dto;

import com.swagvynil.swag_vynil_backend.model.Boleta;
import com.swagvynil.swag_vynil_backend.model.DetalleBoleta;

import java.time.LocalDateTime;
import java.util.List;

public record BoletaResponse(
        Long id,
        Long usuarioId,
        String run,
        String cliente,
        String correo,
        LocalDateTime fecha,
        String estado,
        String metodoPago,
        int total,
        String calle,
        String departamento,
        String region,
        String comuna,
        String indicaciones,
        List<Detalle> detalles
) {
    public record Detalle(Long productoId, String nombre, String imagen,
                          int cantidad, int precioUnitario, int subtotal) {
        static Detalle de(DetalleBoleta d) {
            int precio = d.getPrecioUnitario();
            return new Detalle(d.getProducto().getId(), d.getProducto().getNombre(),
                    d.getProducto().getImagen(), d.getCantidad(), precio, precio * d.getCantidad());
        }
    }

    public static BoletaResponse de(Boleta b) {
        return new BoletaResponse(
                b.getId(),
                b.getUsuario().getId(),
                b.getUsuario().getRun(),
                b.getUsuario().getNombre() + " " + b.getUsuario().getApellidos(),
                b.getUsuario().getCorreo(),
                b.getFecha(),
                b.getEstado().name(),
                b.getMetodoPago(),
                b.getTotal(),
                b.getCalle(),
                b.getDepartamento(),
                b.getRegion(),
                b.getComuna(),
                b.getIndicaciones(),
                b.getDetalles().stream().map(Detalle::de).toList()
        );
    }
}