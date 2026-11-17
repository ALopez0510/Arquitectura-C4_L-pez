package ec.udla.consultas.sri.cliente;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.consultas.common.api.ServicioExternoException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.util.function.Supplier;

/**
 * Adaptador HTTP hacia las APIs REST públicas del SRI.
 * Cualquier falla técnica se convierte en {@link ServicioExternoException}.
 */
@Component
public class ClienteSri {

    private static final String CATASTRO = "SRI Catastro";
    private static final String MATRICULACION = "SRI Matriculación vehicular";

    private final RestClient catastro;
    private final RestClient matriculacion;
    private final ObjectMapper mapper;

    public ClienteSri(RestClient.Builder builder, ObjectMapper mapper, PropiedadesSri props) {
        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(props.timeoutConexion())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(props.timeoutLectura());

        this.catastro = builder.clone().baseUrl(props.catastroUrl()).requestFactory(fabrica).build();
        this.matriculacion = builder.clone().baseUrl(props.matriculacionUrl()).requestFactory(fabrica).build();
        this.mapper = mapper;
    }

    /** GET /ConsolidadoContribuyente/existePorNumeroRuc?numeroRuc= → true / false */
    public boolean existeContribuyente(String ruc) {
        String cuerpo = ejecutar(CATASTRO, () -> catastro.get()
                .uri("/ConsolidadoContribuyente/existePorNumeroRuc?numeroRuc={ruc}", ruc)
                .retrieve()
                .body(String.class));
        return Boolean.parseBoolean(cuerpo == null ? "false" : cuerpo.trim());
    }

    /** GET /ConsolidadoContribuyente/obtenerPorNumerosRuc?&ruc= → arreglo con el contribuyente, o null. */
    public JsonNode obtenerContribuyente(String ruc) {
        JsonNode arreglo = leerJson(CATASTRO, ejecutar(CATASTRO, () -> catastro.get()
                .uri("/ConsolidadoContribuyente/obtenerPorNumerosRuc?&ruc={ruc}", ruc)
                .retrieve()
                .body(String.class)));
        if (arreglo == null || !arreglo.isArray() || arreglo.isEmpty()) {
            return null;
        }
        return arreglo.get(0);
    }

    /**
     * GET /BaseVehiculo/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn?numeroPlacaCampvCpn=
     * Devuelve null cuando el SRI responde {"mensajeServidor":{"texto":"El vehículo no existe"}}.
     */
    public JsonNode obtenerVehiculo(String placa) {
        JsonNode nodo = leerJson(MATRICULACION, ejecutar(MATRICULACION, () -> matriculacion.get()
                .uri("/BaseVehiculo/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn?numeroPlacaCampvCpn={placa}", placa)
                .retrieve()
                .body(String.class)));
        if (nodo == null || !nodo.hasNonNull("numeroPlaca")) {
            return null;
        }
        return nodo;
    }

    private String ejecutar(String servicio, Supplier<String> llamada) {
        try {
            return llamada.get();
        } catch (RestClientResponseException ex) {
            throw new ServicioExternoException(servicio,
                    "HTTP " + ex.getStatusCode().value() + " " + ex.getStatusText(), ex);
        } catch (ResourceAccessException ex) {
            throw new ServicioExternoException(servicio, "sin respuesta o tiempo de espera agotado", ex);
        } catch (RestClientException ex) {
            throw new ServicioExternoException(servicio, "error de comunicación", ex);
        }
    }

    private JsonNode leerJson(String servicio, String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return null;
        }
        try {
            return mapper.readTree(cuerpo);
        } catch (Exception ex) {
            throw new ServicioExternoException(servicio, "respuesta con formato no reconocido", ex);
        }
    }
}
