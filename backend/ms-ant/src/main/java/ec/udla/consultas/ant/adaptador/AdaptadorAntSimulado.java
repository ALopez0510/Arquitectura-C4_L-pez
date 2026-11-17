package ec.udla.consultas.ant.adaptador;

import ec.udla.consultas.ant.PropiedadesAnt;
import ec.udla.consultas.ant.dominio.Citacion;
import ec.udla.consultas.ant.dominio.ConsultaPuntos;
import ec.udla.consultas.ant.dominio.PuntosLicencia;
import ec.udla.consultas.common.api.ServicioExternoException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Contrato simulado de la ANT (ANT_MODO=simulado, valor por defecto mientras la ANT esté caída).
 * Recibe la misma entrada y devuelve la misma estructura que el adaptador real. Los datos son
 * deterministas: la misma cédula siempre produce los mismos puntos y citaciones.
 * Su estado (DISPONIBLE, CAIDO, LENTO) se cambia en tiempo de ejecución para probar la resiliencia.
 */
@Component
@ConditionalOnProperty(name = "app.ant.modo", havingValue = "simulado", matchIfMissing = true)
public class AdaptadorAntSimulado implements PuertoAnt {

    private record Infraccion(String descripcion, int puntos, String valor) {
    }

    private static final List<Infraccion> CATALOGO = List.of(
            new Infraccion("Exceso de velocidad dentro del rango moderado", 6, "230.00"),
            new Infraccion("No respetar la luz roja del semáforo", 10, "460.00"),
            new Infraccion("Uso de teléfono celular mientras conduce", 4, "115.00"),
            new Infraccion("No portar la licencia de conducir", 1, "23.00"),
            new Infraccion("Estacionar en lugar prohibido", 3, "69.00"),
            new Infraccion("No usar el cinturón de seguridad", 3, "69.00"));

    private final AtomicReference<EstadoSimulacion> estado = new AtomicReference<>(EstadoSimulacion.DISPONIBLE);
    private final PropiedadesAnt props;

    public AdaptadorAntSimulado(PropiedadesAnt props) {
        this.props = props;
    }

    @Override
    public PuntosLicencia consultar(ConsultaPuntos consulta) {
        switch (estado.get()) {
            case CAIDO -> throw new ServicioExternoException(nombre(), "HTTP 502 Bad Gateway");
            case LENTO -> esperar();
            default -> {
                // responde normalmente
            }
        }
        return generar(consulta);
    }

    PuntosLicencia generar(ConsultaPuntos consulta) {
        Random azar = new Random(consulta.identificacion().hashCode());
        int cantidad = azar.nextInt(4); // 0 a 3 citaciones
        List<Citacion> citaciones = new ArrayList<>();
        int restados = 0;
        BigDecimal pendiente = BigDecimal.ZERO;
        LocalDate base = LocalDate.of(2026, 10, 31);
        for (int i = 0; i < cantidad; i++) {
            Infraccion inf = CATALOGO.get(azar.nextInt(CATALOGO.size()));
            boolean pagada = azar.nextBoolean();
            BigDecimal valor = new BigDecimal(inf.valor());
            restados += inf.puntos();
            if (!pagada) {
                pendiente = pendiente.add(valor);
            }
            String placa = consulta.placa() == null || consulta.placa().isBlank() ? null : consulta.placa();
            citaciones.add(new Citacion(
                    String.format("SIM-%07d", Math.abs(azar.nextInt(10_000_000))),
                    base.minusDays(azar.nextInt(300)).toString(),
                    placa,
                    inf.descripcion(),
                    inf.puntos(),
                    valor,
                    pagada ? "PAGADA" : "PENDIENTE"));
        }
        return new PuntosLicencia(consulta.identificacion(), null, Math.max(0, 30 - restados), 30,
                citaciones, pendiente);
    }

    private void esperar() {
        try {
            Thread.sleep(props.latenciaSimuladaLenta().toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServicioExternoException(nombre(), "consulta interrumpida");
        }
    }

    public EstadoSimulacion getEstado() {
        return estado.get();
    }

    public void setEstado(EstadoSimulacion nuevo) {
        estado.set(nuevo);
    }

    @Override
    public String nombre() {
        return "ANT (servicio simulado)";
    }

    @Override
    public boolean esSimulado() {
        return true;
    }
}
