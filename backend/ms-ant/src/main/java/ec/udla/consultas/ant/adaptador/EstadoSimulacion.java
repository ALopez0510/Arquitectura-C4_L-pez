package ec.udla.consultas.ant.adaptador;

/** Comportamiento del servicio ANT simulado. */
public enum EstadoSimulacion {
    /** Responde normalmente con datos de prueba. */
    DISPONIBLE,
    /** Falla igual que la ANT real el 17/11/2026: HTTP 502 Bad Gateway. */
    CAIDO,
    /** Responde más lento que el tiempo de espera configurado (provoca timeout). */
    LENTO
}
