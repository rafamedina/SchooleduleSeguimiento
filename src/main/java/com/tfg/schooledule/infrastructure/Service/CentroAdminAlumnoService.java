package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminAlumnoListDTO;
import com.tfg.schooledule.domain.dto.AdminMatriculaFormDTO;
import com.tfg.schooledule.domain.dto.AdminMatriculaListDTO;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroAdminAlumnoService {

  private final CentroAdminContextService context;
  private final AdminAlumnoService adminAlumnoService;
  private final MatriculaRepository matriculaRepository;
  private final ImparticionRepository imparticionRepository;

  public CentroAdminAlumnoService(
      CentroAdminContextService context,
      AdminAlumnoService adminAlumnoService,
      MatriculaRepository matriculaRepository,
      ImparticionRepository imparticionRepository) {
    this.context = context;
    this.adminAlumnoService = adminAlumnoService;
    this.matriculaRepository = matriculaRepository;
    this.imparticionRepository = imparticionRepository;
  }

  @Transactional(readOnly = true)
  public List<AdminAlumnoListDTO> listarAlumnosDeCentros(Integer adminId) {
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);
    return matriculaRepository.findByCentroIds(centroIds).stream()
        .map(m -> m.getAlumno())
        .distinct()
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
  public List<AdminMatriculaListDTO> listarMatriculas(Integer adminId, Integer alumnoId) {
    context.validateUsuarioGestionablePorCentroAdmin(adminId, alumnoId);
    return adminAlumnoService.listarMatriculas(alumnoId);
  }

  @Transactional
  public void crearMatricula(Integer adminId, Integer alumnoId, AdminMatriculaFormDTO form) {
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);
    if (form.getImparticionId() == null
        || !imparticionRepository.existsByIdAndCentroIdIn(form.getImparticionId(), centroIds)) {
      throw new AccessDeniedException("La impartición no pertenece a tus centros");
    }
    adminAlumnoService.crearMatricula(alumnoId, form);
  }

  @Transactional
  public void actualizarMatricula(
      Integer adminId, Integer matriculaId, AdminMatriculaFormDTO form) {
    context.validateMatriculaBelongsToCentroAdmin(adminId, matriculaId);
    adminAlumnoService.actualizarMatricula(matriculaId, form);
  }

  @Transactional
  public void eliminarMatricula(Integer adminId, Integer matriculaId) {
    context.validateMatriculaBelongsToCentroAdmin(adminId, matriculaId);
    adminAlumnoService.eliminarMatricula(matriculaId);
  }

  @Transactional(readOnly = true)
  public Usuario obtenerAlumno(Integer adminId, Integer alumnoId) {
    context.validateUsuarioGestionablePorCentroAdmin(adminId, alumnoId);
    return adminAlumnoService.obtenerAlumno(alumnoId);
  }

  @Transactional(readOnly = true)
  public AdminMatriculaFormDTO obtenerMatriculaParaEditar(Integer adminId, Integer matriculaId) {
    context.validateMatriculaBelongsToCentroAdmin(adminId, matriculaId);
    return adminAlumnoService.obtenerMatriculaParaEditar(matriculaId);
  }
}
