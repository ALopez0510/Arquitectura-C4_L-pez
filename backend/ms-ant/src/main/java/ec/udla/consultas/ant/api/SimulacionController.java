package ec.udla.consultas.ant.api;

import ec.udla.consultas.ant.PropiedadesAnt;
import ec.udla.consultas.ant.adaptador.AdaptadorAntSimulado;
import ec.udla.consultas.ant.adaptador.EstadoSimulacion;
import ec.udla.consultas.common.api.ApiError.DetalleError;
import ec.udla.consultas.common.api.ApiException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Control del servicio simulado para demostrar la resiliencia (solo con ANT_MODO=simulado).
 * GET  /api/v1/simulacion/ant           → estado actual
 * PUT  /api/v1/simulacion/ant {"estado":"CAIDO"} → cambia el estado
 */
@RestController
@RequestMapping("/api/v1/simulacion/ant")
public class SimulacionController {

    public record EstadoAnt(String modo, String estado, long tiempoEsperaSegundos) {
    }

    public record CambioEstado(String estado) {
    }

    private final ObjectProvider<AdaptadorAntSimulado> simulado;
    private final PropiedadesAnt props;

    public SimulacionController(ObjectProvider<AdaptadorAntSimulado> simulado, PropiedadesAnt props) {
        this.simulado = simulado;
        this.props = props;
    }

    @GetMapping
    public EstadoAnt estado() {
        AdaptadorAntSimulado s = simulado.getIfAvailable();
        return new EstadoAnt(s == null ? "real" : "simulado", s == null ? null : s.getEstado().name(),
                props.tiempoEspera().toSeconds());
    }

    @PutMapping
    public EstadoAnt cambiar(@RequestBody CambioEstado cambio) {
        AdaptadorAntSimulado s = simulado.getIfAvailable();
        if (s == null) {
            throw new ApiException(HttpStatus.CONFLICT, "SIMULACION_NO_DISPONIBLE",
                    "El microservicio usa la ANT real (ANT_MODO=real); la simulación no se puede cambiar.");
        }
        s.setEstado(convertir(cambio == null ? null : cambio.estado()));
        return estado();
    }

    private EstadoSimulacion convertir(String valor) {
        String permitidos = Arrays.toString(EstadoSimulacion.values());
        if (valor == null || valor.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ESTADO_INVALIDO", "El estado es obligatorio.",
                    List.of(new DetalleError("estado", "Valores permitidos: " + permitidos)));
        }
        try {
            return EstadoSimulacion.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ESTADO_INVALIDO",
                    "El estado '" + valor + "' no existe.",
                    List.of(new DetalleError("estado", "Valores permitidos: " + permitidos)));
        }
    }
}
