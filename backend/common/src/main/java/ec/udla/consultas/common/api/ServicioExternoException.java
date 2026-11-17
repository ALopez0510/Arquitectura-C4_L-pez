package ec.udla.consultas.common.api;

/**
 * Falla técnica de un servicio externo (caído, HTTP 5xx, tiempo de espera agotado o respuesta
 * irreconocible). Es la única excepción que activa el respaldo desde la caché.
 * Los errores de negocio (RUC inexistente, placa no encontrada) NO usan esta excepción.
 */
public class ServicioExternoException extends RuntimeException {

    private final String servicio;

    public ServicioExternoException(String servicio, String motivo) {
        super(motivo);
        this.servicio = servicio;
    }

    public ServicioExternoException(String servicio, String motivo, Throwable causa) {
        super(motivo, causa);
        this.servicio = servicio;
    }

    public String getServicio() {
        return servicio;
    }
}
