package ec.udla.consultas.sri.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /api/v1/contribuyentes/verificacion */
public record SolicitudVerificacion(
        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$", message = "El correo electrónico no tiene un formato válido.")
        @Size(max = 254, message = "El correo electrónico no puede superar 254 caracteres.")
        String correo,

        @NotBlank(message = "El RUC es obligatorio.")
        String ruc) {
}
