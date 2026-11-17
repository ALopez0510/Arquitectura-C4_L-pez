package ec.udla.consultas.ant.api;

import ec.udla.consultas.ant.dominio.PuntosLicencia;
import ec.udla.consultas.ant.servicio.ServicioPuntos;
import ec.udla.consultas.common.api.Respuesta;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/licencias")
public class LicenciaController {

    private final ServicioPuntos servicio;

    public LicenciaController(ServicioPuntos servicio) {
        this.servicio = servicio;
    }

    /**
     * GET /api/v1/licencias/{cedula}/puntos?placa=ABC1234&refrescar=false
     * 200 OK (metadatos.desactualizado=true si viene del respaldo) | 400 cédula o placa inválida
     * | 503 ANT caída y sin dato guardado
     */
    @GetMapping("/{cedula}/puntos")
    public Respuesta<PuntosLicencia> puntos(@PathVariable String cedula,
                                            @RequestParam(required = false) String placa,
                                            @RequestParam(defaultValue = "false") boolean refrescar) {
        return servicio.consultar(cedula, placa, refrescar);
    }
}
