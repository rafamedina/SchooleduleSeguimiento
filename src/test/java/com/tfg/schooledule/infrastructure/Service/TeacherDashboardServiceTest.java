package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
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
  @Mock private PeriodoEvaluacionRepository periodoRepo;
  @Mock private ResultadoAprendizajeRepository raRepo;
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
    when(mapper.toCenterDto(centro, 1L))
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
    when(mapper.toSubjectDto(imparticion, 1L))
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

    when(mapper.toCriterioGrade(ceA, calA))
        .thenReturn(
            new TeacherCriterioGradeDTO(
                1, "a", "CE-a", new BigDecimal("8.00"), null, 1, BigDecimal.ZERO));
    when(mapper.toCriterioGrade(eq(ceB), isNull()))
        .thenReturn(new TeacherCriterioGradeDTO(2, "b", "CE-b", null, null, null, BigDecimal.ZERO));

    TeacherStudentGradesDTO result = service.getStudentGrades(2, 1);

    assertThat(result.periodos()).hasSize(1);
    TeacherGradeItemDTO itemDto = result.periodos().get(0).items().get(0);
    assertThat(itemDto.criterios()).hasSize(2);
    // mediaRa: solo ceA tiene nota, peso=0 → fallback simple → 8.00
    assertThat(itemDto.mediaRa()).isEqualByComparingTo("8.00");
    // mediaPeriodo = único item con nota → 8.00
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

    when(mapper.toCriterioGrade(ceA, calA))
        .thenReturn(
            new TeacherCriterioGradeDTO(
                1, "a", "x", new BigDecimal("6.00"), null, 1, BigDecimal.ZERO));
    when(mapper.toCriterioGrade(ceB, calB))
        .thenReturn(
            new TeacherCriterioGradeDTO(
                2, "b", "y", new BigDecimal("8.00"), null, 2, BigDecimal.ZERO));
    when(mapper.toCriterioGrade(ceC, calC))
        .thenReturn(
            new TeacherCriterioGradeDTO(
                3, "c", "z", new BigDecimal("7.00"), null, 3, BigDecimal.ZERO));

    TeacherStudentGradesDTO result = service.getStudentGrades(2, 1);

    // todos los pesos son 0 → fallback simple: (6 + 8 + 7) / 3 = 7.00
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
            1, List.of(new GradeUpsertRequest.Entry(1, 1, new BigDecimal("9.00"), "Muy bien")));

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
            new TeacherCriterioGradeDTO(
                1, "a", "x", new BigDecimal("9.00"), "Muy bien", 1, BigDecimal.ZERO));

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
            1, List.of(new GradeUpsertRequest.Entry(1, 1, new BigDecimal("7.00"), null)));

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

    // item 1 exists, but CE 99 does not belong to item 1's RA
    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(1, 99, new BigDecimal("5.00"), null)));

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

  @Test
  void validateCentroOwnership_noLanzaExcepcionCuandoProfesorTieneAcceso() {
    when(imparticionRepo.existsByProfesorIdAndCentroId(2, 1)).thenReturn(true);
    org.junit.jupiter.api.Assertions.assertDoesNotThrow(
        () -> service.validateCentroOwnership(2, 1));
  }

  @Test
  void validateCentroOwnership_lanzaAccessDeniedCuandoCentroNoPertenecealProfesor() {
    when(imparticionRepo.existsByProfesorIdAndCentroId(2, 99)).thenReturn(false);
    assertThatThrownBy(() -> service.validateCentroOwnership(2, 99))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("99");
  }

  @Test
  void buildGradesDTO_itemRecuperacion_apareceSeparadoDelItemRegular() {
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
    ItemEvaluable itemExamen =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .resultadoAprendizaje(ra)
            .nombre("Examen")
            .tipo(TipoActividad.EXAMEN)
            .build();
    ItemEvaluable itemRecuperacion =
        ItemEvaluable.builder()
            .id(2)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .resultadoAprendizaje(ra)
            .nombre("Recuperación")
            .tipo(TipoActividad.RECUPERACION)
            .build();
    CriterioEvaluacion ce =
        CriterioEvaluacion.builder()
            .id(1)
            .resultadoAprendizaje(ra)
            .codigo("a")
            .descripcion("x")
            .build();

    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(itemExamen, itemRecuperacion));
    when(ceRepo.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(any()))
        .thenReturn(List.of(ce));
    when(calificacionRepo.findByMatriculaIdAndCriterioEvaluacionIdIn(eq(1), any()))
        .thenReturn(List.of());
    when(mapper.toCriterioGrade(eq(ce), isNull()))
        .thenReturn(new TeacherCriterioGradeDTO(1, "a", "x", null, null, null, BigDecimal.ZERO));

    TeacherStudentGradesDTO result = service.buildGradesDTO(matricula);

    assertThat(result.periodos()).hasSize(1);
    assertThat(result.periodos().get(0).items()).hasSize(2);
    assertThat(result.periodos().get(0).items())
        .extracting(TeacherGradeItemDTO::tipoActividad)
        .containsExactlyInAnyOrder("EXAMEN", "RECUPERACION");
  }

  @Test
  void crearItem_profesorPropietario_persiste() {
    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .cerrado(false)
            .build();
    ResultadoAprendizaje ra =
        ResultadoAprendizaje.builder().id(1).modulo(modulo).codigo("RA1").descripcion("d").build();

    ItemEvaluableFormDTO dto = new ItemEvaluableFormDTO();
    dto.setNombre("Recuperación RA1");
    dto.setTipo(TipoActividad.RECUPERACION);
    dto.setFecha(LocalDate.now());
    dto.setPeriodoEvaluacionId(1);
    dto.setResultadoAprendizajeId(1);

    when(imparticionRepo.findById(1)).thenReturn(Optional.of(imparticion));
    when(periodoRepo.findById(1)).thenReturn(Optional.of(periodo));
    when(raRepo.findById(1)).thenReturn(Optional.of(ra));
    when(itemEvaluableRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.crearItem(1, 2, dto);

    verify(itemEvaluableRepo).save(argThat(i -> i.getTipo() == TipoActividad.RECUPERACION));
  }

  @Test
  void crearItem_profesorNoPropietario_lanzaAccessDeniedException() {
    when(imparticionRepo.findById(1)).thenReturn(Optional.of(imparticion));

    ItemEvaluableFormDTO dto = new ItemEvaluableFormDTO();
    dto.setNombre("Test");
    dto.setTipo(TipoActividad.EXAMEN);
    dto.setFecha(LocalDate.now());
    dto.setPeriodoEvaluacionId(1);
    dto.setResultadoAprendizajeId(1);

    assertThatThrownBy(() -> service.crearItem(1, 99, dto))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void crearItem_periodoDeOtraImparticion_lanzaIllegalArgumentException() {
    Imparticion otraImparticion =
        Imparticion.builder()
            .id(2)
            .modulo(modulo)
            .grupo(imparticion.getGrupo())
            .profesor(profe)
            .centro(centro)
            .build();
    PeriodoEvaluacion periodoAjeno =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(otraImparticion)
            .nombre("P1")
            .cerrado(false)
            .build();

    when(imparticionRepo.findById(1)).thenReturn(Optional.of(imparticion));
    when(periodoRepo.findById(1)).thenReturn(Optional.of(periodoAjeno));

    ItemEvaluableFormDTO dto = new ItemEvaluableFormDTO();
    dto.setNombre("Test");
    dto.setTipo(TipoActividad.EXAMEN);
    dto.setFecha(LocalDate.now());
    dto.setPeriodoEvaluacionId(1);
    dto.setResultadoAprendizajeId(1);

    assertThatThrownBy(() -> service.crearItem(1, 2, dto))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void eliminarItem_profesorPropietario_elimina() {
    ResultadoAprendizaje ra =
        ResultadoAprendizaje.builder().id(1).modulo(modulo).codigo("RA1").descripcion("d").build();
    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder().id(1).imparticion(imparticion).nombre("P1").build();

    when(itemEvaluableRepo.existsByIdAndImparticionProfesorId(1, 2)).thenReturn(true);
    when(itemEvaluableRepo.findById(1))
        .thenReturn(
            Optional.of(
                ItemEvaluable.builder()
                    .id(1)
                    .imparticion(imparticion)
                    .periodoEvaluacion(periodo)
                    .resultadoAprendizaje(ra)
                    .nombre("E")
                    .tipo(TipoActividad.EXAMEN)
                    .build()));

    service.eliminarItem(1, 2);

    verify(itemEvaluableRepo).deleteById(1);
  }

  @Test
  void eliminarItem_profesorNoPropietario_lanzaAccessDeniedException() {
    when(itemEvaluableRepo.existsByIdAndImparticionProfesorId(1, 99)).thenReturn(false);
    assertThatThrownBy(() -> service.eliminarItem(1, 99)).isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void upsertGrades_itemRecuperacion_periodoCerrado_permiteGrabar() {
    ResultadoAprendizaje ra =
        ResultadoAprendizaje.builder().id(1).modulo(modulo).codigo("RA1").descripcion("d").build();
    PeriodoEvaluacion periodoCerrado =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .cerrado(true)
            .build();
    ItemEvaluable itemRecuperacion =
        ItemEvaluable.builder()
            .id(2)
            .imparticion(imparticion)
            .periodoEvaluacion(periodoCerrado)
            .resultadoAprendizaje(ra)
            .nombre("R")
            .tipo(TipoActividad.RECUPERACION)
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
            1, List.of(new GradeUpsertRequest.Entry(2, 1, new BigDecimal("7.00"), null)));

    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));
    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(itemRecuperacion));
    when(ceRepo.findById(1)).thenReturn(Optional.of(ce));
    when(calificacionRepo.findByMatriculaIdAndCriterioEvaluacionId(1, 1))
        .thenReturn(Optional.empty());
    when(calificacionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(calificacionRepo.findByMatriculaIdAndCriterioEvaluacionIdIn(eq(1), any()))
        .thenReturn(List.of());
    when(ceRepo.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(any()))
        .thenReturn(List.of(ce));
    when(mapper.toCriterioGrade(any(), any()))
        .thenReturn(
            new TeacherCriterioGradeDTO(
                1, "a", "x", new BigDecimal("7.00"), null, 1, BigDecimal.ZERO));
    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
    when(nativeQuery.getSingleResult()).thenReturn("juan@tfg.com");

    // No debe lanzar IllegalStateException aunque el periodo esté cerrado
    org.junit.jupiter.api.Assertions.assertDoesNotThrow(
        () -> service.upsertGrades(2, "juan@tfg.com", req));
    verify(calificacionRepo).save(any(Calificacion.class));
  }

  @Test
  void upsertGrades_itemNormal_periodoCerrado_lanzaIllegalStateException() {
    ResultadoAprendizaje ra =
        ResultadoAprendizaje.builder().id(1).modulo(modulo).codigo("RA1").descripcion("d").build();
    PeriodoEvaluacion periodoCerrado =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .cerrado(true)
            .build();
    ItemEvaluable itemNormal =
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
            1, List.of(new GradeUpsertRequest.Entry(1, 1, new BigDecimal("7.00"), null)));

    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));
    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(itemNormal));
    when(ceRepo.findById(1)).thenReturn(Optional.of(ce));
    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
    when(nativeQuery.getSingleResult()).thenReturn("juan@tfg.com");

    assertThatThrownBy(() -> service.upsertGrades(2, "juan@tfg.com", req))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cerrado");
  }

  @Test
  void upsertGrades_itemIdNoPerteneceLaImparticion_lanzaAccessDeniedException() {
    ResultadoAprendizaje raLocal =
        ResultadoAprendizaje.builder().id(1).modulo(modulo).codigo("RA1").descripcion("d").build();
    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .cerrado(false)
            .build();
    ItemEvaluable itemLocal =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .resultadoAprendizaje(raLocal)
            .nombre("E")
            .tipo(TipoActividad.EXAMEN)
            .build();

    // entry references itemEvaluableId=99, but only item 1 exists
    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(99, 1, new BigDecimal("5.00"), null)));

    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));
    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(itemLocal));
    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
    when(nativeQuery.getSingleResult()).thenReturn("juan@tfg.com");

    assertThatThrownBy(() -> service.upsertGrades(2, "juan@tfg.com", req))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void getStudentGrades_mediaRa_usaMediaPonderadaPorPesoCe() {
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

    Calificacion calA =
        Calificacion.builder()
            .id(1)
            .matricula(matricula)
            .criterioEvaluacion(ceA)
            .valor(new BigDecimal("4.00"))
            .build();
    Calificacion calB =
        Calificacion.builder()
            .id(2)
            .matricula(matricula)
            .criterioEvaluacion(ceB)
            .valor(new BigDecimal("10.00"))
            .build();

    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(item));
    when(ceRepo.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(List.of(1)))
        .thenReturn(List.of(ceA, ceB));
    when(calificacionRepo.findByMatriculaIdAndCriterioEvaluacionIdIn(eq(1), any()))
        .thenReturn(List.of(calA, calB));

    // ceA peso=25, ceB peso=75 → media = (4*25 + 10*75) / (25+75) = 850/100 = 8.50
    when(mapper.toCriterioGrade(ceA, calA))
        .thenReturn(
            new TeacherCriterioGradeDTO(
                1, "a", "x", new BigDecimal("4.00"), null, 1, new BigDecimal("25.00")));
    when(mapper.toCriterioGrade(ceB, calB))
        .thenReturn(
            new TeacherCriterioGradeDTO(
                2, "b", "y", new BigDecimal("10.00"), null, 2, new BigDecimal("75.00")));

    TeacherStudentGradesDTO result = service.getStudentGrades(2, 1);

    assertThat(result.periodos().get(0).items().get(0).mediaRa()).isEqualByComparingTo("8.50");
  }

  @Test
  void getStudentGrades_mediaPeriodo_usaMediaPonderadaPorPesoRa() {
    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));

    ResultadoAprendizaje ra1 =
        ResultadoAprendizaje.builder()
            .id(1)
            .modulo(modulo)
            .codigo("RA1")
            .descripcion("d1")
            .pesoSugerido(new BigDecimal("30.00"))
            .build();
    ResultadoAprendizaje ra2 =
        ResultadoAprendizaje.builder()
            .id(2)
            .modulo(modulo)
            .codigo("RA2")
            .descripcion("d2")
            .pesoSugerido(new BigDecimal("70.00"))
            .build();
    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .peso(BigDecimal.ONE)
            .cerrado(false)
            .build();
    ItemEvaluable item1 =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .resultadoAprendizaje(ra1)
            .nombre("E1")
            .tipo(TipoActividad.EXAMEN)
            .build();
    ItemEvaluable item2 =
        ItemEvaluable.builder()
            .id(2)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .resultadoAprendizaje(ra2)
            .nombre("E2")
            .tipo(TipoActividad.EXAMEN)
            .build();

    CriterioEvaluacion ce1 =
        CriterioEvaluacion.builder()
            .id(1)
            .resultadoAprendizaje(ra1)
            .codigo("a")
            .descripcion("x")
            .build();
    CriterioEvaluacion ce2 =
        CriterioEvaluacion.builder()
            .id(2)
            .resultadoAprendizaje(ra2)
            .codigo("b")
            .descripcion("y")
            .build();

    Calificacion cal1 =
        Calificacion.builder()
            .id(1)
            .matricula(matricula)
            .criterioEvaluacion(ce1)
            .valor(new BigDecimal("4.00"))
            .build();
    Calificacion cal2 =
        Calificacion.builder()
            .id(2)
            .matricula(matricula)
            .criterioEvaluacion(ce2)
            .valor(new BigDecimal("8.00"))
            .build();

    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(item1, item2));
    when(ceRepo.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(any()))
        .thenReturn(List.of(ce1, ce2));
    when(calificacionRepo.findByMatriculaIdAndCriterioEvaluacionIdIn(eq(1), any()))
        .thenReturn(List.of(cal1, cal2));

    // item1: ce1 peso=0 → mediaRa1 = 4.00 (fallback), raPeso=30
    // item2: ce2 peso=0 → mediaRa2 = 8.00 (fallback), raPeso=70
    // mediaPeriodo = (4*30 + 8*70) / (30+70) = (120+560)/100 = 6.80
    when(mapper.toCriterioGrade(ce1, cal1))
        .thenReturn(
            new TeacherCriterioGradeDTO(
                1, "a", "x", new BigDecimal("4.00"), null, 1, BigDecimal.ZERO));
    when(mapper.toCriterioGrade(ce2, cal2))
        .thenReturn(
            new TeacherCriterioGradeDTO(
                2, "b", "y", new BigDecimal("8.00"), null, 2, BigDecimal.ZERO));

    TeacherStudentGradesDTO result = service.getStudentGrades(2, 1);

    assertThat(result.periodos().get(0).media()).isEqualByComparingTo("6.80");
  }
}
