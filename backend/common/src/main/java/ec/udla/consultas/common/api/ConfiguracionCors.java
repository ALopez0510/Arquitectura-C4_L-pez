package ec.udla.consultas.common.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Permite que el frontend React (otro origen) consuma la API. */
@Configuration
public class ConfiguracionCors implements WebMvcConfigurer {

    private final String[] origenes;

    public ConfiguracionCors(@Value("${app.cors.origenes:http://localhost:5173}") String[] origenes) {
        this.origenes = origenes;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET", "POST", "PUT", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
