package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.*;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.mapper.TeacherDashboardMapper;
import com.tfg.schooledule.infrastructure.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TeacherDashboardService {

  private final ImparticionRepository imparticionRepo;
  private final MatriculaRepository matriculaRepo;
  private final ItemEvaluableRepository itemEvaluableRepo;
  private final CalificacionRepository calificacionRepo;
  private final CriterioEvaluacionRepository ceRepo;
  private final TeacherDashboardMapper mapper;
  private final EntityManager entityManager;

  public TeacherDashboardService(
      ImparticionRepository imparticionRepo,
      MatriculaRepository matriculaRepo,
      ItemEvaluableRepository itemEvaluableRepo,
      CalificacionRepository calificacionRepo,
      CriterioEvaluacionRepository ceRepo,
      TeacherDashboardMapper mapper,
      EntityManager entityManager) {
    this.imparticionRepo = imparticionRepo;
    this.matriculaRepo = matriculaRepo;
    this.itemEvaluableRepo = itemEvaluableRepo;
    this.calificacionRepo = calificacionRepo;
    this.ceRepo = ceRepo;
    this.mapper = mapper;
    this.entityManager = entityManager;
  }

  public List<TeacherCenterDTO> getCentersForTeacher(Usuario profesor) {
    return profesor.getCentros().stream()
        .map(
            centro -> {
              long count =
                  imparticionRepo
                      .findByProfesorIdAndCentroId(profesor.getId(), centro.getId())
                      .size();
              return mapper.toCenterDto(centro, count);
            })
        .sorted(Comparator.comparing(TeacherCenterDTO::nombre))
        .collect(Collectors.toList());
  }

  public List<TeacherSubjectDTO> getSubjectsForTeacherAndCenter(
      Integer profesorId, Integer centroId) {
    return imparticionRepo.findByProfesorIdAndCentroId(profesorId, centroId).stream()
        .map(
            imp -> {
              long alumnosCount =
                  matriculaRepo
                      .findByImparticionIdAndEstado(imp.getId(), EstadoMatricula.ACTIVA)
                      .size();
              return mapper.toSubjectDto(imp, alumnosCount);
            })
        .collect(Collectors.toList());
  }

  public List<TeacherStudentRowDTO> getRosterForImparticion(
      Integer profesorId, Integer imparticionId) {
    if (!imparticionRepo.existsByIdAndProfesorId(imparticionId, profesorId)) {
      throw new AccessDeniedException(
          "El profesor no tiene acceso a la impartición " + imparticionId);
    }
    return matriculaRepo
        .findByImparticionIdAndEstado(imparticionId, EstadoMatricula.ACTIVA)
        .stream()
        .map(mapper::toStudentRow)
        .collect(Collectors.toList());
  }

  public TeacherStudentGradesDTO getStudentGrades(Integer profesorId, Integer matriculaId) {
    Matricula matricula = loadMatriculaWithOwnershipCheck(profesorId, matriculaId);
    return buildGradesDTO(matricula);
  }

  @Transactional
  public TeacherStudentGradesDTO upsertGrades(
      Integer profesorId, String profesorEmail, GradeUpsertRequest req) {
    Matricula matricula = loadMatriculaWithOwnershipCheck(profesorId, req.matriculaId());

    entityManager
        .createNativeQuery("SELECT set_config('app.usuario_actual', :u, true)")
        .setParameter("u", profesorEmail)
        .getSingleResult();

    // RA ids válidos para esta impartición
    List<ItemEvaluable> items =
        itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(
            matricula.getImparticion().getId());
    Set<Integer> validRaIds =
        items.stream().map(ie -> ie.getResultadoAprendizaje().getId()).collect(Collectors.toSet());

    // Mapa raId → item (para verificar cerrado)
    Map<Integer, ItemEvaluable> itemByRaId =
        items.stream()
            .collect(
                Collectors.toMap(
                    ie -> ie.getResultadoAprendizaje().getId(),
                    ie -> ie,
                    (a, b) -> a)); // en caso de duplicado (recuperación), usar el primero

    for (GradeUpsertRequest.Entry entry : req.entries()) {
      CriterioEvaluacion ce =
          ceRepo
              .findById(entry.criterioEvaluacionId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Criterio de evaluación no encontrado: " + entry.criterioEvaluacionId()));

      Integer raId = ce.getResultadoAprendizaje().getId();
      if (!validRaIds.contains(raId)) {
        throw new IllegalArgumentException(
            "El criterio " + entry.criterioEvaluacionId() + " no pertenece a esta impartición");
      }

      ItemEvaluable item = itemByRaId.get(raId);
      if (Boolean.TRUE.equals(item.getPeriodoEvaluacion().getCerrado())) {
        throw new IllegalStateException(
            "El periodo '"
                + item.getPeriodoEvaluacion().getNombre()
                + "' está cerrado y no admite cambios");
      }

      Optional<Calificacion> existing =
          calificacionRepo.findByMatriculaIdAndCriterioEvaluacionId(matricula.getId(), ce.getId());

      if (existing.isPresent()) {
        Calificacion cal = existing.get();
        cal.setValor(entry.valor());
        cal.setComentario(entry.comentario());
        calificacionRepo.save(cal);
      } else {
        calificacionRepo.save(
            Calificacion.builder()
                .matricula(matricula)
                .criterioEvaluacion(ce)
                .valor(entry.valor())
                .comentario(entry.comentario())
                .build());
      }
    }

    return buildGradesDTO(matricula);
  }

  public Integer getCentroIdByImparticion(Integer profesorId, Integer imparticionId) {
    return imparticionRepo
        .findById(imparticionId)
        .filter(i -> i.getProfesor().getId().equals(profesorId))
        .map(i -> i.getCentro().getId())
        .orElseThrow(
            () ->
                new AccessDeniedException(
                    "El profesor no tiene acceso a la impartición " + imparticionId));
  }

  // ─── helpers privados ────────────────────────────────────────────────────────

  private Matricula loadMatriculaWithOwnershipCheck(Integer profesorId, Integer matriculaId) {
    return matriculaRepo
        .findByIdAndImparticionProfesorId(matriculaId, profesorId)
        .orElseThrow(
            () ->
                new AccessDeniedException(
                    "El profesor no tiene acceso a la matrícula " + matriculaId));
  }

  /**
   * Construye el DTO completo de notas para una matrícula (periodos, RAs, CEs, medias). Público
   * para que servicios con distinto criterio de autorización (profesor vs alumno) puedan reusar la
   * construcción tras hacer su propia validación de ownership.
   */
  public TeacherStudentGradesDTO buildGradesDTO(Matricula matricula) {
    Integer imparticionId = matricula.getImparticion().getId();
    List<ItemEvaluable> items =
        itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(imparticionId);

    // Recoger todos los RA ids de los items
    List<Integer> raIds =
        items.stream()
            .map(ie -> ie.getResultadoAprendizaje().getId())
            .distinct()
            .collect(Collectors.toList());

    // CEs agrupados por RA id
    List<CriterioEvaluacion> allCes =
        ceRepo.findByResultadoAprendizajeIdInOrderByResultadoAprendizajeIdAscCodigoAsc(raIds);
    Map<Integer, List<CriterioEvaluacion>> cesByRaId =
        allCes.stream()
            .collect(
                Collectors.groupingBy(
                    ce -> ce.getResultadoAprendizaje().getId(),
                    LinkedHashMap::new,
                    Collectors.toList()));

    // Calificaciones del alumno para todos los CEs relevantes
    List<Integer> allCeIds =
        allCes.stream().map(CriterioEvaluacion::getId).collect(Collectors.toList());
    List<Calificacion> grades =
        calificacionRepo.findByMatriculaIdAndCriterioEvaluacionIdIn(matricula.getId(), allCeIds);
    Map<Integer, Calificacion> gradeByCeId =
        grades.stream().collect(Collectors.toMap(c -> c.getCriterioEvaluacion().getId(), c -> c));

    // Agrupar items por periodo manteniendo el orden
    Map<PeriodoEvaluacion, List<ItemEvaluable>> byPeriodo = new LinkedHashMap<>();
    for (ItemEvaluable item : items) {
      byPeriodo.computeIfAbsent(item.getPeriodoEvaluacion(), k -> new ArrayList<>()).add(item);
    }

    List<TeacherPeriodoGradesDTO> periodos = new ArrayList<>();
    for (Map.Entry<PeriodoEvaluacion, List<ItemEvaluable>> entry : byPeriodo.entrySet()) {
      PeriodoEvaluacion periodo = entry.getKey();
      List<TeacherGradeItemDTO> itemDtos = new ArrayList<>();

      for (ItemEvaluable item : entry.getValue()) {
        ResultadoAprendizaje ra = item.getResultadoAprendizaje();
        List<CriterioEvaluacion> ces = cesByRaId.getOrDefault(ra.getId(), Collections.emptyList());

        List<TeacherCriterioGradeDTO> criterioDtos =
            ces.stream()
                .map(ce -> mapper.toCriterioGrade(ce, gradeByCeId.get(ce.getId())))
                .collect(Collectors.toList());

        BigDecimal mediaRa = computeCeMedia(criterioDtos);

        itemDtos.add(
            new TeacherGradeItemDTO(
                item.getId(),
                ra.getId(),
                ra.getCodigo(),
                ra.getDescripcion(),
                item.getNombre(),
                item.getTipo().name(),
                item.getFecha(),
                criterioDtos,
                mediaRa));
      }

      BigDecimal mediaPeriodo = computePeriodoMedia(itemDtos);
      periodos.add(
          new TeacherPeriodoGradesDTO(
              periodo.getId(),
              periodo.getNombre(),
              periodo.getPeso(),
              Boolean.TRUE.equals(periodo.getCerrado()),
              itemDtos,
              mediaPeriodo));
    }

    BigDecimal mediaGlobal = computeMediaGlobal(periodos);
    String alumnoNombre =
        matricula.getAlumno().getNombre() + " " + matricula.getAlumno().getApellidos();
    String label =
        matricula.getImparticion().getGrupo().getNombre()
            + " · "
            + matricula.getImparticion().getModulo().getNombre();

    return new TeacherStudentGradesDTO(
        matricula.getId(), alumnoNombre, label, periodos, mediaGlobal);
  }

  private BigDecimal computeCeMedia(List<TeacherCriterioGradeDTO> criterios) {
    List<BigDecimal> notas =
        criterios.stream()
            .map(TeacherCriterioGradeDTO::valor)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (notas.isEmpty()) return null;
    BigDecimal suma = notas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return suma.divide(new BigDecimal(notas.size()), 2, RoundingMode.HALF_UP);
  }

  private BigDecimal computePeriodoMedia(List<TeacherGradeItemDTO> items) {
    List<BigDecimal> medias =
        items.stream()
            .map(TeacherGradeItemDTO::mediaRa)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (medias.isEmpty()) return null;
    BigDecimal suma = medias.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return suma.divide(new BigDecimal(medias.size()), 2, RoundingMode.HALF_UP);
  }

  private BigDecimal computeMediaGlobal(List<TeacherPeriodoGradesDTO> periodos) {
    List<TeacherPeriodoGradesDTO> conNota =
        periodos.stream().filter(p -> p.media() != null).collect(Collectors.toList());
    if (conNota.isEmpty()) return null;

    BigDecimal sumaPonderada = BigDecimal.ZERO;
    BigDecimal sumaPesos = BigDecimal.ZERO;

    for (TeacherPeriodoGradesDTO p : conNota) {
      BigDecimal peso = p.peso() != null ? p.peso() : BigDecimal.ONE;
      sumaPonderada = sumaPonderada.add(p.media().multiply(peso));
      sumaPesos = sumaPesos.add(peso);
    }

    if (sumaPesos.compareTo(BigDecimal.ZERO) == 0) return null;
    return sumaPonderada.divide(sumaPesos, 2, RoundingMode.HALF_UP);
  }
}
