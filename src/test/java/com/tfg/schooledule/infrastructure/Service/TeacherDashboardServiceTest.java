package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.*;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.domain.enums.TipoActividad;
import com.tfg.schooledule.infrastructure.mapper.TeacherDashboardMapper;
import com.tfg.schooledule.infrastructure.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TeacherDashboardServiceTest {

  @Mock private ImparticionRepository imparticionRepo;
  @Mock private MatriculaRepository matriculaRepo;
  @Mock private ItemEvaluableRepository itemEvaluableRepo;
  @Mock private CalificacionRepository calificacionRepo;
  @Mock private CriterioEvaluacionRepository ceRepo;
  @Mock private TeacherDashboardMapper mapper;
  @Mock private EntityManager entityManager;
  @Mock private jakarta.persistence.Query nativeQuery;

  @InjectMocks private TeacherDashboardService service;

  private Usuario profe;
  private Centro centro;
  private Imparticion imparticion;
  private Matricula matricula;
  private Modulo modulo;

  @BeforeEach
  void setUp() {
    profe = Usuario.builder().id(2).email("juan@tfg.com").nombre("Juan").apellidos("G").build();
    centro = Centro.builder().id(1).nombre("IES Central").ubicacion("Madrid").build();
    profe.setCentros(Set.of(centro));

    modulo = Modulo.builder().id(1).codigo("M1").nombre("Modulo1").build();
    CursoAcademico curso =
        CursoAcademico.builder()
            .id(1)
            .nombre("2025/2026")
            .fechaInicio(LocalDate.now())
            .fechaFin(LocalDate.now().plusYears(1))
            .build();
    Grupo grupo =
        Grupo.builder().id(1).nombre("DAW1-A").centro(centro).cursoAcademico(curso).build();

    imparticion =
        Imparticion.builder()
            .id(1)
            .modulo(modulo)
            .grupo(grupo)
            .profesor(profe)
            .centro(centro)
            .build();

    Usuario alumno =
        Usuario.builder().id(3).nombre("Ana").apellidos("Lopez").email("ana@t.com").build();
    matricula =
        Matricula.builder()
            .id(1)
            .alumno(alumno)
            .imparticion(imparticion)
            .centro(centro)
            .estado(EstadoMatricula.ACTIVA)
            .build();
  }

  @Test
  void getCentersForTeacher_devuelveSoloCentrosVinculados() {
    when(mapper.toCenterDto(eq(centro), eq(1L)))
        .thenReturn(new TeacherCenterDTO(1, "IES Central", "Madrid", 1L));
    when(imparticionRepo.findByProfesorIdAndCentroId(2, 1)).thenReturn(List.of(imparticion));

    List<TeacherCenterDTO> result = service.getCentersForTeacher(profe);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).nombre()).isEqualTo("IES Central");
    assertThat(result.get(0).imparticionesCount()).isEqualTo(1L);
  }

  @Test
  void getSubjectsForTeacherAndCenter_filtraPorProfesorYCentro() {
    when(imparticionRepo.findByProfesorIdAndCentroId(2, 1)).thenReturn(List.of(imparticion));
    when(matriculaRepo.findByImparticionIdAndEstado(1, EstadoMatricula.ACTIVA))
        .thenReturn(List.of(matricula));
    when(mapper.toSubjectDto(eq(imparticion), eq(1L)))
        .thenReturn(new TeacherSubjectDTO(1, "M1", "Modulo1", "DAW1-A", "2025/2026", 1L));

    List<TeacherSubjectDTO> result = service.getSubjectsForTeacherAndCenter(2, 1);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).moduloCodigo()).isEqualTo("M1");
  }

  @Test
  void getRosterForImparticion_lanzaAccessDenied_siProfesorNoEsPropietario() {
    when(imparticionRepo.existsByIdAndProfesorId(1, 2)).thenReturn(false);

    assertThatThrownBy(() -> service.getRosterForImparticion(2, 1))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void getStudentGrades_retornaItemsConCriteriosYCalculaMedias() {
    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));

    ResultadoAprendizaje ra =
        ResultadoAprendizaje.builder()
            .id(1)
            .modulo(modulo)
            .codigo("RA1")
            .descripcion("RA1 desc")
            .build();
    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .peso(new BigDecimal("100.00"))
            .cerrado(false)
            .build();
    ItemEvaluable item =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .resultadoAprendizaje(ra)
            .nombre("Examen RA1")
            .tipo(TipoActividad.EXAMEN)
            .fecha(LocalDate.now())
            .build();

    CriterioEvaluacion ceA =
        CriterioEvaluacion.builder()
            .id(1)
            .resultadoAprendizaje(ra)
            .codigo("a")
            .descripcion("CE-a")
            .build();
    CriterioEvaluacion ceB =
        CriterioEvaluacion.builder()
            .id(2)
            .resultadoAprendizaje(ra)
            .codigo("b")
            .descripcion("CE-b")
            .build();

    Calificacion calA =
        Calificacion.builder()
            .id(1)
            .matricula(matricula)
            .criterioEvaluacion(ceA)
            .valor(new BigDecimal("8.00"))
            .build();

    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(item));
    when(ceRepo.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(List.of(1)))
        .thenReturn(List.of(ceA, ceB));
    when(calificacionRepo.findByMatriculaIdAndCriterioEvaluacionIdIn(eq(1), any()))
        .thenReturn(List.of(calA));

    when(mapper.toCriterioGrade(eq(ceA), eq(calA)))
        .thenReturn(new TeacherCriterioGradeDTO(1, "a", "CE-a", new BigDecimal("8.00"), null, 1));
    when(mapper.toCriterioGrade(eq(ceB), isNull()))
        .thenReturn(new TeacherCriterioGradeDTO(2, "b", "CE-b", null, null, null));

    TeacherStudentGradesDTO result = service.getStudentGrades(2, 1);

    assertThat(result.periodos()).hasSize(1);
    TeacherGradeItemDTO itemDto = result.periodos().get(0).items().get(0);
    assertThat(itemDto.criterios()).hasSize(2);
    // mediaRa: solo ceA tiene nota → 8.00
    assertThat(itemDto.mediaRa()).isEqualByComparingTo("8.00");
    // mediaPeriodo = media de los mediaRa del periodo = 8.00
    assertThat(result.periodos().get(0).media()).isEqualByComparingTo("8.00");
    // mediaGlobal = único periodo con nota
    assertThat(result.mediaGlobal()).isEqualByComparingTo("8.00");
  }

  @Test
  void getStudentGrades_mediaRa_esMediaAritmeticaDeCEs() {
    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));

    ResultadoAprendizaje ra =
        ResultadoAprendizaje.builder().id(1).modulo(modulo).codigo("RA1").descripcion("d").build();
    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .peso(BigDecimal.ONE)
            .cerrado(false)
            .build();
    ItemEvaluable item =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .resultadoAprendizaje(ra)
            .nombre("E")
            .tipo(TipoActividad.EXAMEN)
            .build();

    CriterioEvaluacion ceA =
        CriterioEvaluacion.builder()
            .id(1)
            .resultadoAprendizaje(ra)
            .codigo("a")
            .descripcion("x")
            .build();
    CriterioEvaluacion ceB =
        CriterioEvaluacion.builder()
            .id(2)
            .resultadoAprendizaje(ra)
            .codigo("b")
            .descripcion("y")
            .build();
    CriterioEvaluacion ceC =
        CriterioEvaluacion.builder()
            .id(3)
            .resultadoAprendizaje(ra)
            .codigo("c")
            .descripcion("z")
            .build();

    Calificacion calA =
        Calificacion.builder()
            .id(1)
            .matricula(matricula)
            .criterioEvaluacion(ceA)
            .valor(new BigDecimal("6.00"))
            .build();
    Calificacion calB =
        Calificacion.builder()
            .id(2)
            .matricula(matricula)
            .criterioEvaluacion(ceB)
            .valor(new BigDecimal("8.00"))
            .build();
    Calificacion calC =
        Calificacion.builder()
            .id(3)
            .matricula(matricula)
            .criterioEvaluacion(ceC)
            .valor(new BigDecimal("7.00"))
            .build();

    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(item));
    when(ceRepo.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(List.of(1)))
        .thenReturn(List.of(ceA, ceB, ceC));
    when(calificacionRepo.findByMatriculaIdAndCriterioEvaluacionIdIn(eq(1), any()))
        .thenReturn(List.of(calA, calB, calC));

    when(mapper.toCriterioGrade(eq(ceA), eq(calA)))
        .thenReturn(new TeacherCriterioGradeDTO(1, "a", "x", new BigDecimal("6.00"), null, 1));
    when(mapper.toCriterioGrade(eq(ceB), eq(calB)))
        .thenReturn(new TeacherCriterioGradeDTO(2, "b", "y", new BigDecimal("8.00"), null, 2));
    when(mapper.toCriterioGrade(eq(ceC), eq(calC)))
        .thenReturn(new TeacherCriterioGradeDTO(3, "c", "z", new BigDecimal("7.00"), null, 3));

    TeacherStudentGradesDTO result = service.getStudentGrades(2, 1);

    // media = (6 + 8 + 7) / 3 = 7.00
    assertThat(result.periodos().get(0).items().get(0).mediaRa()).isEqualByComparingTo("7.00");
  }

  @Test
  void upsertGrades_creaCalificacionNueva_cuandoCeNoTieneNota() {
    ResultadoAprendizaje ra =
        ResultadoAprendizaje.builder().id(1).modulo(modulo).codigo("RA1").descripcion("d").build();
    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .peso(BigDecimal.ONE)
            .cerrado(false)
            .build();
    ItemEvaluable item =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .resultadoAprendizaje(ra)
            .nombre("E")
            .tipo(TipoActividad.EXAMEN)
            .build();
    CriterioEvaluacion ce =
        CriterioEvaluacion.builder()
            .id(1)
            .resultadoAprendizaje(ra)
            .codigo("a")
            .descripcion("x")
            .build();

    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("9.00"), "Muy bien")));

    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));
    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(item));
    when(ceRepo.findById(1)).thenReturn(Optional.of(ce));
    when(calificacionRepo.findByMatriculaIdAndCriterioEvaluacionId(1, 1))
        .thenReturn(Optional.empty());
    when(calificacionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // Para recompute post-save
    Calificacion savedCal =
        Calificacion.builder()
            .id(1)
            .matricula(matricula)
            .criterioEvaluacion(ce)
            .valor(new BigDecimal("9.00"))
            .build();
    when(calificacionRepo.findByMatriculaIdAndCriterioEvaluacionIdIn(eq(1), any()))
        .thenReturn(List.of(savedCal));
    when(ceRepo.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(any()))
        .thenReturn(List.of(ce));
    when(mapper.toCriterioGrade(any(), any()))
        .thenReturn(
            new TeacherCriterioGradeDTO(1, "a", "x", new BigDecimal("9.00"), "Muy bien", 1));

    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
    when(nativeQuery.getSingleResult()).thenReturn("juan@tfg.com");

    TeacherStudentGradesDTO result = service.upsertGrades(2, "juan@tfg.com", req);

    assertThat(result).isNotNull();
    verify(entityManager).createNativeQuery("SELECT set_config('app.usuario_actual', :u, true)");
    verify(nativeQuery).setParameter("u", "juan@tfg.com");
    verify(calificacionRepo).save(any(Calificacion.class));
  }

  @Test
  void upsertGrades_rechazaCuandoPeriodoCerrado() {
    ResultadoAprendizaje ra =
        ResultadoAprendizaje.builder().id(1).modulo(modulo).codigo("RA1").descripcion("d").build();
    PeriodoEvaluacion periodoCerrado =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .cerrado(true)
            .build();
    ItemEvaluable item =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodoCerrado)
            .resultadoAprendizaje(ra)
            .nombre("E")
            .tipo(TipoActividad.EXAMEN)
            .build();
    CriterioEvaluacion ce =
        CriterioEvaluacion.builder()
            .id(1)
            .resultadoAprendizaje(ra)
            .codigo("a")
            .descripcion("x")
            .build();

    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("7.00"), null)));

    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));
    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(item));
    when(ceRepo.findById(1)).thenReturn(Optional.of(ce));
    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
    when(nativeQuery.getSingleResult()).thenReturn("juan@tfg.com");

    assertThatThrownBy(() -> service.upsertGrades(2, "juan@tfg.com", req))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cerrado");
  }

  @Test
  void upsertGrades_rechazaCuandoCeNoEsDeEsaImparticion() {
    ResultadoAprendizaje raAjeno =
        ResultadoAprendizaje.builder()
            .id(99)
            .modulo(modulo)
            .codigo("RA99")
            .descripcion("d")
            .build();
    CriterioEvaluacion ceAjeno =
        CriterioEvaluacion.builder()
            .id(99)
            .resultadoAprendizaje(raAjeno)
            .codigo("a")
            .descripcion("x")
            .build();

    ResultadoAprendizaje raLocal =
        ResultadoAprendizaje.builder().id(1).modulo(modulo).codigo("RA1").descripcion("d").build();
    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .cerrado(false)
            .build();
    ItemEvaluable item =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .resultadoAprendizaje(raLocal)
            .nombre("E")
            .tipo(TipoActividad.EXAMEN)
            .build();

    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(99, new BigDecimal("5.00"), null)));

    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));
    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(item));
    when(ceRepo.findById(99)).thenReturn(Optional.of(ceAjeno));
    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
    when(nativeQuery.getSingleResult()).thenReturn("juan@tfg.com");

    assertThatThrownBy(() -> service.upsertGrades(2, "juan@tfg.com", req))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
