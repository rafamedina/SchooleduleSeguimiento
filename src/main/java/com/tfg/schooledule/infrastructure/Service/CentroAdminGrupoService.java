package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminGrupoFormDTO;
import com.tfg.schooledule.domain.dto.AdminGrupoListDTO;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroAdminGrupoService {

  private final CentroAdminContextService context;
  private final AdminGrupoService adminGrupoService;
  private final GrupoRepository grupoRepository;
  private final ImparticionRepository imparticionRepository;

  public CentroAdminGrupoService(
      CentroAdminContextService context,
      AdminGrupoService adminGrupoService,
      GrupoRepository grupoRepository,
      ImparticionRepository imparticionRepository) {
    this.context = context;
    this.adminGrupoService = adminGrupoService;
    this.grupoRepository = grupoRepository;
    this.imparticionRepository = imparticionRepository;
  }

  @Transactional(readOnly = true)
  public List<com.tfg.schooledule.domain.entity.Centro> getCentrosDelAdmin(Integer adminId) {
    return context.getCentrosDelAdmin(adminId);
  }

  @Transactional(readOnly = true)
  public List<AdminGrupoListDTO> listarGruposDeCentros(Integer adminId) {
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);
    return grupoRepository.findByCentroIdInOrderByCentroNombreAscNombreAsc(centroIds).stream()
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
  public AdminGrupoFormDTO obtenerParaEditar(Integer adminId, Integer grupoId) {
    context.validateGrupoBelongsToCentroAdmin(adminId, grupoId);
    return adminGrupoService.obtenerParaEditar(grupoId);
  }

  @Transactional
  public void crear(Integer adminId, AdminGrupoFormDTO form) {
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);
    if (form.getCentroId() == null || !centroIds.contains(form.getCentroId())) {
      throw new AccessDeniedException("Solo puedes crear grupos en tus centros asignados");
    }
    adminGrupoService.crear(form);
  }

  @Transactional
  public void actualizar(Integer adminId, Integer grupoId, AdminGrupoFormDTO form) {
    context.validateGrupoBelongsToCentroAdmin(adminId, grupoId);
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);
    if (form.getCentroId() == null || !centroIds.contains(form.getCentroId())) {
      throw new AccessDeniedException("Solo puedes asignar grupos a tus centros");
    }
    adminGrupoService.actualizar(grupoId, form);
  }

  @Transactional
  public void eliminar(Integer adminId, Integer grupoId) {
    context.validateGrupoBelongsToCentroAdmin(adminId, grupoId);
    adminGrupoService.eliminar(grupoId);
  }
}
