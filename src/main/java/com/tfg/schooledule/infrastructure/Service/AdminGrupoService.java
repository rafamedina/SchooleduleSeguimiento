package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminGrupoFormDTO;
import com.tfg.schooledule.domain.dto.AdminGrupoListDTO;
import com.tfg.schooledule.domain.dto.GrupoFiltroDTO;
import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.CursoAcademico;
import com.tfg.schooledule.domain.entity.Grupo;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.mapper.AdminGrupoMapper;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminGrupoService {

  private static final String ERR_GRUPO = "Grupo no encontrado: ";

  private final GrupoRepository grupoRepository;
  private final ImparticionRepository imparticionRepository;
  private final CentroRepository centroRepository;
  private final CursoAcademicoRepository cursoAcademicoRepository;
  private final UsuarioRepository usuarioRepository;
  private final AdminGrupoMapper adminGrupoMapper;
  private final AdminCursoActivoService cursoActivoService;

  public AdminGrupoService(
      GrupoRepository grupoRepository,
      ImparticionRepository imparticionRepository,
      CentroRepository centroRepository,
      CursoAcademicoRepository cursoAcademicoRepository,
      UsuarioRepository usuarioRepository,
      AdminGrupoMapper adminGrupoMapper,
      AdminCursoActivoService cursoActivoService) {
    this.grupoRepository = grupoRepository;
    this.imparticionRepository = imparticionRepository;
    this.centroRepository = centroRepository;
    this.cursoAcademicoRepository = cursoAcademicoRepository;
    this.usuarioRepository = usuarioRepository;
    this.adminGrupoMapper = adminGrupoMapper;
    this.cursoActivoService = cursoActivoService;
  }

  @Transactional(readOnly = true)
  public List<AdminGrupoListDTO> listarFiltrado(GrupoFiltroDTO filtro) {
    Integer cursoId =
        filtro.cursoAcademicoId() != null
            ? filtro.cursoAcademicoId()
            : cursoActivoService.getCursoActivoId();
    return grupoRepository.findByFiltro(filtro.centroId(), cursoId).stream()
        .map(this::toListDTO)
        .toList();
  }

  private AdminGrupoListDTO toListDTO(Grupo g) {
    String tutorNombre =
        g.getTutor() != null ? g.getTutor().getNombre() + " " + g.getTutor().getApellidos() : null;
    return new AdminGrupoListDTO(
        g.getId(),
        g.getNombre(),
        g.getCentro().getNombre(),
        g.getCursoAcademico().getNombre(),
        imparticionRepository.countByGrupoId(g.getId()),
        tutorNombre);
  }

  @Transactional(readOnly = true)
  public List<AdminGrupoListDTO> listarTodos() {
    return grupoRepository.findAllByOrderByCentroNombreAscNombreAsc().stream()
        .map(
            g -> {
              String tutorNombre =
                  g.getTutor() != null
                      ? g.getTutor().getNombre() + " " + g.getTutor().getApellidos()
                      : null;
              return new AdminGrupoListDTO(
                  g.getId(),
                  g.getNombre(),
                  g.getCentro().getNombre(),
                  g.getCursoAcademico().getNombre(),
                  imparticionRepository.countByGrupoId(g.getId()),
                  tutorNombre);
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminGrupoFormDTO obtenerParaEditar(Integer id) {
    Grupo grupo =
        grupoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(ERR_GRUPO + id));
    return adminGrupoMapper.toFormDTO(grupo);
  }

  @Transactional
  public void crear(AdminGrupoFormDTO dto) {
    Centro centro =
        centroRepository
            .findById(dto.getCentroId())
            .orElseThrow(
                () -> new EntityNotFoundException("Centro no encontrado: " + dto.getCentroId()));
    CursoAcademico curso =
        cursoAcademicoRepository
            .findById(dto.getCursoAcademicoId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Curso académico no encontrado: " + dto.getCursoAcademicoId()));
    if (grupoRepository.existsByNombreAndCentroIdAndCursoAcademicoId(
        dto.getNombre(), dto.getCentroId(), dto.getCursoAcademicoId())) {
      throw new IllegalArgumentException(
          "Ya existe un grupo con ese nombre en este centro y curso académico");
    }
    Usuario tutor = resolveTutor(dto.getTutorId());
    grupoRepository.save(
        Grupo.builder()
            .nombre(dto.getNombre())
            .centro(centro)
            .cursoAcademico(curso)
            .tutor(tutor)
            .build());
  }

  @Transactional
  public void actualizar(Integer id, AdminGrupoFormDTO dto) {
    Grupo grupo =
        grupoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(ERR_GRUPO + id));
    Centro centro =
        centroRepository
            .findById(dto.getCentroId())
            .orElseThrow(
                () -> new EntityNotFoundException("Centro no encontrado: " + dto.getCentroId()));
    CursoAcademico curso =
        cursoAcademicoRepository
            .findById(dto.getCursoAcademicoId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Curso académico no encontrado: " + dto.getCursoAcademicoId()));
    if (grupoRepository.existsByNombreAndCentroIdAndCursoAcademicoIdAndIdNot(
        dto.getNombre(), dto.getCentroId(), dto.getCursoAcademicoId(), id)) {
      throw new IllegalArgumentException(
          "Ya existe un grupo con ese nombre en este centro y curso académico");
    }
    grupo.setNombre(dto.getNombre());
    grupo.setCentro(centro);
    grupo.setCursoAcademico(curso);
    grupo.setTutor(resolveTutor(dto.getTutorId()));
    grupoRepository.save(grupo);
  }

  private Usuario resolveTutor(Integer tutorId) {
    if (tutorId == null) return null;
    return usuarioRepository
        .findById(tutorId)
        .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado: " + tutorId));
  }

  @Transactional
  public void eliminar(Integer id) {
    if (!grupoRepository.existsById(id)) {
      throw new EntityNotFoundException(ERR_GRUPO + id);
    }
    if (imparticionRepository.existsByGrupoId(id)) {
      throw new IllegalStateException(
          "No se puede eliminar el grupo porque tiene imparticiones asociadas");
    }
    grupoRepository.deleteById(id);
  }
}
