package ec.udla.consultas.common.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Caché en la nube: Redis administrado (Upstash). La conexión se define con la variable
 * de entorno REDIS_URL, por ejemplo rediss://default:CLAVE@xxxx.upstash.io:6379
 */
@Component
@ConditionalOnProperty(name = "app.cache.tipo", havingValue = "redis", matchIfMissing = true)
public class AlmacenRedis implements AlmacenCache {

    private final StringRedisTemplate redis;

    public AlmacenRedis(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<String> leer(String clave) {
        return Optional.ofNullable(redis.opsForValue().get(clave));
    }

    @Override
    public void guardar(String clave, String valor, Duration tiempoDeVida) {
        redis.opsForValue().set(clave, valor, tiempoDeVida);
    }

    @Override
    public String descripcion() {
        return "Redis en la nube (Upstash)";
    }
}
