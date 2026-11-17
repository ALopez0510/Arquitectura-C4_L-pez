package ec.udla.consultas.common.api;

/** De dónde salió el dato que se devuelve al cliente. */
public enum Origen {
    /** Respuesta recién obtenida del servicio externo. */
    SERVICIO_EXTERNO,
    /** Dato en caché todavía vigente (no se llamó al servicio externo). */
    CACHE,
    /** El servicio externo falló y se devolvió el último dato bueno guardado. */
    CACHE_RESPALDO
}
