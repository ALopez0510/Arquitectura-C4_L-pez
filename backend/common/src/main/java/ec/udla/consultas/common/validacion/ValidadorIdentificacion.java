package ec.udla.consultas.common.validacion;

import ec.udla.consultas.common.api.ApiError.DetalleError;
import ec.udla.consultas.common.api.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validaciones de identificaciones ecuatorianas y placas.
 */
public final class ValidadorIdentificacion {

    private static final Pattern PLACA_AUTO = Pattern.compile("^[A-Z]{3}\\d{3,4}$");
    private static final Pattern PLACA_MOTO = Pattern.compile("^[A-Z]{2}\\d{3}[A-Z]$");

    private ValidadorIdentificacion() {
    }

    /** Valida una cédula ecuatoriana (10 dígitos, provincia válida y dígito verificador módulo 10). */
    public static String validarCedula(String valor, String campo) {
        String cedula = valor == null ? "" : valor.trim();
        if (!cedula.matches("\\d{10}")) {
            throw invalido("CEDULA_INVALIDA", campo, "La cédula debe tener exactamente 10 dígitos.");
        }
        if (!provinciaValida(cedula)) {
            throw invalido("CEDULA_INVALIDA", campo, "Los dos primeros dígitos no corresponden a una provincia.");
        }
        if (cedula.charAt(2) - '0' >= 6) {
            throw invalido("CEDULA_INVALIDA", campo, "El tercer dígito de una cédula debe ser menor a 6.");
        }
        if (!digitoVerificadorValido(cedula)) {
            throw invalido("CEDULA_INVALIDA", campo, "El dígito verificador no es válido.");
        }
        return cedula;
    }

    /**
     * Valida el formato de un RUC. Si el tercer dígito indica persona natural (menor a 6) se
     * comprueba además el dígito verificador de la cédula. Que realmente sea persona natural lo
     * confirma el SRI.
     */
    public static String validarRuc(String valor, String campo) {
        String ruc = valor == null ? "" : valor.trim();
        if (!ruc.matches("\\d{13}")) {
            throw invalido("RUC_INVALIDO", campo, "El RUC debe tener exactamente 13 dígitos numéricos.");
        }
        if (!provinciaValida(ruc)) {
            throw invalido("RUC_INVALIDO", campo, "Los dos primeros dígitos no corresponden a una provincia.");
        }
        if (ruc.endsWith("000")) {
            throw invalido("RUC_INVALIDO", campo, "Los tres últimos dígitos (establecimiento) no pueden ser 000.");
        }
        if (ruc.charAt(2) - '0' < 6 && !digitoVerificadorValido(ruc.substring(0, 10))) {
            throw invalido("RUC_INVALIDO", campo, "Los 10 primeros dígitos no forman una cédula válida.");
        }
        return ruc;
    }

    /** Normaliza (mayúsculas, sin guiones ni espacios) y valida una placa de auto o moto. */
    public static String validarPlaca(String valor, String campo) {
        String placa = valor == null ? "" : valor.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        if (!PLACA_AUTO.matcher(placa).matches() && !PLACA_MOTO.matcher(placa).matches()) {
            throw invalido("PLACA_INVALIDA", campo,
                    "La placa debe tener el formato ABC1234 (auto) o AB123C (moto).");
        }
        return placa;
    }

    private static boolean provinciaValida(String numero) {
        int provincia = Integer.parseInt(numero.substring(0, 2));
        return (provincia >= 1 && provincia <= 24) || provincia == 30;
    }

    private static boolean digitoVerificadorValido(String cedula) {
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int v = (cedula.charAt(i) - '0') * (i % 2 == 0 ? 2 : 1);
            suma += v > 9 ? v - 9 : v;
        }
        int verificador = (10 - suma % 10) % 10;
        return verificador == cedula.charAt(9) - '0';
    }

    private static ApiException invalido(String codigo, String campo, String mensaje) {
        return new ApiException(HttpStatus.BAD_REQUEST, codigo, mensaje, List.of(new DetalleError(campo, mensaje)));
    }
}
