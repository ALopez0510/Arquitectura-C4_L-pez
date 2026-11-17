package ec.udla.consultas.ant.adaptador;

import ec.udla.consultas.ant.dominio.ConsultaPuntos;
import ec.udla.consultas.ant.dominio.PuntosLicencia;

/**
 * Contrato (puerto) con la ANT. Lo implementan el adaptador web real y el adaptador simulado,
 * de modo que el resto del sistema no sabe cuál se está usando.
 * Ante una falla técnica las implementaciones lanzan ServicioExternoException.
 */
public interface PuertoAnt {

    PuntosLicencia consultar(ConsultaPuntos consulta);

    /** Nombre que se muestra en los metadatos y avisos. */
    String nombre();

    boolean esSimulado();
}
