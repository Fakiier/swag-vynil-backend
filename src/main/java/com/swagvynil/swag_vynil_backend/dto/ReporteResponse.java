
package com.swagvynil.swag_vynil_backend.dto;

import java.util.List;

public record ReporteResponse(
        int ventasTotales,
        long cantidadBoletas,
        double ticketPromedio,
        List<ProductoVendido> productosMasVendidos
) {

    public record ProductoVendido(
            Long productoId,
            String nombre,
    long unidadesVendidas,
    int totalVendido
    ) {
    }
}
