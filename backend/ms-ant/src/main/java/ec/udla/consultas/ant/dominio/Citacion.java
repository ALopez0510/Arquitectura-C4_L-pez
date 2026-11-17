package ec.udla.consultas.ant.dominio;

import java.math.BigDecimal;

/** Una citación (multa) registrada en la ANT. */
public record Citacion(
        String numero,
        String fechaEmision,
        String placa,
        String infraccion,
        int puntosRestados,
        BigDecimal valor,
        String estado) {
}
