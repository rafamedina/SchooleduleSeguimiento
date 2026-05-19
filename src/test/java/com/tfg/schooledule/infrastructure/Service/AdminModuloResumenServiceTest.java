package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.tfg.schooledule.domain.dto.AdminModuloResumenDTO;
import com.tfg.schooledule.domain.entity.CriterioEvaluacion;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.domain.entity.Modulo;
import com.tfg.schooledule.domain.entity.ResultadoAprendizaje;
import com.tfg.schooledule.domain.enums.InstrumentoEvaluacion;
import com.tfg.schooledule.infrastructure.repository.CriterioEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminModuloResumenServiceTest {

  @Mock private ModuloRepository moduloRepository;
  @Mock private ImparticionRepository imparticionRepository;
  @Mock private ResultadoAprendizajeRepository raRepository;
  @Mock private CriterioEvaluacionRepository ceRepository;
  @Mock private ModuloImportService moduloImportService;

  @InjectMocks private AdminModuloService service;

  private Modulo modulo(int id) {
    return Modulo.builder().id(id).codigo("M" + id).nombre("Módulo " + id).activo(true).build();
  }

  private CursoAcademico curso(int id, String nombre) {
    return CursoAcademico.builder()
        .id(id)
        .nombre(nombre)
        .fechaInicio(LocalDate.of(2025, 9, 1))
        .fechaFin(LocalDate.of(2026, 6, 30))
        .activo(true)
        .build();
  }

  private ResultadoAprendizaje ra(int id, Modulo m, CursoAcademico c, String codigo) {
    return ResultadoAprendizaje.builder()
        .id(id)
        .modulo(m)
        .cursoAcademico(c)
        .codigo(codigo)
        .descripcion("Descripción " + codigo)
        .pesoSugerido(new BigDecimal("25.00"))
        .build();
  }

  private CriterioEvaluacion ce(int id, ResultadoAprendizaje ra, String codigo) {
    return CriterioEvaluacion.builder()
        .id(id)
        .resultadoAprendizaje(ra)
        .codigo(codigo)
        .descripcion("Criterio " + codigo)
        .peso(new BigDecimal("33.33"))
        .instrumento(InstrumentoEvaluacion.PRUEBA_OBJETIVA)
        .build();
  }

  @Test
  void getResumen_moduloInexistente_lanzaEntityNotFoundException() {
    when(moduloRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getResumen(99))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("99");
  }

  @Test
  void getResumen_moduloSinRas_devuelveConteosCero() {
    Modulo m = modulo(1);
    when(moduloRepository.findById(1)).thenReturn(Optional.of(m));
    when(imparticionRepository.countByModuloId(1)).thenReturn(2);
    when(raRepository.findByModuloIdOrderByCursoAcademicoIdAscCodigoAsc(1)).thenReturn(List.of());

    AdminModuloResumenDTO result = service.getResumen(1);

    assertThat(result.numRasTotal()).isZero();
    assertThat(result.numCesTotal()).isZero();
    assertThat(result.numCursosConRas()).isZero();
    assertThat(result.numImparticiones()).isEqualTo(2);
    assertThat(result.cursos()).isEmpty();
  }

  @Test
  void getResumen_moduloConRasYCes_agrupaCorrectamentePorCurso() {
    Modulo m = modulo(1);
    CursoAcademico c1 = curso(1, "2024/2025");
    CursoAcademico c2 = curso(2, "2025/2026");

    ResultadoAprendizaje ra1 = ra(1, m, c1, "RA1");
    ResultadoAprendizaje ra2 = ra(2, m, c2, "RA1");
    ResultadoAprendizaje ra3 = ra(3, m, c2, "RA2");

    CriterioEvaluacion ce1a = ce(1, ra1, "a");
    CriterioEvaluacion ce2a = ce(2, ra2, "a");
    CriterioEvaluacion ce2b = ce(3, ra2, "b");

    when(moduloRepository.findById(1)).thenReturn(Optional.of(m));
    when(imparticionRepository.countByModuloId(1)).thenReturn(0);
    when(raRepository.findByModuloIdOrderByCursoAcademicoIdAscCodigoAsc(1))
        .thenReturn(List.of(ra1, ra2, ra3));
    when(ceRepository.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(
            any()))
        .thenReturn(List.of(ce1a, ce2a, ce2b));

    AdminModuloResumenDTO result = service.getResumen(1);

    assertThat(result.numCursosConRas()).isEqualTo(2);
    assertThat(result.numRasTotal()).isEqualTo(3);
    assertThat(result.numCesTotal()).isEqualTo(3);
    assertThat(result.cursos()).hasSize(2);

    var cursoDto1 = result.cursos().get(0);
    assertThat(cursoDto1.cursoNombre()).isEqualTo("2024/2025");
    assertThat(cursoDto1.numRas()).isEqualTo(1);
    assertThat(cursoDto1.numCes()).isEqualTo(1);
    assertThat(cursoDto1.ras()).hasSize(1);
    assertThat(cursoDto1.ras().get(0).ces()).hasSize(1);

    var cursoDto2 = result.cursos().get(1);
    assertThat(cursoDto2.cursoNombre()).isEqualTo("2025/2026");
    assertThat(cursoDto2.numRas()).isEqualTo(2);
    assertThat(cursoDto2.numCes()).isEqualTo(2);
  }

  @Test
  void getResumen_conteosTotalesCorrectos() {
    Modulo m = modulo(1);
    CursoAcademico c = curso(1, "2025/2026");
    ResultadoAprendizaje ra1 = ra(1, m, c, "RA1");
    ResultadoAprendizaje ra2 = ra(2, m, c, "RA2");
    CriterioEvaluacion ce1 = ce(1, ra1, "a");
    CriterioEvaluacion ce2 = ce(2, ra1, "b");
    CriterioEvaluacion ce3 = ce(3, ra2, "a");

    when(moduloRepository.findById(1)).thenReturn(Optional.of(m));
    when(imparticionRepository.countByModuloId(1)).thenReturn(3);
    when(raRepository.findByModuloIdOrderByCursoAcademicoIdAscCodigoAsc(1))
        .thenReturn(List.of(ra1, ra2));
    when(ceRepository.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(
            any()))
        .thenReturn(List.of(ce1, ce2, ce3));

    AdminModuloResumenDTO result = service.getResumen(1);

    assertThat(result.numImparticiones()).isEqualTo(3);
    assertThat(result.numCursosConRas()).isEqualTo(1);
    assertThat(result.numRasTotal()).isEqualTo(2);
    assertThat(result.numCesTotal()).isEqualTo(3);
  }

  @Test
  void getResumen_mapea_camposDeCeCorrectamente() {
    Modulo m = modulo(1);
    CursoAcademico c = curso(1, "2025/2026");
    ResultadoAprendizaje ra = ra(1, m, c, "RA1");
    CriterioEvaluacion ceConDatos =
        CriterioEvaluacion.builder()
            .id(1)
            .resultadoAprendizaje(ra)
            .codigo("a")
            .descripcion("Identifica tipos")
            .peso(new BigDecimal("40.00"))
            .instrumento(InstrumentoEvaluacion.ACTIVIDAD_EVALUABLE)
            .unidadDidactica("UD1")
            .trimestre((short) 1)
            .build();

    when(moduloRepository.findById(1)).thenReturn(Optional.of(m));
    when(imparticionRepository.countByModuloId(1)).thenReturn(0);
    when(raRepository.findByModuloIdOrderByCursoAcademicoIdAscCodigoAsc(1)).thenReturn(List.of(ra));
    when(ceRepository.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(
            any()))
        .thenReturn(List.of(ceConDatos));

    AdminModuloResumenDTO result = service.getResumen(1);

    var ceDto = result.cursos().get(0).ras().get(0).ces().get(0);
    assertThat(ceDto.codigo()).isEqualTo("a");
    assertThat(ceDto.peso()).isEqualByComparingTo("40.00");
    assertThat(ceDto.instrumento()).isEqualTo("ACTIVIDAD_EVALUABLE");
    assertThat(ceDto.unidadDidactica()).isEqualTo("UD1");
    assertThat(ceDto.trimestre()).isEqualTo((short) 1);
  }
}
