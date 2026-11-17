package ec.udla.consultas.sri.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import ec.udla.consultas.common.api.ApiException;
import ec.udla.consultas.common.api.Respuesta;
import ec.udla.consultas.common.cache.CacheResiliente;
import ec.udla.consultas.common.cache.PoliticaCache;
import ec.udla.consultas.common.validacion.ValidadorIdentificacion;
import ec.udla.consultas.sri.cliente.ClienteSri;
import ec.udla.consultas.sri.cliente.PropiedadesSri;
import ec.udla.consultas.sri.dominio.Contribuyente;
import ec.udla.consultas.sri.dominio.Vehiculo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Casos de uso del microservicio SRI. */
@Service
public class ServicioSri {

    private final ClienteSri cliente;
    private final CacheResiliente cache;
    private final PoliticaCache politicaContribuyente;
    private final PoliticaCache politicaVehiculo;

    public ServicioSri(ClienteSri cliente, CacheResiliente cache, PropiedadesSri props) {
        this.cliente = cliente;
        this.cache = cache;
        this.politicaContribuyente = new PoliticaCache("SRI (catastro)", false,
                props.vigenciaContribuyente(), props.retencion());
        this.politicaVehiculo = new PoliticaCache("SRI (matriculación vehicular)", false,
                props.vigenciaVehiculo(), props.retencion());
    }

    /**
     * 1) Verifica que el RUC exista en el catastro del SRI; 2) obtiene sus datos;
     * 3) exige que sea PERSONA NATURAL.
     */
    public Respuesta<Contribuyente> verificarPersonaNatural(String rucIngresado) {
        String ruc = ValidadorIdentificacion.validarRuc(rucIngresado, "ruc");
        Respuesta<Contribuyente> respuesta = cache.obtener(politicaContribuyente, "sri:contribuyente:" + ruc,
                Contribuyente.class, false, () -> cargarContribuyente(ruc));

        Contribuyente c = respuesta.datos();
        if (!c.esPersonaNatural()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "NO_ES_PERSONA_NATURAL",
                    "El RUC " + ruc + " pertenece a un contribuyente de tipo " + c.tipoContribuyente()
                            + ". Esta consulta solo admite personas naturales.");
        }
        return respuesta;
    }

    public Respuesta<Vehiculo> consultarVehiculo(String placaIngresada, boolean refrescar) {
        String placa = ValidadorIdentificacion.validarPlaca(placaIngresada, "placa");
        return cache.obtener(politicaVehiculo, "sri:vehiculo:" + placa, Vehiculo.class, refrescar, () -> {
            JsonNode nodo = cliente.obtenerVehiculo(placa);
            if (nodo == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "VEHICULO_NO_ENCONTRADO",
                        "El SRI no tiene registrado un vehículo con la placa " + placa + ".");
            }
            return Vehiculo.desdeSri(nodo);
        });
    }

    private Contribuyente cargarContribuyente(String ruc) {
        if (!cliente.existeContribuyente(ruc)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CONTRIBUYENTE_NO_ENCONTRADO",
                    "El RUC " + ruc + " no está registrado como contribuyente en el SRI.");
        }
        JsonNode nodo = cliente.obtenerContribuyente(ruc);
        if (nodo == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CONTRIBUYENTE_NO_ENCONTRADO",
                    "El SRI no devolvió información para el RUC " + ruc + ".");
        }
        return Contribuyente.desdeSri(nodo);
    }
}
