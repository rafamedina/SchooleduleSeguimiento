package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.ModuloImportErrorDTO;
import com.tfg.schooledule.domain.dto.ModuloImportPreviewDTO;
import com.tfg.schooledule.domain.dto.ModuloImportRowDTO;
import com.tfg.schooledule.domain.entity.CriterioEvaluacion;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.domain.entity.ResultadoAprendizaje;
import com.tfg.schooledule.domain.enums.InstrumentoEvaluacion;
import com.tfg.schooledule.domain.exception.ModuloImportException;
import com.tfg.schooledule.infrastructure.repository.CriterioEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModuloImportServiceTest {

  @Mock private ModuloExcelParserService parser;
  @Mock private ModuloImportValidatorService validatorService;
  @Mock private ModuloRepository moduloRepository;
  @Mock private CursoAcademicoRepository cursoAcademicoRepository;
  @Mock private ResultadoAprendizajeRepository raRepository;
  @Mock private CriterioEvaluacionRepository ceRepository;

  @InjectMocks private ModuloImportService importService;

  private static final byte[] BYTES_DUMMY = new byte[] {1, 2, 3};
  private static final Integer MODULO_ID = 10;
  private static final Integer CURSO_ID = 5;

  private List<ModuloImportRowDTO> dosFilasValidas() {
    return List.of(
        new ModuloImportRowDTO(
            1,
            "RA1",
            "Desc RA1",
            new BigDecimal("50"),
            "1a",
            "Desc CE 1a",
            new BigDecimal("50"),
            null,
            null,
            null),
        new ModuloImportRowDTO(
            2,
            "RA1",
            "Desc RA1",
            new BigDecimal("50"),
            "1b",
            "Desc CE 1b",
            new BigDecimal("50"),
            null,
            null,
            null));
  }

  private ModuloImportPreviewDTO previewValido(int totalRas, int totalCes) {
    return new ModuloImportPreviewDTO(true, List.of(), List.of(), totalRas, totalCes);
  }

  private Modulo moduloFake() {
    Modulo m = new Modulo();
    m.setId(MODULO_ID);
    return m;
  }

  private CursoAcademico cursoFake() {
    CursoAcademico c = new CursoAcademico();
    c.setId(CURSO_ID);
    return c;
  }

  private ResultadoAprendizaje raGuardado() {
    ResultadoAprendizaje ra = new ResultadoAprendizaje();
    ra.setId(100);
    return ra;
  }

  @Test
  void importar_archivoValido_persisteRasYCes() {
    List<ModuloImportRowDTO> filas = dosFilasValidas();
    when(parser.parsear(BYTES_DUMMY)).thenReturn(filas);
    when(validatorService.validar(filas)).thenReturn(previewValido(1, 2));
    when(moduloRepository.findById(MODULO_ID)).thenReturn(Optional.of(moduloFake()));
    when(cursoAcademicoRepository.findById(CURSO_ID)).thenReturn(Optional.of(cursoFake()));
    when(raRepository.existsByModuloIdAndCursoAcademicoId(MODULO_ID, CURSO_ID)).thenReturn(false);
    when(raRepository.save(any())).thenReturn(raGuardado());
    when(ceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    importService.importar(BYTES_DUMMY, MODULO_ID, CURSO_ID);

    verify(raRepository, times(1)).save(any(ResultadoAprendizaje.class));
    verify(ceRepository, times(2)).save(any(CriterioEvaluacion.class));
  }

  @Test
  void importar_archivoValido_devuelveNumeroDeCesCreados() {
    List<ModuloImportRowDTO> filas = dosFilasValidas();
    when(parser.parsear(BYTES_DUMMY)).thenReturn(filas);
    when(validatorService.validar(filas)).thenReturn(previewValido(1, 2));
    when(moduloRepository.findById(MODULO_ID)).thenReturn(Optional.of(moduloFake()));
    when(cursoAcademicoRepository.findById(CURSO_ID)).thenReturn(Optional.of(cursoFake()));
    when(raRepository.existsByModuloIdAndCursoAcademicoId(MODULO_ID, CURSO_ID)).thenReturn(false);
    when(raRepository.save(any())).thenReturn(raGuardado());
    when(ceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    int result = importService.importar(BYTES_DUMMY, MODULO_ID, CURSO_ID);

    assertThat(result).isEqualTo(2);
  }

  @Test
  void importar_moduloNoExiste_lanzaEntityNotFoundException() {
    List<ModuloImportRowDTO> filas = dosFilasValidas();
    when(parser.parsear(BYTES_DUMMY)).thenReturn(filas);
    when(validatorService.validar(filas)).thenReturn(previewValido(1, 2));
    when(moduloRepository.findById(MODULO_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> importService.importar(BYTES_DUMMY, MODULO_ID, CURSO_ID))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void importar_cursoNoExiste_lanzaEntityNotFoundException() {
    List<ModuloImportRowDTO> filas = dosFilasValidas();
    when(parser.parsear(BYTES_DUMMY)).thenReturn(filas);
    when(validatorService.validar(filas)).thenReturn(previewValido(1, 2));
    when(moduloRepository.findById(MODULO_ID)).thenReturn(Optional.of(moduloFake()));
    when(cursoAcademicoRepository.findById(CURSO_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> importService.importar(BYTES_DUMMY, MODULO_ID, CURSO_ID))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void importar_rasYaExisteParaModuloCurso_lanzaIllegalStateException() {
    List<ModuloImportRowDTO> filas = dosFilasValidas();
    when(parser.parsear(BYTES_DUMMY)).thenReturn(filas);
    when(validatorService.validar(filas)).thenReturn(previewValido(1, 2));
    when(moduloRepository.findById(MODULO_ID)).thenReturn(Optional.of(moduloFake()));
    when(cursoAcademicoRepository.findById(CURSO_ID)).thenReturn(Optional.of(cursoFake()));
    when(raRepository.existsByModuloIdAndCursoAcademicoId(MODULO_ID, CURSO_ID)).thenReturn(true);

    assertThatThrownBy(() -> importService.importar(BYTES_DUMMY, MODULO_ID, CURSO_ID))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Ya existen RAs");
  }

  @Test
  void importar_archivoConErroresDeValidacion_lanzaModuloImportException() {
    List<ModuloImportRowDTO> filas = dosFilasValidas();
    ModuloImportPreviewDTO previewInvalido =
        new ModuloImportPreviewDTO(
            false,
            List.of(new ModuloImportErrorDTO(1, "ra_codigo", "El código de RA es obligatorio")),
            List.of(),
            0,
            0);
    when(parser.parsear(BYTES_DUMMY)).thenReturn(filas);
    when(validatorService.validar(filas)).thenReturn(previewInvalido);

    assertThatThrownBy(() -> importService.importar(BYTES_DUMMY, MODULO_ID, CURSO_ID))
        .isInstanceOf(ModuloImportException.class);
  }

  @Test
  void importar_instrumentoTextoLibre_convierteAEnum() {
    List<ModuloImportRowDTO> filas =
        List.of(
            new ModuloImportRowDTO(
                1,
                "RA1",
                "Desc RA1",
                new BigDecimal("100"),
                "1a",
                "Desc CE 1a",
                new BigDecimal("100"),
                "PRUEBA_OBJETIVA",
                null,
                null));
    when(parser.parsear(BYTES_DUMMY)).thenReturn(filas);
    when(validatorService.validar(filas)).thenReturn(previewValido(1, 1));
    when(moduloRepository.findById(MODULO_ID)).thenReturn(Optional.of(moduloFake()));
    when(cursoAcademicoRepository.findById(CURSO_ID)).thenReturn(Optional.of(cursoFake()));
    when(raRepository.existsByModuloIdAndCursoAcademicoId(MODULO_ID, CURSO_ID)).thenReturn(false);
    when(raRepository.save(any())).thenReturn(raGuardado());
    ArgumentCaptor<CriterioEvaluacion> captor = ArgumentCaptor.forClass(CriterioEvaluacion.class);
    when(ceRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

    importService.importar(BYTES_DUMMY, MODULO_ID, CURSO_ID);

    assertThat(captor.getValue().getInstrumento()).isEqualTo(InstrumentoEvaluacion.PRUEBA_OBJETIVA);
  }

  @Test
  void importar_camposOpcionalesNulos_persisteConNullEnCe() {
    List<ModuloImportRowDTO> filas =
        List.of(
            new ModuloImportRowDTO(
                1,
                "RA1",
                "Desc RA1",
                new BigDecimal("100"),
                "1a",
                "Desc CE 1a",
                new BigDecimal("100"),
                null,
                null,
                null));
    when(parser.parsear(BYTES_DUMMY)).thenReturn(filas);
    when(validatorService.validar(filas)).thenReturn(previewValido(1, 1));
    when(moduloRepository.findById(MODULO_ID)).thenReturn(Optional.of(moduloFake()));
    when(cursoAcademicoRepository.findById(CURSO_ID)).thenReturn(Optional.of(cursoFake()));
    when(raRepository.existsByModuloIdAndCursoAcademicoId(MODULO_ID, CURSO_ID)).thenReturn(false);
    when(raRepository.save(any())).thenReturn(raGuardado());
    ArgumentCaptor<CriterioEvaluacion> captor = ArgumentCaptor.forClass(CriterioEvaluacion.class);
    when(ceRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

    importService.importar(BYTES_DUMMY, MODULO_ID, CURSO_ID);

    assertThat(captor.getValue().getInstrumento()).isNull();
    assertThat(captor.getValue().getTrimestre()).isNull();
  }

  @Test
  void importar_archivoBinarioInvalido_lanzaIllegalArgumentException() {
    when(parser.parsear(BYTES_DUMMY)).thenThrow(new IllegalArgumentException("No es xlsx"));

    assertThatThrownBy(() -> importService.importar(BYTES_DUMMY, MODULO_ID, CURSO_ID))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validarArchivo_sinPersistir_devuelvePreview() {
    List<ModuloImportRowDTO> filas = dosFilasValidas();
    ModuloImportPreviewDTO preview = previewValido(1, 2);
    when(parser.parsear(BYTES_DUMMY)).thenReturn(filas);
    when(validatorService.validar(filas)).thenReturn(preview);

    ModuloImportPreviewDTO resultado =
        importService.validarArchivo(BYTES_DUMMY, MODULO_ID, CURSO_ID);

    assertThat(resultado).isEqualTo(preview);
    verify(moduloRepository, never()).findById(any());
    verify(raRepository, never()).save(any());
    verify(ceRepository, never()).save(any());
  }
}
