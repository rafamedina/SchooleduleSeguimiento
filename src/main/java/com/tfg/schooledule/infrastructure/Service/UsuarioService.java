package com.tfg.schooledule.infrastructure.Service;

import com.tfg.schooledule.domain.DTO.AlumnoProfileDTO;
import com.tfg.schooledule.domain.DTO.GradeDTO;
import com.tfg.schooledule.domain.DTO.GradeDashboardDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.CalificacionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.PeriodoEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

  @Autowired private UsuarioRepository usuarioRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private MatriculaRepository matriculaRepository;

  @Autowired private CalificacionRepository calificacionRepository;

  @Autowired private PeriodoEvaluacionRepository periodoRepository;

  public AlumnoProfileDTO getAlumnoProfile(Integer usuarioId) {
    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    Matricula matricula =
        matriculaRepository
            .findFirstByAlumnoIdOrderByImparticionGrupoCursoAcademicoIdDesc(usuarioId)
            .orElseThrow(() -> new RuntimeException("Matricula no encontrada"));

    Grupo grupo = matricula.getImparticion().getGrupo();

    return AlumnoProfileDTO.builder()
        .id(usuario.getId())
        .username(usuario.getUsername())
        .nombre(usuario.getNombre())
        .apellidos(usuario.getApellidos())
        .email(usuario.getEmail())
        .centroNombre(matricula.getCentro().getNombre())
        .grupoNombre(grupo.getNombre())
        .cursoAcademico(grupo.getCursoAcademico().getNombre())
        .build();
  }

  public GradeDashboardDTO getStudentGrades(Integer usuarioId, Integer periodoId) {
    List<Calificacion> calificaciones =
        calificacionRepository.findByAlumnoIdAndPeriodoId(usuarioId, periodoId);

    if (calificaciones.isEmpty()) {
      return GradeDashboardDTO.builder().gradesByModulo(new HashMap<>()).build();
    }

    String periodoNombre =
        calificaciones.get(0).getItemEvaluable().getPeriodoEvaluacion().getNombre();

    Map<String, List<GradeDTO>> gradesByModulo = new HashMap<>();

    for (Calificacion calif : calificaciones) {
      String moduloNombre = calif.getMatricula().getImparticion().getModulo().getNombre();

      GradeDTO gradeDTO =
          GradeDTO.builder()
              .itemNombre(calif.getItemEvaluable().getNombre())
              .valor(calif.getValor())
              .comentario(calif.getComentario())
              .fecha(calif.getItemEvaluable().getFecha())
              .tipoActividad(calif.getItemEvaluable().getTipo().name())
              .build();

      gradesByModulo.computeIfAbsent(moduloNombre, k -> new ArrayList<>()).add(gradeDTO);
    }

    return GradeDashboardDTO.builder()
        .periodoNombre(periodoNombre)
        .gradesByModulo(gradesByModulo)
        .build();
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
