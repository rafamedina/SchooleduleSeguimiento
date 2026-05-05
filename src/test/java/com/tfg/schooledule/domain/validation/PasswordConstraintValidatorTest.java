package com.tfg.schooledule.domain.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordConstraintValidatorTest {

  private final PasswordConstraintValidator validator = new PasswordConstraintValidator();

  @Test
  void null_esValido_paraPermitirEdicionSinCambio() {
    assertThat(validator.isValid(null, null)).isTrue();
  }

  @Test
  void cadenaVacia_esValida_paraPermitirEdicionSinCambio() {
    assertThat(validator.isValid("", null)).isTrue();
  }

  @Test
  void cadenaBlank_esValida_paraPermitirEdicionSinCambio() {
    assertThat(validator.isValid("   ", null)).isTrue();
  }

  @Test
  void contrasenyaMuyCorta_esInvalida() {
    assertThat(validator.isValid("abc", null)).isFalse();
  }

  @Test
  void sinMayusculasNiDigito_esInvalida() {
    assertThat(validator.isValid("password", null)).isFalse();
  }

  @Test
  void sinDigito_esInvalida() {
    assertThat(validator.isValid("Password", null)).isFalse();
  }

  @Test
  void sinMayuscula_esInvalida() {
    assertThat(validator.isValid("password1", null)).isFalse();
  }

  @Test
  void cumpleTodosLosRequisitos_esValida() {
    assertThat(validator.isValid("Password1", null)).isTrue();
  }

  @Test
  void contrasenyaCompleja_esValida() {
    assertThat(validator.isValid("MiClave123", null)).isTrue();
  }
}
