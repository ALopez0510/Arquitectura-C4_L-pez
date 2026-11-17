package ec.udla.consultas.ant.dominio;

import java.math.BigDecimal;
import java.util.List;

/**
 * Salida del contrato con la ANT. El adaptador real y el simulado devuelven exactamente esta estructura.
 *
 * @param identificacion cédula consultada
 * @param titular        nombre del titular según la ANT (puede ser null si la ANT no lo devuelve)
 * @param puntos         puntos disponibles en la licencia (máximo 30)
 * @param puntosMaximos  puntaje máximo de una licencia en Ecuador (30)
 * @param citaciones     citaciones registradas
 * @param totalPendiente suma de los valores de las citaciones pendientes de pago
 */
public record PuntosLicencia(
        String identificacion,
        String titular,
        int puntos,
        int puntosMaximos,
        List<Citacion> citaciones,
        BigDecimal totalPendiente) {
}
