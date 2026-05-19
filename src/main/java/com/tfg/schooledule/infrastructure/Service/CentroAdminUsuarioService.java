package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminUsuarioFormDTO;
import com.tfg.schooledule.domain.dto.AdminUsuarioListDTO;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroAdminUsuarioService {

  private static final Set<String> PROTECTED_ROLES = Set.of("ROLE_ADMIN", "ROLE_ADMIN_CENTRO");

  private final CentroAdminContextService context;
  private final AdminUsuarioService adminUsuarioService;
  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;

  public CentroAdminUsuarioService(
      CentroAdminContextService context,
      AdminUsuarioService adminUsuarioService,
      UsuarioRepository usuarioRepository,
      RolRepository rolRepository) {
    this.context = context;
    this.adminUsuarioService = adminUsuarioService;
    this.usuarioRepository = usuarioRepository;
    this.rolRepository = rolRepository;
  }

  @Transactional(readOnly = true)
  public List<AdminUsuarioListDTO> listarUsuariosDeCentros(Integer adminId) {
    Set<Integer> centroIds = context.getCentroIdsDelAdmin(adminId);
    return usuarioRepository.findAllByOrderByApellidosAscNombreAsc().stream()
        .filter(
            u ->
                u.getRoles().stream().noneMatch(r -> PROTECTED_ROLES.contains(r.getNombre()))
                    && u.getCentros().stream().anyMatch(c -> centroIds.contains(c.getId())))
        .map(
            u ->
                new AdminUsuarioListDTO(
                    u.getId(),
                    u.getUsername(),
                    u.getNombre(),
                    u.getApellidos(),
                    u.getEmail(),
                    Boolean.TRUE.equals(u.getActivo()),
                    u.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet()),
                    u.getCentros().stream().map(c -> c.getNombre()).collect(Collectors.toSet())))
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminUsuarioFormDTO obtenerParaEditar(Integer adminId, Integer usuarioId) {
    context.validateUsuarioGestionablePorCentroAdmin(adminId, usuarioId);
    return adminUsuarioService.obtenerParaEditar(usuarioId);
  }

  @Transactional
  public void crear(Integer adminId, AdminUsuarioFormDTO form) {
    validateNoProtectedRoles(form.getRoleIds());
    adminUsuarioService.crear(form);
  }

  @Transactional
  public void actualizar(Integer adminId, Integer usuarioId, AdminUsuarioFormDTO form) {
    context.validateUsuarioGestionablePorCentroAdmin(adminId, usuarioId);
    validateNoProtectedRoles(form.getRoleIds());
    adminUsuarioService.actualizar(usuarioId, form);
  }

  @Transactional
  public void eliminar(Integer adminId, Integer usuarioId) {
    context.validateUsuarioGestionablePorCentroAdmin(adminId, usuarioId);
    adminUsuarioService.toggleActivo(usuarioId);
  }

  private void validateNoProtectedRoles(Set<Integer> roleIds) {
    if (roleIds == null || roleIds.isEmpty()) return;
    List<Rol> roles = rolRepository.findAllById(roleIds);
    boolean hasProtected = roles.stream().anyMatch(r -> PROTECTED_ROLES.contains(r.getNombre()));
    if (hasProtected) {
      throw new AccessDeniedException("No puedes asignar roles de administrador");
    }
  }
}
