package ec.udla.consultas.ant.dominio;

/**
 * Entrada del contrato con la ANT. Corresponde a los parámetros de la consulta web:
 * ps_tipo_identificacion, ps_identificacion y ps_placa.
 */
public record ConsultaPuntos(String tipoIdentificacion, String identificacion, String placa) {
}
