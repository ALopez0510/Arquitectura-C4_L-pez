package ec.udla.consultas.sri.dominio;

import com.fasterxml.jackson.databind.JsonNode;

/** Utilidades para leer campos opcionales de las respuestas del SRI. */
final class Json {

    private Json() {
    }

    static String texto(JsonNode nodo, String campo) {
        JsonNode v = nodo.path(campo);
        if (v.isMissingNode() || v.isNull()) {
            return null;
        }
        String t = v.asText().trim();
        return t.isEmpty() ? null : t;
    }

    static Integer entero(JsonNode nodo, String campo) {
        JsonNode v = nodo.path(campo);
        return v.canConvertToInt() ? v.asInt() : null;
    }

    /** "2004-12-30 12:20:08.0" → "2004-12-30" */
    static String fecha(JsonNode nodo, String campo) {
        String t = texto(nodo, campo);
        return t == null ? null : t.split(" ")[0];
    }
}
