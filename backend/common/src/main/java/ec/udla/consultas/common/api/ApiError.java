package ec.udla.consultas.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Cuerpo estándar de todos los errores de la API.
 * Ejemplo:
 * {"codigo":"RUC_INVALIDO","mensaje":"El RUC debe tener 13 dígitos.","estado":400,
 *  "ruta":"/api/v1/contribuyentes/verificacion","timestamp":"2026-11-17T11:20:00-05:00",
 *  "detalles":[{"campo":"ruc","mensaje":"Debe contener solo dígitos."}]}
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        String codigo,
        String mensaje,
        int estado,
        String ruta,
        OffsetDateTime timestamp,
        List<DetalleError> detalles) {

    public record DetalleError(String campo, String mensaje) {
    }
}
