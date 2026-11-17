package ec.udla.consultas.common.validacion;

import ec.udla.consultas.common.api.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidadorIdentificacionTest {

    @Test
    void aceptaCedulaValida() {
        assertThat(ValidadorIdentificacion.validarCedula("1710034065", "cedula")).isEqualTo("1710034065");
    }

    @Test
    void rechazaCedulaConDigitoVerificadorIncorrecto() {
        assertThatThrownBy(() -> ValidadorIdentificacion.validarCedula("1710034064", "cedula"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("verificador");
    }

    @Test
    void aceptaRucDePersonaNaturalYDeSociedad() {
        assertThat(ValidadorIdentificacion.validarRuc("1710034065001", "ruc")).isEqualTo("1710034065001");
        // La sociedad pasa el formato; el SRI luego indica que no es persona natural (HTTP 422)
        assertThat(ValidadorIdentificacion.validarRuc("1790016919001", "ruc")).isEqualTo("1790016919001");
    }

    @Test
    void rechazaRucConLetras() {
        assertThatThrownBy(() -> ValidadorIdentificacion.validarRuc("17100340650AB", "ruc"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void normalizaPlaca() {
        assertThat(ValidadorIdentificacion.validarPlaca("pbx-1234", "placa")).isEqualTo("PBX1234");
        assertThat(ValidadorIdentificacion.validarPlaca("ab123c", "placa")).isEqualTo("AB123C");
    }
}
