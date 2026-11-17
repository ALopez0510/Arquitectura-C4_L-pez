package ec.udla.consultas.common.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caché local en memoria. Solo para desarrollo o pruebas sin conexión (CACHE_TIPO=memoria).
 */
@Component
@ConditionalOnProperty(name = "app.cache.tipo", havingValue = "memoria")
public class AlmacenMemoria implements AlmacenCache {

    private record Valor(String contenido, Instant expiraEn) {
    }

    private final Map<String, Valor> datos = new ConcurrentHashMap<>();
    private final Clock reloj;

    public AlmacenMemoria() {
        this(Clock.systemUTC());
    }

    AlmacenMemoria(Clock reloj) {
        this.reloj = reloj;
    }

    @Override
    public Optional<String> leer(String clave) {
        Valor v = datos.get(clave);
        if (v == null) {
            return Optional.empty();
        }
        if (v.expiraEn().isBefore(reloj.instant())) {
            datos.remove(clave);
            return Optional.empty();
        }
        return Optional.of(v.contenido());
    }

    @Override
    public void guardar(String clave, String valor, Duration tiempoDeVida) {
        datos.put(clave, new Valor(valor, reloj.instant().plus(tiempoDeVida)));
    }

    @Override
    public String descripcion() {
        return "Memoria local (solo desarrollo)";
    }
}
