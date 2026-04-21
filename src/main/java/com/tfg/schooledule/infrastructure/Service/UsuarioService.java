package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AlumnoProfileDTO;
import com.tfg.schooledule.domain.dto.GradeDashboardDTO;
import com.tfg.schooledule.domain.dto.TeacherStudentGradesDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.mapper.AlumnoProfileMapper;
import com.tfg.schooledule.infrastructure.mapper.GradeDashboardMapper;
import com.tfg.schooledule.infrastructure.repository.CalificacionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.PeriodoEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

  @Autowired private UsuarioRepository usuarioRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private MatriculaRepository matriculaRepository;
  @Autowired private CalificacionRepository calificacionRepository;
  @Autowired private PeriodoEvaluacionRepository periodoRepository;
  @Autowired private AlumnoProfileMapper alumnoProfileMapper;
  @Autowired private GradeDashboardMapper gradeDashboardMapper;
  @Autowired private TeacherDashboardService teacherDashboardService;

  public AlumnoProfileDTO getAlumnoProfile(Integer usuarioId) {
    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    Matricula matricula =
        matriculaRepository
            .findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(usuarioId)
            .orElseThrow(() -> new RuntimeException("Matricula no encontrada"));

    return alumnoProfileMapper.toDto(usuario, matricula);
  }

  public List<Matricula> getAsignaturasAlumno(Integer usuarioId) {
    return matriculaRepository.findActivasByAlumnoId(usuarioId);
  }

  /**
   * Detalle completo de notas (Periodos → RAs → CEs) para una matrícula del propio alumno.
   * AccessDeniedException si la matrícula no pertenece al alumno.
   */
  public TeacherStudentGradesDTO getAlumnoMatriculaGrades(Integer alumnoId, Integer matriculaId) {
    Matricula matricula =
        matriculaRepository
            .findById(matriculaId)
            .orElseThrow(
                () -> new AccessDeniedException("Matrícula no encontrada: " + matriculaId));
    if (!matricula.getAlumno().getId().equals(alumnoId)) {
      throw new AccessDeniedException("La matrícula no pertenece al alumno");
    }
    return teacherDashboardService.buildGradesDTO(matricula);
  }

  public GradeDashboardDTO getStudentGrades(Integer usuarioId, Integer periodoId) {
    List<Calificacion> calificaciones =
        calificacionRepository.findByAlumnoIdAndPeriodoId(usuarioId, periodoId);

    String periodoNombre =
        periodoRepository.findById(periodoId).map(p -> p.getNombre()).orElse(null);

    return gradeDashboardMapper.toDto(calificaciones, periodoNombre);
  }

  public boolean comprobarPassword(String email, String password) {
    Optional<Usuario> usuario = usuarioRepository.findUsuarioByEmail(email);
    if (usuario.isEmpty()) {
      return false;
    }
    return passwordEncoder.matches(password, usuario.get().getPasswordHash());
  }

  public Optional<Usuario> buscarPorCorreo(String email) {
    return usuarioRepository.findUsuarioByEmail(email);
  }

  public Optional<Usuario> buscarPorNombreUsuario(String username) {
    return usuarioRepository.findByUsername(username);
  }

  public List<PeriodoEvaluacion> getStudentPeriods(Integer usuarioId) {
    List<Matricula> matriculas = matriculaRepository.findByAlumnoId(usuarioId);
    Set<PeriodoEvaluacion> periodos = new HashSet<>();
    for (Matricula m : matriculas) {
      periodos.addAll(periodoRepository.findByImparticionId(m.getImparticion().getId()));
    }
    return new ArrayList<>(periodos);
  }
}
