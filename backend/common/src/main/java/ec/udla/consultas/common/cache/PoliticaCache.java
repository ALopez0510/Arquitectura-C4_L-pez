package ec.udla.consultas.common.cache;

import java.time.Duration;

/**
 * Reglas de caché para un tipo de dato.
 *
 * @param fuente    nombre del servicio externo (se muestra en los metadatos y avisos)
 * @param simulado  true si el adaptador es el servicio simulado
 * @param vigencia  tiempo durante el cual el dato se considera reciente y se sirve sin consultar al servicio
 * @param retencion tiempo máximo que se conserva el último dato bueno para usarlo como respaldo
 */
public record PoliticaCache(String fuente, boolean simulado, Duration vigencia, Duration retencion) {

    public PoliticaCache {
        if (retencion.compareTo(vigencia) < 0) {
            throw new IllegalArgumentException("La retención debe ser mayor o igual que la vigencia");
        }
    }
}
