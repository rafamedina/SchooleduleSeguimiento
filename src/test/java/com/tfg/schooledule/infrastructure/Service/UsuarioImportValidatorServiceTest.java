package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tfg.schooledule.domain.dto.UsuarioImportErrorDTO;
import com.tfg.schooledule.domain.dto.UsuarioImportPreviewDTO;
import com.tfg.schooledule.domain.dto.UsuarioImportRowDTO;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class UsuarioImportValidatorServiceTest {

  private final UsuarioImportValidatorService validator = new UsuarioImportValidatorService();

  private UsuarioImportRowDTO filaValida(int fila, String username) {
    return new UsuarioImportRowDTO(
        fila,
        username,
        "Juan",
        "García",
        null,
        "Abc12345",
        "IES La Madraza",
        "2025/2026",
        "DAW 1A",
        null);
  }

  private UsuarioImportRowDTO filaValida(String username) {
    return filaValida(1, username);
  }

  // ── Lista vacía ──────────────────────────────────────────────────────────

  @Test
  void validar_listaVacia_errorGlobalFila0() {
    UsuarioImportPreviewDTO result = validator.validar(List.of());

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).hasSize(1);
    assertThat(result.errores().get(0).fila()).isZero();
    assertThat(result.errores().get(0).campo()).isEqualTo("archivo");
  }

  // ── Datos válidos ─────────────────────────────────────────────────────────

  @Test
  void validar_datosCompletosValidos_sinErrores() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "user1"), filaValida(2, "user2"));

    UsuarioImportPreviewDTO result = validator.validar(filas);

    assertThat(result.valido()).isTrue();
    assertThat(result.errores()).isEmpty();
    assertThat(result.totalUsuarios()).isEqualTo(2);
  }

  // ── Username ──────────────────────────────────────────────────────────────

  @Test
  void validar_usernameBlanco_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "", "Juan", "García", null, "Abc12345", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.fila() == 1 && e.campo().equals("username"));
  }

  @Test
  void validar_usernameNulo_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, null, "Juan", "García", null, "Abc12345", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("username"));
  }

  @Test
  void validar_usernameConEspacios_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1,
            "juan garcia",
            "Juan",
            "García",
            null,
            "Abc12345",
            "Centro",
            "2025/2026",
            "Grupo",
            null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("username"));
  }

  @Test
  void validar_usernameDuplicadoEnLote_errorEnSegundaFila() {
    List<UsuarioImportRowDTO> filas = List.of(filaValida(1, "alumno01"), filaValida(2, "alumno01"));

    UsuarioImportPreviewDTO result = validator.validar(filas);

    assertThat(result.valido()).isFalse();
    assertThat(result.errores())
        .anyMatch(
            e -> e.fila() == 2 && e.campo().equals("username") && e.mensaje().contains("alumno01"));
  }

  // ── Nombre / Apellidos ────────────────────────────────────────────────────

  @Test
  void validar_nombreBlanco_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "", "García", null, "Abc12345", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("nombre"));
  }

  @Test
  void validar_apellidosBlanco_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "", null, "Abc12345", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("apellidos"));
  }

  // ── Email ─────────────────────────────────────────────────────────────────

  @Test
  void validar_emailFormatoInvalido_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1,
            "user1",
            "Juan",
            "García",
            "noesvalido",
            "Abc12345",
            "Centro",
            "2025/2026",
            "Grupo",
            null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("email"));
  }

  @Test
  void validar_emailFormatoValido_noEsError() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1,
            "user1",
            "Juan",
            "García",
            "user@dominio.es",
            "Abc12345",
            "Centro",
            "2025/2026",
            "Grupo",
            null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.errores()).noneMatch(e -> e.campo().equals("email"));
  }

  @Test
  void validar_emailNulo_noEsError() {
    UsuarioImportPreviewDTO result = validator.validar(List.of(filaValida("user1")));

    assertThat(result.errores()).noneMatch(e -> e.campo().equals("email"));
  }

  @Test
  void validar_emailDuplicadoEnLote_errorEnSegundaFila() {
    UsuarioImportRowDTO f1 =
        new UsuarioImportRowDTO(
            1,
            "user1",
            "Juan",
            "García",
            "mismo@email.es",
            "Abc12345",
            "Centro",
            "2025/2026",
            "Grupo",
            null);
    UsuarioImportRowDTO f2 =
        new UsuarioImportRowDTO(
            2,
            "user2",
            "Ana",
            "López",
            "mismo@email.es",
            "Abc12345",
            "Centro",
            "2025/2026",
            "Grupo",
            null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(f1, f2));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores())
        .anyMatch(
            e ->
                e.fila() == 2
                    && e.campo().equals("email")
                    && e.mensaje().contains("mismo@email.es"));
  }

  @Test
  void validar_emailNuloNoDuplicaConNulo() {
    UsuarioImportRowDTO f1 =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "García", null, "Abc12345", "Centro", "2025/2026", "Grupo", null);
    UsuarioImportRowDTO f2 =
        new UsuarioImportRowDTO(
            2, "user2", "Ana", "López", null, "Abc12345", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(f1, f2));

    assertThat(result.errores()).noneMatch(e -> e.campo().equals("email"));
  }

  // ── Password ──────────────────────────────────────────────────────────────

  @Test
  void validar_passwordBlanco_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "García", null, "", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("password"));
  }

  @Test
  void validar_passwordCorto_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "García", null, "Ab1", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("password"));
  }

  @Test
  void validar_passwordSinMayuscula_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "García", null, "abcdefg1", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("password"));
  }

  @Test
  void validar_passwordSinMinuscula_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "García", null, "ABCDEFG1", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("password"));
  }

  @Test
  void validar_passwordSinDigito_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "García", null, "Abcdefgh", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("password"));
  }

  @Test
  void validar_passwordValido_noEsError() {
    UsuarioImportPreviewDTO result = validator.validar(List.of(filaValida("user1")));

    assertThat(result.errores()).noneMatch(e -> e.campo().equals("password"));
  }

  // ── Centro / Curso / Grupo ────────────────────────────────────────────────

  @Test
  void validar_centroNombreBlanco_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "García", null, "Abc12345", "", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("centro_nombre"));
  }

  @Test
  void validar_cursoNombreBlanco_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "García", null, "Abc12345", "Centro", "", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("curso_academico_nombre"));
  }

  @Test
  void validar_grupoNombreBlanco_errorEnFila() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, "user1", "Juan", "García", null, "Abc12345", "Centro", "2025/2026", "", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.campo().equals("grupo_nombre"));
  }

  // ── Límites ───────────────────────────────────────────────────────────────

  @Test
  void validar_superaLimite200_errorGlobal() {
    List<UsuarioImportRowDTO> filas = new ArrayList<>();
    for (int i = 1; i <= 201; i++) {
      filas.add(filaValida(i, "user" + i));
    }

    UsuarioImportPreviewDTO result = validator.validar(filas);

    assertThat(result.valido()).isFalse();
    assertThat(result.errores()).anyMatch(e -> e.fila() == 0);
  }

  // ── Múltiples errores ─────────────────────────────────────────────────────

  @Test
  void validar_multiplesErroresEnMismaFila_devuelveTodos() {
    UsuarioImportRowDTO fila =
        new UsuarioImportRowDTO(
            1, null, "", "García", null, "Abc12345", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(fila));

    assertThat(result.valido()).isFalse();
    assertThat(result.errores().stream().filter(e -> e.fila() == 1).count())
        .isGreaterThanOrEqualTo(2);
  }

  @Test
  void validar_multiplesFilasConErrores_devuelveTodos() {
    UsuarioImportRowDTO f1 =
        new UsuarioImportRowDTO(
            1, null, "Juan", "García", null, "Abc12345", "Centro", "2025/2026", "Grupo", null);
    UsuarioImportRowDTO f3 =
        new UsuarioImportRowDTO(
            3, "user3", "Ana", "López", null, "corta", "Centro", "2025/2026", "Grupo", null);

    UsuarioImportPreviewDTO result = validator.validar(List.of(f1, f3));

    assertThat(result.errores().stream().map(UsuarioImportErrorDTO::fila)).contains(1, 3);
  }
}
