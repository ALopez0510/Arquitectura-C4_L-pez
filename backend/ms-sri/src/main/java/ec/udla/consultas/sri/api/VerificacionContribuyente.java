package ec.udla.consultas.sri.api;

import ec.udla.consultas.sri.dominio.Contribuyente;

/** Resultado de la verificación: el correo validado y el contribuyente persona natural. */
public record VerificacionContribuyente(String correo, Contribuyente contribuyente) {
}
