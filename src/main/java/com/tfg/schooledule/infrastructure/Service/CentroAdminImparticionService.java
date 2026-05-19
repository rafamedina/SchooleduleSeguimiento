package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminImparticionFormDTO;
import com.tfg.schooledule.domain.dto.AdminImparticionListDTO;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroAdminImparticionService {

  private final CentroAdminContextService context;
  private final AdminImparticionService adminImparticionService;
  private final ImparticionRepository imparticionRepository;
  private final GrupoRepository grupoRepository;

  public CentroAdminImparticionService(
      CentroAdminContextService context,
      AdminImparticionService adminImparticionService,
      ImparticionRepository imparticionRepository,
      GrupoRepository grupoRepository) {
    this.context = context;
    this.adminImparticionService = adminImparticionService;
    this.imparticionRepository = imparticionRepository;
    this.grupoRepository = grupoRepository;
  }

  @Transactional(readOnly = true)
  public List<AdminImparticionListDTO> listarImparticionesDeCentros(Integer adminId) {
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);
    return imparticionRepository
        .findByCentroIdInOrderByGrupoNombreAscModuloNombreAsc(centroIds)
        .stream()
        .map(
            i ->
                new AdminImparticionListDTO(
                    i.getId(),
                    i.getModulo().getCodigo(),
                    i.getModulo().getNombre(),
                    i.getGrupo().getNombre(),
                    i.getCentro().getNombre(),
                    i.getProfesor().getApellidos() + ", " + i.getProfesor().getNombre()))
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminImparticionFormDTO obtenerParaEditar(Integer adminId, Integer imparticionId) {
    context.validateImparticionBelongsToCentroAdmin(adminId, imparticionId);
    return adminImparticionService.obtenerParaEditar(imparticionId);
  }

  @Transactional
  public void crear(Integer adminId, AdminImparticionFormDTO form) {
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);
    if (form.getGrupoId() == null
        || !grupoRepository.existsByIdAndCentroIdIn(form.getGrupoId(), centroIds)) {
      throw new AccessDeniedException("El grupo no pertenece a tus centros");
    }
    adminImparticionService.crear(form);
  }

  @Transactional
  public void actualizar(Integer adminId, Integer imparticionId, AdminImparticionFormDTO form) {
    context.validateImparticionBelongsToCentroAdmin(adminId, imparticionId);
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);
    if (form.getGrupoId() == null
        || !grupoRepository.existsByIdAndCentroIdIn(form.getGrupoId(), centroIds)) {
      throw new AccessDeniedException("El grupo no pertenece a tus centros");
    }
    adminImparticionService.actualizar(imparticionId, form);
  }

  @Transactional
  public void eliminar(Integer adminId, Integer imparticionId) {
    context.validateImparticionBelongsToCentroAdmin(adminId, imparticionId);
    adminImparticionService.eliminar(imparticionId);
  }
}
