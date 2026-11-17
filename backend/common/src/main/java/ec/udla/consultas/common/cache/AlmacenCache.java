package ec.udla.consultas.common.cache;

import java.time.Duration;
import java.util.Optional;

/** Puerto de almacenamiento clave-valor con tiempo de vida. */
public interface AlmacenCache {

    Optional<String> leer(String clave);

    void guardar(String clave, String valor, Duration tiempoDeVida);

    String descripcion();
}
