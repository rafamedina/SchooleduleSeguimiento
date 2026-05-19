package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.entity.Centro;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroAdminContextService {

  private static final Set<String> PROTECTED_ROLES = Set.of("ROLE_ADMIN", "ROLE_ADMIN_CENTRO");

  private final UsuarioRepository usuarioRepository;
  private final GrupoRepository grupoRepository;
  private final ImparticionRepository imparticionRepository;
  private final MatriculaRepository matriculaRepository;

  public CentroAdminContextService(
      UsuarioRepository usuarioRepository,
      GrupoRepository grupoRepository,
      ImparticionRepository imparticionRepository,
      MatriculaRepository matriculaRepository) {
    this.usuarioRepository = usuarioRepository;
    this.grupoRepository = grupoRepository;
    this.imparticionRepository = imparticionRepository;
    this.matriculaRepository = matriculaRepository;
  }

  @Transactional(readOnly = true)
  public List<Centro> getCentrosDelAdmin(Integer adminId) {
    return List.copyOf(loadAdmin(adminId).getCentros());
  }

  @Transactional(readOnly = true)
  public Set<Integer> getCentroIdsDelAdmin(Integer adminId) {
    return loadCentroIds(adminId);
  }

  private Set<Integer> loadCentroIds(Integer adminId) {
    return loadAdmin(adminId).getCentros().stream().map(Centro::getId).collect(Collectors.toSet());
  }

  @Transactional(readOnly = true)
  public void validateGrupoBelongsToCentroAdmin(Integer adminId, Integer grupoId) {
    Set<Integer> ids = loadCentroIds(adminId);
    if (!grupoRepository.existsByIdAndCentroIdIn(grupoId, ids)) {
      throw new AccessDeniedException("Grupo no pertenece a tus centros");
    }
  }

  @Transactional(readOnly = true)
  public void validateImparticionBelongsToCentroAdmin(Integer adminId, Integer imparticionId) {
    Set<Integer> ids = loadCentroIds(adminId);
    if (!imparticionRepository.existsByIdAndCentroIdIn(imparticionId, ids)) {
      throw new AccessDeniedException("Impartición no pertenece a tus centros");
    }
  }

  @Transactional(readOnly = true)
  public void validateMatriculaBelongsToCentroAdmin(Integer adminId, Integer matriculaId) {
    Set<Integer> ids = loadCentroIds(adminId);
    if (!matriculaRepository.existsByIdAndImparticionCentroIdIn(matriculaId, ids)) {
      throw new AccessDeniedException("Matrícula no pertenece a tus centros");
    }
  }

  @Transactional(readOnly = true)
  public void validateUsuarioGestionablePorCentroAdmin(Integer adminId, Integer usuarioId) {
    loadAdmin(adminId);
    Usuario objetivo =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + usuarioId));
    boolean tieneRolProtegido =
        objetivo.getRoles().stream().anyMatch(r -> PROTECTED_ROLES.contains(r.getNombre()));
    if (tieneRolProtegido) {
      throw new AccessDeniedException("No puedes gestionar usuarios con rol de administrador");
    }
  }

  private Usuario loadAdmin(Integer adminId) {
    return usuarioRepository
        .findById(adminId)
        .orElseThrow(() -> new EntityNotFoundException("AdminCentro no encontrado: " + adminId));
  }
}
