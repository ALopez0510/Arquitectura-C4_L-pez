package ec.udla.consultas.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ec.udla.consultas.common.api.Origen;
import ec.udla.consultas.common.api.Respuesta;
import ec.udla.consultas.common.api.ServicioExternoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CacheResilienteTest {

    record Dato(String valor) {
    }

    /** Reloj que se puede adelantar para simular el paso del tiempo. */
    static class RelojMovible extends Clock {
        private Instant ahora = Instant.parse("2026-11-17T16:00:00Z");

        void avanzar(Duration d) {
            ahora = ahora.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private final PoliticaCache politica = new PoliticaCache("ANT", false, Duration.ofHours(1), Duration.ofDays(7));
    private RelojMovible reloj;
    private CacheResiliente cache;

    @BeforeEach
    void preparar() {
        reloj = new RelojMovible();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        cache = new CacheResiliente(new AlmacenMemoria(reloj), mapper, reloj);
    }

    @Test
    void primeraConsultaVaAlServicioYLaSegundaSaleDeCache() {
        AtomicInteger llamadas = new AtomicInteger();

        Respuesta<Dato> r1 = cache.obtener(politica, "k", Dato.class, false, () -> {
            llamadas.incrementAndGet();
            return new Dato("A");
        });
        Respuesta<Dato> r2 = cache.obtener(politica, "k", Dato.class, false, () -> {
            llamadas.incrementAndGet();
            return new Dato("B");
        });

        assertThat(r1.metadatos().origen()).isEqualTo(Origen.SERVICIO_EXTERNO);
        assertThat(r2.metadatos().origen()).isEqualTo(Origen.CACHE);
        assertThat(r2.datos().valor()).isEqualTo("A");
        assertThat(llamadas).hasValue(1);
    }

    @Test
    void siElServicioFallaDevuelveElUltimoDatoBuenoConAviso() {
        cache.obtener(politica, "k", Dato.class, false, () -> new Dato("A"));
        reloj.avanzar(Duration.ofHours(2)); // el dato ya no es vigente

        Respuesta<Dato> r = cache.obtener(politica, "k", Dato.class, false, () -> {
            throw new ServicioExternoException("ANT", "HTTP 502 Bad Gateway");
        });

        assertThat(r.datos().valor()).isEqualTo("A");
        assertThat(r.metadatos().origen()).isEqualTo(Origen.CACHE_RESPALDO);
        assertThat(r.metadatos().desactualizado()).isTrue();
        assertThat(r.metadatos().aviso()).contains("puede no estar actualizado");
    }

    @Test
    void refrescarIgnoraLaVigenciaPeroConservaElRespaldo() {
        cache.obtener(politica, "k", Dato.class, false, () -> new Dato("A"));

        Respuesta<Dato> r = cache.obtener(politica, "k", Dato.class, true, () -> {
            throw new ServicioExternoException("ANT", "tiempo de espera agotado");
        });

        assertThat(r.metadatos().origen()).isEqualTo(Origen.CACHE_RESPALDO);
    }

    @Test
    void siFallaYNoHayDatoGuardadoPropagaElError() {
        assertThatThrownBy(() -> cache.obtener(politica, "k", Dato.class, false, () -> {
            throw new ServicioExternoException("ANT", "HTTP 502 Bad Gateway");
        })).isInstanceOf(ServicioExternoException.class);
    }

    @Test
    void elRespaldoExpiraDespuesDeLaRetencion() {
        cache.obtener(politica, "k", Dato.class, false, () -> new Dato("A"));
        reloj.avanzar(Duration.ofDays(8));

        assertThatThrownBy(() -> cache.obtener(politica, "k", Dato.class, false, () -> {
            throw new ServicioExternoException("ANT", "HTTP 502 Bad Gateway");
        })).isInstanceOf(ServicioExternoException.class);
    }
}
