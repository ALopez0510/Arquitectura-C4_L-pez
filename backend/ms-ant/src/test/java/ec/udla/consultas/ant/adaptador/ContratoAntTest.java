package ec.udla.consultas.ant.adaptador;

import ec.udla.consultas.ant.PropiedadesAnt;
import ec.udla.consultas.ant.dominio.ConsultaPuntos;
import ec.udla.consultas.ant.dominio.PuntosLicencia;
import ec.udla.consultas.common.api.ServicioExternoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica que el servicio simulado cumple el contrato que también cumple el adaptador real:
 * misma entrada (ConsultaPuntos) y misma salida (PuntosLicencia).
 */
class ContratoAntTest {

    private final PropiedadesAnt props = new PropiedadesAnt("simulado",
            "https://consultaweb.ant.gob.ec/PortalWEB/paginas/clientes/clp_grid_citaciones.jsp",
            Duration.ofSeconds(3), Duration.ofSeconds(8), Duration.ofHours(1), Duration.ofDays(7),
            Duration.ofMillis(10));

    private final ConsultaPuntos consulta = new ConsultaPuntos("CED", "1710034065", "PBX1234");

    @Test
    void simuladoCumpleElContrato() {
        PuntosLicencia r = new AdaptadorAntSimulado(props).consultar(consulta);

        assertThat(r.identificacion()).isEqualTo("1710034065");
        assertThat(r.puntosMaximos()).isEqualTo(30);
        assertThat(r.puntos()).isBetween(0, 30);
        assertThat(r.citaciones()).isNotNull();
        int restados = r.citaciones().stream().mapToInt(c -> c.puntosRestados()).sum();
        assertThat(r.puntos()).isEqualTo(Math.max(0, 30 - restados));
        BigDecimal pendiente = r.citaciones().stream()
                .filter(c -> c.estado().equals("PENDIENTE"))
                .map(c -> c.valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(r.totalPendiente()).isEqualByComparingTo(pendiente);
    }

    @Test
    void simuladoEsDeterminista() {
        AdaptadorAntSimulado ant = new AdaptadorAntSimulado(props);
        assertThat(ant.consultar(consulta)).isEqualTo(ant.consultar(consulta));
    }

    @Test
    void simuladoCaidoFallaComoLaAntReal() {
        AdaptadorAntSimulado ant = new AdaptadorAntSimulado(props);
        ant.setEstado(EstadoSimulacion.CAIDO);
        assertThatThrownBy(() -> ant.consultar(consulta))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("502");
    }

    @Test
    void adaptadorRealExtraePuntosDelHtml() {
        String html = "<html><body><table>"
                + "<tr><td>Puntos de licencia:</td><td>24</td></tr>"
                + "<tr><td>C-123</td><td>2026-05-10</td><td>Exceso de velocidad</td><td>PENDIENTE</td><td>230.00</td></tr>"
                + "</table></body></html>";

        PuntosLicencia r = new AdaptadorAntWeb(props).extraer("1710034065", html);

        assertThat(r.puntos()).isEqualTo(24);
        assertThat(r.citaciones()).hasSize(1);
        assertThat(r.totalPendiente()).isEqualByComparingTo("230.00");
    }

    @Test
    void adaptadorRealRechazaHtmlNoReconocido() {
        assertThatThrownBy(() -> new AdaptadorAntWeb(props).extraer("1710034065", "<html>502 Bad Gateway</html>"))
                .isInstanceOf(ServicioExternoException.class);
    }
}
