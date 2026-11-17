package ec.udla.consultas.sri.cliente;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Configuración de las APIs REST del SRI (application.yml, prefijo app.sri). */
@ConfigurationProperties(prefix = "app.sri")
public record PropiedadesSri(
        String catastroUrl,
        String matriculacionUrl,
        Duration timeoutConexion,
        Duration timeoutLectura,
        Duration vigenciaContribuyente,
        Duration vigenciaVehiculo,
        Duration retencion) {
}
