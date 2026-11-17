package ec.udla.consultas.common.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.consultas.common.api.Metadatos;
import ec.udla.consultas.common.api.Origen;
import ec.udla.consultas.common.api.Respuesta;
import ec.udla.consultas.common.api.ServicioExternoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Patrón Cache-Aside con respaldo ante fallas ("stale-if-error").
 *
 * <ol>
 *   <li>Si hay un dato en caché dentro de su vigencia, se devuelve sin llamar al servicio externo.</li>
 *   <li>Si no, se llama al servicio externo. Cada respuesta correcta se guarda en caché durante el
 *       tiempo de retención.</li>
 *   <li>Si el servicio externo falla ({@link ServicioExternoException}) y existe un dato guardado,
 *       se devuelve ese último dato bueno marcado como desactualizado y con un aviso.</li>
 *   <li>Si falla y no hay dato guardado, se propaga el error (HTTP 503).</li>
 * </ol>
 * Si la propia caché no responde, el sistema sigue funcionando contra el servicio externo.
 */
@Component
public class CacheResiliente {

    private static final Logger log = LoggerFactory.getLogger(CacheResiliente.class);
    private static final ZoneId ZONA = ZoneId.of("America/Guayaquil");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Lo que se guarda en Redis: el dato y el instante en que se obtuvo. */
    public record EntradaCache(long obtenidoEnEpochMs, JsonNode datos) {
    }

    private final AlmacenCache almacen;
    private final ObjectMapper mapper;
    private final Clock reloj;

    @Autowired
    public CacheResiliente(AlmacenCache almacen, ObjectMapper mapper) {
        this(almacen, mapper, Clock.system(ZONA));
    }

    public CacheResiliente(AlmacenCache almacen, ObjectMapper mapper, Clock reloj) {
        this.almacen = almacen;
        this.mapper = mapper;
        this.reloj = reloj;
    }

    /**
     * @param politica vigencia y retención del dato
     * @param clave    clave única de la consulta
     * @param tipo     clase del dato
     * @param refrescar true para ignorar la vigencia y consultar el servicio externo (el respaldo sigue activo)
     * @param origen   llamada al servicio externo
     */
    public <T> Respuesta<T> obtener(PoliticaCache politica, String clave, Class<T> tipo,
                                    boolean refrescar, Supplier<T> origen) {
        Instant ahora = reloj.instant();
        Optional<EntradaCache> guardado = leer(clave);

        if (!refrescar && guardado.isPresent() && esVigente(guardado.get(), politica, ahora)) {
            EntradaCache e = guardado.get();
            return new Respuesta<>(mapper.convertValue(e.datos(), tipo),
                    metadatos(politica, Origen.CACHE, e.obtenidoEnEpochMs(), false, null));
        }

        try {
            T datos = origen.get();
            guardar(clave, datos, ahora, politica);
            return new Respuesta<>(datos,
                    metadatos(politica, Origen.SERVICIO_EXTERNO, ahora.toEpochMilli(), false, null));
        } catch (ServicioExternoException falla) {
            if (guardado.isEmpty()) {
                throw falla;
            }
            EntradaCache e = guardado.get();
            String fecha = FORMATO.format(Instant.ofEpochMilli(e.obtenidoEnEpochMs()).atZone(ZONA));
            String aviso = "El servicio " + politica.fuente() + " no está disponible (" + falla.getMessage()
                    + "). Se muestra el último dato guardado el " + fecha
                    + "; puede no estar actualizado.";
            log.warn("Respaldo desde caché para {}: {}", clave, falla.getMessage());
            return new Respuesta<>(mapper.convertValue(e.datos(), tipo),
                    metadatos(politica, Origen.CACHE_RESPALDO, e.obtenidoEnEpochMs(), true, aviso));
        }
    }

    private boolean esVigente(EntradaCache e, PoliticaCache politica, Instant ahora) {
        return Instant.ofEpochMilli(e.obtenidoEnEpochMs()).plus(politica.vigencia()).isAfter(ahora);
    }

    private Optional<EntradaCache> leer(String clave) {
        try {
            Optional<String> json = almacen.leer(clave);
            if (json.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(mapper.readValue(json.get(), EntradaCache.class));
        } catch (Exception ex) {
            log.warn("No se pudo leer la caché ({}): {}", almacen.descripcion(), ex.getMessage());
            return Optional.empty();
        }
    }

    private void guardar(String clave, Object datos, Instant ahora, PoliticaCache politica) {
        try {
            EntradaCache entrada = new EntradaCache(ahora.toEpochMilli(), mapper.valueToTree(datos));
            almacen.guardar(clave, mapper.writeValueAsString(entrada), politica.retencion());
        } catch (Exception ex) {
            log.warn("No se pudo escribir en la caché ({}): {}", almacen.descripcion(), ex.getMessage());
        }
    }

    private Metadatos metadatos(PoliticaCache politica, Origen origen, long obtenidoEnEpochMs,
                                boolean desactualizado, String aviso) {
        OffsetDateTime obtenidoEn = Instant.ofEpochMilli(obtenidoEnEpochMs).atZone(ZONA).toOffsetDateTime();
        return new Metadatos(origen, politica.fuente(), politica.simulado(), obtenidoEn, desactualizado, aviso,
                politica.vigencia().toSeconds());
    }
}
