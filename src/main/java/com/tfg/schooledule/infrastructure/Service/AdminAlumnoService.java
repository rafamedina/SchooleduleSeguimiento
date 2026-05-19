package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminAlumnoListDTO;
import com.tfg.schooledule.domain.dto.AdminMatriculaFormDTO;
import com.tfg.schooledule.domain.dto.AdminMatriculaListDTO;
import com.tfg.schooledule.domain.dto.AlumnoFiltroDTO;
import com.tfg.schooledule.domain.entity.Imparticion;
import com.tfg.schooledule.domain.entity.Matricula;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.repository.CalificacionRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAlumnoService {

  private static final String ERR_MATRICULA = "Matrícula no encontrada: ";

  private final UsuarioRepository usuarioRepository;
  private final MatriculaRepository matriculaRepository;
  private final ImparticionRepository imparticionRepository;
  private final CalificacionRepository calificacionRepository;
  private final AdminCursoActivoService cursoActivoService;

  public AdminAlumnoService(
      UsuarioRepository usuarioRepository,
      MatriculaRepository matriculaRepository,
      ImparticionRepository imparticionRepository,
      CalificacionRepository calificacionRepository,
      AdminCursoActivoService cursoActivoService) {
    this.usuarioRepository = usuarioRepository;
    this.matriculaRepository = matriculaRepository;
    this.imparticionRepository = imparticionRepository;
    this.calificacionRepository = calificacionRepository;
    this.cursoActivoService = cursoActivoService;
  }

  private AdminAlumnoListDTO toListDTO(Usuario u) {
    return new AdminAlumnoListDTO(
        u.getId(),
        u.getNombre(),
        u.getApellidos(),
        u.getEmail(),
        matriculaRepository.findByAlumnoId(u.getId()).size());
  }

  @Transactional(readOnly = true)
  public List<AdminAlumnoListDTO> listarFiltrado(AlumnoFiltroDTO filtro) {
    Integer cursoId =
        filtro.cursoAcademicoId() != null
            ? filtro.cursoAcademicoId()
            : cursoActivoService.getCursoActivoId();
    return usuarioRepository
        .findAlumnosByFiltro(filtro.centroId(), filtro.grupoId(), cursoId)
        .stream()
        .map(this::toListDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AdminAlumnoListDTO> listarAlumnos() {
    return usuarioRepository.findAllAlumnosOrdenados().stream()
        .map(
            u ->
                new AdminAlumnoListDTO(
                    u.getId(),
                    u.getNombre(),
                    u.getApellidos(),
                    u.getEmail(),
                    matriculaRepository.findByAlumnoId(u.getId()).size()))
        .toList();
  }

  @Transactional(readOnly = true)
  public Usuario obtenerAlumno(Integer alumnoId) {
    return findAlumno(alumnoId);
  }

  private Usuario findAlumno(Integer alumnoId) {
    return usuarioRepository
        .findById(alumnoId)
        .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_ALUMNO".equals(r.getNombre())))
        .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + alumnoId));
  }

  @Transactional(readOnly = true)
  public List<AdminMatriculaListDTO> listarMatriculas(Integer alumnoId) {
    return matriculaRepository.findByAlumnoId(alumnoId).stream()
        .map(
            m ->
                new AdminMatriculaListDTO(
                    m.getId(),
                    m.getImparticion().getModulo().getNombre(),
                    m.getImparticion().getGrupo().getNombre(),
                    m.getImparticion().getCentro().getNombre(),
                    m.getEstado(),
                    m.getEsRepetidor()))
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminMatriculaFormDTO obtenerMatriculaParaEditar(Integer id) {
    Matricula m =
        matriculaRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ERR_MATRICULA + id));
    AdminMatriculaFormDTO dto = new AdminMatriculaFormDTO();
    dto.setId(m.getId());
    dto.setImparticionId(m.getImparticion().getId());
    dto.setEstado(m.getEstado());
    dto.setEsRepetidor(m.getEsRepetidor());
    return dto;
  }

  @Transactional
  public void crearMatricula(Integer alumnoId, AdminMatriculaFormDTO dto) {
    Usuario alumno = findAlumno(alumnoId);
    Imparticion imparticion =
        imparticionRepository
            .findById(dto.getImparticionId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Impartición no encontrada: " + dto.getImparticionId()));
    if (matriculaRepository.existsByAlumnoIdAndImparticionId(alumnoId, dto.getImparticionId())) {
      throw new IllegalArgumentException("El alumno ya está matriculado en esta impartición");
    }
    EstadoMatricula estado = dto.getEstado() != null ? dto.getEstado() : EstadoMatricula.ACTIVA;
    matriculaRepository.save(
        Matricula.builder()
            .alumno(alumno)
            .imparticion(imparticion)
            .centro(imparticion.getCentro())
            .estado(estado)
            .esRepetidor(Boolean.TRUE.equals(dto.getEsRepetidor()))
            .build());
  }

  @Transactional
  public void actualizarMatricula(Integer id, AdminMatriculaFormDTO dto) {
    Matricula matricula =
        matriculaRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ERR_MATRICULA + id));
    matricula.setEstado(dto.getEstado());
    matricula.setEsRepetidor(Boolean.TRUE.equals(dto.getEsRepetidor()));
    matriculaRepository.save(matricula);
  }

  @Transactional
  public void eliminarMatricula(Integer id) {
    if (!matriculaRepository.existsById(id)) {
      throw new EntityNotFoundException(ERR_MATRICULA + id);
    }
    if (calificacionRepository.existsByMatriculaId(id)) {
      throw new IllegalStateException(
          "No se puede eliminar la matrícula porque tiene calificaciones registradas");
    }
    matriculaRepository.deleteById(id);
  }
}
