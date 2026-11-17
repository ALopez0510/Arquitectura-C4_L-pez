package ec.udla.consultas.sri.dominio;

import com.fasterxml.jackson.databind.JsonNode;

/** Datos del vehículo expuestos por la API. */
public record Vehiculo(
        String placa,
        String camvCpn,
        String marca,
        String modelo,
        Integer anio,
        String paisOrigen,
        String clase,
        String servicio,
        String color,
        Integer cilindraje,
        Integer ultimoAnioPagado,
        String fechaUltimaMatricula,
        String fechaCaducidadMatricula,
        String canton) {

    public static Vehiculo desdeSri(JsonNode n) {
        return new Vehiculo(
                Json.texto(n, "numeroPlaca"),
                Json.texto(n, "numeroCamvCpn"),
                Json.texto(n, "descripcionMarca"),
                Json.texto(n, "descripcionModelo"),
                Json.entero(n, "anioAuto"),
                Json.texto(n, "descripcionPais"),
                Json.texto(n, "nombreClase"),
                Json.texto(n, "descripcionServicio"),
                Json.texto(n, "colorVehiculo1"),
                Json.entero(n, "cilindraje"),
                Json.entero(n, "ultimoAnioPagado"),
                Json.fecha(n, "fechaUltimaMatricula"),
                Json.fecha(n, "fechaCaducidadMatricula"),
                Json.texto(n, "descripcionCanton"));
    }
}
