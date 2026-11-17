package ec.udla.consultas.ant.servicio;

import ec.udla.consultas.ant.PropiedadesAnt;
import ec.udla.consultas.ant.adaptador.PuertoAnt;
import ec.udla.consultas.ant.dominio.ConsultaPuntos;
import ec.udla.consultas.ant.dominio.PuntosLicencia;
import ec.udla.consultas.common.api.Respuesta;
import ec.udla.consultas.common.api.ServicioExternoException;
import ec.udla.consultas.common.cache.CacheResiliente;
import ec.udla.consultas.common.cache.PoliticaCache;
import ec.udla.consultas.common.validacion.ValidadorIdentificacion;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Consulta de puntos con: tiempo de espera máximo, caché vigente y respaldo con el último dato bueno.
 */
@Service
public class ServicioPuntos {

    private final PuertoAnt ant;
    private final CacheResiliente cache;
    private final PropiedadesAnt props;
    private final PoliticaCache politica;
    private final ExecutorService ejecutor = Executors.newCachedThreadPool();

    public ServicioPuntos(PuertoAnt ant, CacheResiliente cache, PropiedadesAnt props) {
        this.ant = ant;
        this.cache = cache;
        this.props = props;
        this.politica = new PoliticaCache(ant.nombre(), ant.esSimulado(), props.vigencia(), props.retencion());
    }

    public Respuesta<PuntosLicencia> consultar(String cedulaIngresada, String placaIngresada, boolean refrescar) {
        String cedula = ValidadorIdentificacion.validarCedula(cedulaIngresada, "cedula");
        String placa = placaIngresada == null || placaIngresada.isBlank()
                ? "" : ValidadorIdentificacion.validarPlaca(placaIngresada, "placa");
        ConsultaPuntos consulta = new ConsultaPuntos("CED", cedula, placa);

        // La clave separa datos simulados de reales para no mezclarlos al cambiar de modo
        String clave = "ant:" + (ant.esSimulado() ? "simulado" : "real") + ":puntos:" + cedula + ":" + placa;
        return cache.obtener(politica, clave, PuntosLicencia.class, refrescar, () -> conTiempoLimite(consulta));
    }

    private PuntosLicencia conTiempoLimite(ConsultaPuntos consulta) {
        Future<PuntosLicencia> tarea = ejecutor.submit(() -> ant.consultar(consulta));
        long limite = props.tiempoEspera().toMillis();
        try {
            return tarea.get(limite, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            tarea.cancel(true);
            throw new ServicioExternoException(ant.nombre(),
                    "no respondió en " + props.tiempoEspera().toSeconds() + " s");
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof RuntimeException causa) {
                throw causa;
            }
            throw new ServicioExternoException(ant.nombre(), "error inesperado", ex.getCause());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServicioExternoException(ant.nombre(), "consulta interrumpida", ex);
        }
    }

    @PreDestroy
    void cerrar() {
        ejecutor.shutdownNow();
    }
}
