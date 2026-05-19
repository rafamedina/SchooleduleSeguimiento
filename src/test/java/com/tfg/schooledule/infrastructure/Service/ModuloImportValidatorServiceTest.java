package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.*;

import com.tfg.schooledule.domain.dto.ModuloImportErrorDTO;
import com.tfg.schooledule.domain.dto.ModuloImportPreviewDTO;
import com.tfg.schooledule.domain.dto.ModuloImportRowDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModuloImportValidatorServiceTest {

  private final ModuloImportValidatorService validator = new ModuloImportValidatorService();

  private ModuloImportRowDTO filaValida(
      int numero,
      String raCod,
      String raDesc,
      String raPeso,
      String ceCod,
      String ceDesc,
      String cePeso) {
    return new ModuloImportRowDTO(
        numero,
        raCod,
        raDesc,
        raPeso != null ? new BigDecimal(raPeso) : null,
        ceCod,
        ceDesc,
        cePeso != null ? new BigDecimal(cePeso) : null,
        null,
        null,
        null);
  }

  @Test
  void validar_datosCompletos_sinErroresSinAdvertencias() {
    List<ModuloImportRowDTO> filas =
        List.of(
            filaValida(1, "RA1", "Desc RA1", "50", "1a", "Desc CE 1a", "50"),
            filaValida(2, "RA1", "Desc RA1", "50", "1b", "Desc CE 1b", "50"),
            filaValida(3, "RA2", "Desc RA2", "50", "2a", "Desc CE 2a", "100"));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isTrue();
    assertThat(resultado.errores()).isEmpty();
    assertThat(resultado.advertencias()).isEmpty();
    assertThat(resultado.totalRas()).isEqualTo(2);
    assertThat(resultado.totalCes()).isEqualTo(3);
  }

  @Test
  void validar_listaVacia_errorGlobalFila0() {
    ModuloImportPreviewDTO resultado = validator.validar(List.of());

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).hasSize(1);
    ModuloImportErrorDTO err = resultado.errores().get(0);
    assertThat(err.fila()).isZero();
    assertThat(err.mensaje()).contains("no contiene filas");
  }

  @Test
  void validar_raCodigoBlanco_errorConFilaYCampo() {
    List<ModuloImportRowDTO> filas =
        List.of(filaValida(1, "  ", "Desc RA1", "50", "1a", "Desc CE 1a", "50"));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).anyMatch(e -> e.fila() == 1 && e.campo().equals("ra_codigo"));
  }

  @Test
  void validar_raPesoNulo_errorEnFila() {
    List<ModuloImportRowDTO> filas =
        List.of(
            new ModuloImportRowDTO(
                1, "RA1", "Desc", null, "1a", "Desc CE", new BigDecimal("50"), null, null, null));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).anyMatch(e -> e.fila() == 1 && e.campo().equals("ra_peso"));
  }

  @Test
  void validar_raPesoFueraDeRango_errorEnFila() {
    List<ModuloImportRowDTO> filasConCero =
        List.of(filaValida(1, "RA1", "Desc", "0", "1a", "Desc CE", "50"));
    List<ModuloImportRowDTO> filasConSobreRango =
        List.of(filaValida(1, "RA1", "Desc", "101", "1a", "Desc CE", "50"));

    assertThat(validator.validar(filasConCero).valido()).isFalse();
    assertThat(validator.validar(filasConSobreRango).valido()).isFalse();
  }

  @Test
  void validar_ceCodigoBlanco_errorEnFila() {
    List<ModuloImportRowDTO> filas =
        List.of(filaValida(1, "RA1", "Desc RA1", "50", "", "Desc CE 1a", "50"));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).anyMatch(e -> e.fila() == 1 && e.campo().equals("ce_codigo"));
  }

  @Test
  void validar_cePesoNulo_errorEnFila() {
    List<ModuloImportRowDTO> filas =
        List.of(
            new ModuloImportRowDTO(
                1, "RA1", "Desc", new BigDecimal("50"), "1a", "Desc CE", null, null, null, null));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).anyMatch(e -> e.fila() == 1 && e.campo().equals("ce_peso"));
  }

  @Test
  void validar_instrumentoInvalido_errorEnFila() {
    List<ModuloImportRowDTO> filas =
        List.of(
            new ModuloImportRowDTO(
                1,
                "RA1",
                "Desc",
                new BigDecimal("50"),
                "1a",
                "Desc CE",
                new BigDecimal("50"),
                "INSTRUMENTO_INVALIDO",
                null,
                null));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).anyMatch(e -> e.fila() == 1 && e.campo().equals("instrumento"));
  }

  @Test
  void validar_instrumentoVacio_noEsError() {
    List<ModuloImportRowDTO> filas =
        List.of(
            new ModuloImportRowDTO(
                1,
                "RA1",
                "Desc",
                new BigDecimal("100"),
                "1a",
                "Desc CE",
                new BigDecimal("100"),
                null,
                null,
                null));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isTrue();
  }

  @Test
  void validar_trimestreInvalido_errorEnFila() {
    List<ModuloImportRowDTO> filas =
        List.of(
            new ModuloImportRowDTO(
                1,
                "RA1",
                "Desc",
                new BigDecimal("100"),
                "1a",
                "Desc CE",
                new BigDecimal("100"),
                null,
                null,
                3));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).anyMatch(e -> e.fila() == 1 && e.campo().equals("trimestre"));
  }

  @Test
  void validar_trimestreNulo_noEsError() {
    List<ModuloImportRowDTO> filas =
        List.of(
            new ModuloImportRowDTO(
                1,
                "RA1",
                "Desc",
                new BigDecimal("100"),
                "1a",
                "Desc CE",
                new BigDecimal("100"),
                null,
                null,
                null));

    assertThat(validator.validar(filas).valido()).isTrue();
  }

  @Test
  void validar_ceDuplicadoEnMismoRa_errorEnFila() {
    List<ModuloImportRowDTO> filas =
        List.of(
            filaValida(1, "RA1", "Desc RA1", "50", "1a", "Desc CE 1a", "50"),
            filaValida(2, "RA1", "Desc RA1", "50", "1a", "Desc CE 1a dup", "50"));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores())
        .anyMatch(e -> e.campo().equals("ce_codigo") && e.mensaje().contains("1a"));
  }

  @Test
  void validar_ceDuplicadoEnRasDistintos_noEsError() {
    List<ModuloImportRowDTO> filas =
        List.of(
            filaValida(1, "RA1", "Desc RA1", "50", "1a", "Desc CE 1a", "100"),
            filaValida(2, "RA2", "Desc RA2", "50", "1a", "Desc CE 2a", "100"));

    assertThat(validator.validar(filas).valido()).isTrue();
  }

  @Test
  void validar_raDescripcionInconsistente_errorEnFila() {
    List<ModuloImportRowDTO> filas =
        List.of(
            filaValida(1, "RA1", "Descripción A", "50", "1a", "Desc CE 1a", "50"),
            filaValida(2, "RA1", "Descripción B", "50", "1b", "Desc CE 1b", "50"));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores())
        .anyMatch(e -> e.campo().equals("ra_descripcion") && e.mensaje().contains("RA1"));
  }

  @Test
  void validar_raPesoInconsistente_errorEnFila() {
    List<ModuloImportRowDTO> filas =
        List.of(
            filaValida(1, "RA1", "Desc RA1", "50", "1a", "Desc CE 1a", "50"),
            filaValida(2, "RA1", "Desc RA1", "60", "1b", "Desc CE 1b", "50"));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores())
        .anyMatch(e -> e.campo().equals("ra_peso") && e.mensaje().contains("RA1"));
  }

  @Test
  void validar_sumaPesosRaNoEsCien_soloAdvertencia() {
    List<ModuloImportRowDTO> filas =
        List.of(
            filaValida(1, "RA1", "Desc RA1", "40", "1a", "Desc CE 1a", "100"),
            filaValida(2, "RA2", "Desc RA2", "40", "2a", "Desc CE 2a", "100"));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isTrue();
    assertThat(resultado.advertencias()).anyMatch(a -> a.contains("80") && a.contains("RA"));
  }

  @Test
  void validar_sumaPesosCeNoEsCien_soloAdvertencia() {
    List<ModuloImportRowDTO> filas =
        List.of(
            filaValida(1, "RA1", "Desc RA1", "100", "1a", "Desc CE 1a", "40"),
            filaValida(2, "RA1", "Desc RA1", "100", "1b", "Desc CE 1b", "40"));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isTrue();
    assertThat(resultado.advertencias()).anyMatch(a -> a.contains("80") && a.contains("RA1"));
  }

  @Test
  void validar_superaLimiteRas_errorGlobal() {
    List<ModuloImportRowDTO> filas = new ArrayList<>();
    for (int i = 1; i <= 16; i++) {
      filas.add(filaValida(i, "RA" + i, "Desc RA" + i, "5", i + "a", "Desc CE", "100"));
    }

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).anyMatch(e -> e.fila() == 0 && e.mensaje().contains("15 RAs"));
  }

  @Test
  void validar_superaLimiteCes_errorGlobal() {
    List<ModuloImportRowDTO> filas = new ArrayList<>();
    for (int i = 1; i <= 101; i++) {
      filas.add(
          new ModuloImportRowDTO(
              i,
              "RA1",
              "Desc RA1",
              new BigDecimal("100"),
              "CE" + i,
              "Desc CE " + i,
              new BigDecimal("1"),
              null,
              null,
              null));
    }

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).anyMatch(e -> e.fila() == 0 && e.mensaje().contains("100 CEs"));
  }

  @Test
  void validar_multipleErrores_devuelveTodos() {
    List<ModuloImportRowDTO> filas =
        List.of(
            filaValida(1, "", "Desc RA1", "50", "1a", "Desc CE 1a", "50"),
            filaValida(2, "RA2", "Desc RA2", "0", "2a", "Desc CE 2a", "50"),
            filaValida(3, "RA3", "Desc RA3", "50", "3a", "", "50"));

    ModuloImportPreviewDTO resultado = validator.validar(filas);

    assertThat(resultado.valido()).isFalse();
    assertThat(resultado.errores()).hasSizeGreaterThanOrEqualTo(3);
  }
}
