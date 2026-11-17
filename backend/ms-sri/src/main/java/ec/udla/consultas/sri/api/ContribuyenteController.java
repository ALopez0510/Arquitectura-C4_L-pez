package ec.udla.consultas.sri.api;

import ec.udla.consultas.common.api.Respuesta;
import ec.udla.consultas.sri.dominio.Contribuyente;
import ec.udla.consultas.sri.servicio.ServicioSri;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/contribuyentes")
public class ContribuyenteController {

    private final ServicioSri servicio;

    public ContribuyenteController(ServicioSri servicio) {
        this.servicio = servicio;
    }

    /**
     * Valida correo y RUC, verifica en el SRI que sea contribuyente y persona natural.
     * 200 OK | 400 validación | 404 no contribuyente | 422 no es persona natural | 503 SRI caído sin respaldo
     */
    @PostMapping("/verificacion")
    public Respuesta<VerificacionContribuyente> verificar(@Valid @RequestBody SolicitudVerificacion solicitud) {
        Respuesta<Contribuyente> r = servicio.verificarPersonaNatural(solicitud.ruc());
        String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);
        return new Respuesta<>(new VerificacionContribuyente(correo, r.datos()), r.metadatos());
    }

    /** Consulta directa de una persona natural por RUC. */
    @GetMapping("/{ruc}")
    public Respuesta<Contribuyente> obtener(@PathVariable String ruc) {
        return servicio.verificarPersonaNatural(ruc);
    }
}
