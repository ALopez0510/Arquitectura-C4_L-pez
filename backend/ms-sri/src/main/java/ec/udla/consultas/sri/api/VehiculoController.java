package ec.udla.consultas.sri.api;

import ec.udla.consultas.common.api.Respuesta;
import ec.udla.consultas.sri.dominio.Vehiculo;
import ec.udla.consultas.sri.servicio.ServicioSri;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehiculos")
public class VehiculoController {

    private final ServicioSri servicio;

    public VehiculoController(ServicioSri servicio) {
        this.servicio = servicio;
    }

    /** 200 OK | 400 placa inválida | 404 vehículo no existe | 503 SRI caído sin respaldo */
    @GetMapping("/{placa}")
    public Respuesta<Vehiculo> obtener(@PathVariable String placa,
                                       @RequestParam(defaultValue = "false") boolean refrescar) {
        return servicio.consultarVehiculo(placa, refrescar);
    }
}
