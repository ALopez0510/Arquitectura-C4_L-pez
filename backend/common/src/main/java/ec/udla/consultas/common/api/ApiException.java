package ec.udla.consultas.common.api;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Error de negocio o de validación que se traduce a una respuesta HTTP con código y mensaje.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus estado;
    private final String codigo;
    private final List<ApiError.DetalleError> detalles;

    public ApiException(HttpStatus estado, String codigo, String mensaje) {
        this(estado, codigo, mensaje, List.of());
    }

    public ApiException(HttpStatus estado, String codigo, String mensaje, List<ApiError.DetalleError> detalles) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
        this.detalles = detalles == null ? List.of() : List.copyOf(detalles);
    }

    public HttpStatus getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public List<ApiError.DetalleError> getDetalles() {
        return detalles;
    }
}
