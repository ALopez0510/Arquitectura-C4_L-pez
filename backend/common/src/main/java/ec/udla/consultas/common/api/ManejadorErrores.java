package ec.udla.consultas.common.api;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Traduce cualquier excepción a {@link ApiError} con el código HTTP adecuado.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);
    private static final ZoneId ZONA = ZoneId.of("America/Guayaquil");

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> negocio(ApiException ex, HttpServletRequest req) {
        return construir(ex.getEstado(), ex.getCodigo(), ex.getMessage(), req, ex.getDetalles());
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ApiError> servicioExterno(ServicioExternoException ex, HttpServletRequest req) {
        log.warn("Servicio externo {} no disponible y sin respaldo en caché: {}", ex.getServicio(), ex.getMessage());
        return construir(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_EXTERNO_NO_DISPONIBLE",
                "El servicio " + ex.getServicio() + " no está disponible (" + ex.getMessage()
                        + ") y no hay un dato guardado para esta consulta. Intente más tarde.",
                req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.DetalleError> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiError.DetalleError(e.getField(), e.getDefaultMessage()))
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "VALIDACION_FALLIDA",
                "Los datos enviados no son válidos.", req, detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "CUERPO_INVALIDO",
                "El cuerpo de la solicitud no es un JSON válido o tiene campos con formato incorrecto.", req, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> parametroFaltante(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "PARAMETRO_FALTANTE",
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'.", req,
                List.of(new ApiError.DetalleError(ex.getParameterName(), "Es obligatorio.")));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tipoIncorrecto(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "El parámetro '" + ex.getName() + "' tiene un formato incorrecto.", req, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> rutaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA",
                "La ruta solicitada no existe en esta API.", req, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> metodoNoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO",
                "El método " + ex.getMethod() + " no está permitido en esta ruta.", req, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> tipoContenido(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return construir(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_CONTENIDO_NO_SOPORTADO",
                "Envíe el cuerpo como application/json.", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Intente nuevamente.", req, List.of());
    }

    private ResponseEntity<ApiError> construir(HttpStatus estado, String codigo, String mensaje,
                                               HttpServletRequest req, List<ApiError.DetalleError> detalles) {
        ApiError cuerpo = new ApiError(codigo, mensaje, estado.value(), req.getRequestURI(),
                OffsetDateTime.now(ZONA), detalles);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
