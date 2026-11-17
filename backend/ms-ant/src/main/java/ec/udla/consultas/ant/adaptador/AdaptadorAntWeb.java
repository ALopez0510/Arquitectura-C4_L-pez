package ec.udla.consultas.ant.adaptador;

import ec.udla.consultas.ant.PropiedadesAnt;
import ec.udla.consultas.ant.dominio.Citacion;
import ec.udla.consultas.ant.dominio.ConsultaPuntos;
import ec.udla.consultas.ant.dominio.PuntosLicencia;
import ec.udla.consultas.common.api.ServicioExternoException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adaptador real: consulta la página web de citaciones de la ANT y extrae puntos y citaciones del HTML.
 * Se activa con ANT_MODO=real.
 *
 * Nota: al 17/11/2026 la ANT responde 502 Bad Gateway (ver carpeta evidencia/), por lo que el
 * extractor se diseñó de forma tolerante (busca el texto "puntos" y las filas con fecha). Si la
 * respuesta no se reconoce se trata como falla técnica, para no guardar en caché un dato incorrecto.
 */
@Component
@ConditionalOnProperty(name = "app.ant.modo", havingValue = "real")
public class AdaptadorAntWeb implements PuertoAnt {

    private static final String NOMBRE = "ANT";
    private static final Pattern PUNTOS = Pattern.compile("(?i)puntos?[^0-9]{0,40}(\\d{1,2})");
    private static final Pattern FECHA = Pattern.compile("\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}");
    private static final Pattern VALOR = Pattern.compile("\\d+[.,]\\d{2}");

    private final PropiedadesAnt props;
    private final HttpClient http;

    public AdaptadorAntWeb(PropiedadesAnt props) {
        this.props = props;
        this.http = HttpClient.newBuilder()
                .connectTimeout(props.timeoutConexion())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public PuntosLicencia consultar(ConsultaPuntos consulta) {
        URI uri = UriComponentsBuilder.fromHttpUrl(props.url())
                .queryParam("ps_tipo_identificacion", consulta.tipoIdentificacion())
                .queryParam("ps_identificacion", consulta.identificacion())
                .queryParam("ps_placa", consulta.placa())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        HttpRequest solicitud = HttpRequest.newBuilder(uri)
                .timeout(props.tiempoEspera())
                .header("Accept", "text/html")
                .GET()
                .build();
        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(solicitud, HttpResponse.BodyHandlers.ofString(StandardCharsets.ISO_8859_1));
        } catch (HttpTimeoutException ex) {
            throw new ServicioExternoException(NOMBRE, "tiempo de espera agotado", ex);
        } catch (IOException ex) {
            throw new ServicioExternoException(NOMBRE, "sin conexión con el servidor", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServicioExternoException(NOMBRE, "consulta interrumpida", ex);
        }
        if (respuesta.statusCode() >= 400) {
            throw new ServicioExternoException(NOMBRE, "HTTP " + respuesta.statusCode());
        }
        return extraer(consulta.identificacion(), respuesta.body());
    }

    PuntosLicencia extraer(String identificacion, String html) {
        Document doc = Jsoup.parse(html);
        Matcher m = PUNTOS.matcher(doc.text());
        if (!m.find()) {
            throw new ServicioExternoException(NOMBRE, "respuesta con formato no reconocido");
        }
        int puntos = Math.min(30, Integer.parseInt(m.group(1)));

        List<Citacion> citaciones = new ArrayList<>();
        BigDecimal pendiente = BigDecimal.ZERO;
        for (Element fila : doc.select("tr")) {
            List<String> celdas = fila.select("td").eachText();
            if (celdas.size() < 4) {
                continue;
            }
            String fecha = celdas.stream().filter(c -> FECHA.matcher(c).find()).findFirst().orElse(null);
            if (fecha == null) {
                continue;
            }
            BigDecimal valor = celdas.stream()
                    .filter(c -> VALOR.matcher(c).matches())
                    .map(c -> new BigDecimal(c.replace(',', '.')))
                    .reduce((a, b) -> b)
                    .orElse(BigDecimal.ZERO);
            String estado = celdas.stream().filter(c -> c.toUpperCase().matches(".*(PENDIENTE|PAGAD|ANULAD).*"))
                    .findFirst().orElse("DESCONOCIDO").toUpperCase();
            if (estado.contains("PENDIENTE")) {
                pendiente = pendiente.add(valor);
            }
            citaciones.add(new Citacion(celdas.get(0), fecha, null, String.join(" ", celdas.subList(1, Math.min(3, celdas.size()))),
                    0, valor, estado));
        }
        return new PuntosLicencia(identificacion, null, puntos, 30, citaciones, pendiente);
    }

    @Override
    public String nombre() {
        return NOMBRE;
    }

    @Override
    public boolean esSimulado() {
        return false;
    }
}
