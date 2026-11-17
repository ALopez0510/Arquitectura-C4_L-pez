package ec.udla.consultas.common.api;

import java.time.OffsetDateTime;

/**
 * Información sobre la frescura del dato. El frontend usa {@code desactualizado} y {@code aviso}
 * para advertir al usuario cuando el dato viene del respaldo en caché.
 *
 * @param origen           SERVICIO_EXTERNO, CACHE o CACHE_RESPALDO
 * @param fuente           nombre del servicio consultado (p. ej. "ANT")
 * @param simulado         true si el dato proviene del servicio simulado (contrato simulado)
 * @param obtenidoEn       momento en que se obtuvo el dato del servicio externo
 * @param desactualizado   true si el dato superó su vigencia y se entrega por respaldo
 * @param aviso            mensaje para mostrar al usuario cuando desactualizado = true
 * @param vigenciaSegundos tiempo de vida definido para el dato
 */
public record Metadatos(
        Origen origen,
        String fuente,
        boolean simulado,
        OffsetDateTime obtenidoEn,
        boolean desactualizado,
        String aviso,
        long vigenciaSegundos) {
}
