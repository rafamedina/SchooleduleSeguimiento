package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.TeacherStudentGradesDTO;
import com.tfg.schooledule.domain.dto.TeacherStudentRowDTO;
import com.tfg.schooledule.domain.dto.TutorGrupoListDTO;
import com.tfg.schooledule.domain.dto.TutorImparticionDTO;
import com.tfg.schooledule.domain.entity.Grupo;
import com.tfg.schooledule.domain.entity.Imparticion;
import com.tfg.schooledule.domain.entity.Matricula;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TutorService {

  private final GrupoRepository grupoRepository;
  private final ImparticionRepository imparticionRepository;
  private final MatriculaRepository matriculaRepository;
  private final TeacherDashboardService teacherDashboardService;

  public TutorService(
      GrupoRepository grupoRepository,
      ImparticionRepository imparticionRepository,
      MatriculaRepository matriculaRepository,
      TeacherDashboardService teacherDashboardService) {
    this.grupoRepository = grupoRepository;
    this.imparticionRepository = imparticionRepository;
    this.matriculaRepository = matriculaRepository;
    this.teacherDashboardService = teacherDashboardService;
  }

  public List<TutorGrupoListDTO> getGruposDeTutor(Integer tutorId) {
    return grupoRepository.findByTutorId(tutorId).stream()
        .map(
            grupo -> {
              List<Imparticion> imparticiones = imparticionRepository.findByGrupoId(grupo.getId());
              long numAlumnos =
                  imparticiones.stream()
                      .flatMap(
                          i ->
                              matriculaRepository
                                  .findByImparticionIdAndEstado(i.getId(), EstadoMatricula.ACTIVA)
                                  .stream())
                      .distinct()
                      .count();
              return new TutorGrupoListDTO(
                  grupo.getId(),
                  grupo.getNombre(),
                  grupo.getCentro().getNombre(),
                  grupo.getCursoAcademico().getNombre(),
                  imparticiones.size(),
                  numAlumnos);
            })
        .toList();
  }

  public void validateTutorOwnership(Integer tutorId, Integer grupoId) {
    if (!grupoRepository.existsByIdAndTutorId(grupoId, tutorId)) {
      throw new AccessDeniedException("No eres tutor del grupo " + grupoId);
    }
  }

  public List<TutorImparticionDTO> getImparticionesByGrupo(Integer tutorId, Integer grupoId) {
    validateTutorOwnership(tutorId, grupoId);
    return imparticionRepository.findByGrupoId(grupoId).stream()
        .map(
            imp -> {
              long numAlumnos =
                  matriculaRepository
                      .findByImparticionIdAndEstado(imp.getId(), EstadoMatricula.ACTIVA)
                      .size();
              boolean puedeEditar = imp.getProfesor().getId().equals(tutorId);
              String profesorNombre =
                  imp.getProfesor().getNombre() + " " + imp.getProfesor().getApellidos();
              return new TutorImparticionDTO(
                  imp.getId(),
                  imp.getModulo().getNombre(),
                  profesorNombre,
                  numAlumnos,
                  puedeEditar);
            })
        .toList();
  }

  public TeacherStudentGradesDTO getStudentGradesAsTutor(Integer tutorId, Integer matriculaId) {
    Matricula matricula =
        matriculaRepository
            .findById(matriculaId)
            .orElseThrow(
                () -> new EntityNotFoundException("Matrícula no encontrada: " + matriculaId));
    Integer grupoId = matricula.getImparticion().getGrupo().getId();
    validateTutorOwnership(tutorId, grupoId);
    return teacherDashboardService.buildGradesDTO(matricula);
  }

  public List<TeacherStudentRowDTO> buildRosterAsTutor(
      Integer tutorId, Integer grupoId, Integer imparticionId) {
    validateTutorOwnership(tutorId, grupoId);
    return teacherDashboardService.buildRoster(imparticionId);
  }

  public Grupo getGrupoOrFail(Integer grupoId) {
    return grupoRepository
        .findById(grupoId)
        .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + grupoId));
  }
}
