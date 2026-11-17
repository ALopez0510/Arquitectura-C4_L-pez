package ec.udla.consultas.sri.dominio;

import com.fasterxml.jackson.databind.JsonNode;

/** Datos del contribuyente expuestos por la API (contrato propio, independiente del formato del SRI). */
public record Contribuyente(
        String ruc,
        String cedula,
        String razonSocial,
        String tipoContribuyente,
        String estado,
        String actividadEconomicaPrincipal,
        String regimen,
        String obligadoLlevarContabilidad,
        String fechaInicioActividades,
        String fechaActualizacion,
        String motivoCancelacionSuspension) {

    public boolean esPersonaNatural() {
        return "PERSONA NATURAL".equalsIgnoreCase(tipoContribuyente);
    }

    public static Contribuyente desdeSri(JsonNode n) {
        String ruc = Json.texto(n, "numeroRuc");
        JsonNode fechas = n.path("informacionFechasContribuyente");
        return new Contribuyente(
                ruc,
                ruc != null && ruc.length() >= 10 ? ruc.substring(0, 10) : null,
                Json.texto(n, "razonSocial"),
                Json.texto(n, "tipoContribuyente"),
                Json.texto(n, "estadoContribuyenteRuc"),
                Json.texto(n, "actividadEconomicaPrincipal"),
                Json.texto(n, "regimen"),
                Json.texto(n, "obligadoLlevarContabilidad"),
                Json.fecha(fechas, "fechaInicioActividades"),
                Json.fecha(fechas, "fechaActualizacion"),
                Json.texto(n, "motivoCancelacionSuspension"));
    }
}
