package ec.udla.consultas.common.api;

/** Envoltura de todas las respuestas exitosas: los datos y sus metadatos de frescura. */
public record Respuesta<T>(T datos, Metadatos metadatos) {
}
