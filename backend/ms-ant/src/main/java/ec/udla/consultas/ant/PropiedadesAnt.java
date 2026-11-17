package ec.udla.consultas.ant;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Configuración del microservicio ANT (application.yml, prefijo app.ant). */
@ConfigurationProperties(prefix = "app.ant")
public record PropiedadesAnt(
        /* simulado | real */
        String modo,
        String url,
        Duration timeoutConexion,
        Duration tiempoEspera,
        Duration vigencia,
        Duration retencion,
        Duration latenciaSimuladaLenta) {
}
