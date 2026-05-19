package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CentroAdminContextServiceTest {

  @Mock private UsuarioRepository usuarioRepository;
  @Mock private GrupoRepository grupoRepository;
  @Mock private ImparticionRepository imparticionRepository;
  @Mock private MatriculaRepository matriculaRepository;

  @InjectMocks private CentroAdminContextService service;

  private static final int ADMIN_ID = 10;
  private static final int CENTRO_A = 1;
  private static final int CENTRO_B = 2;

  private Centro centroA;
  private Centro centroB;
  private Usuario admin;

  @BeforeEach
  void setUp() {
    centroA = new Centro();
    centroA.setId(CENTRO_A);
    centroA.setNombre("IES Getafe");

    centroB = new Centro();
    centroB.setId(CENTRO_B);
    centroB.setNombre("IES Humanes");

    admin = new Usuario();
    admin.setId(ADMIN_ID);
    admin.setCentros(Set.of(centroA));
  }

  // --- getCentrosDelAdmin ---

  @Test
  void getCentrosDelAdmin_retornaCentrosAsignados() {
    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));

    List<Centro> result = service.getCentrosDelAdmin(ADMIN_ID);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(CENTRO_A);
  }

  @Test
  void getCentrosDelAdmin_lanzaSiAdminNoExiste() {
    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getCentrosDelAdmin(ADMIN_ID))
        .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
  }

  // --- getCentroIdsDelAdmin ---

  @Test
  void getCentroIdsDelAdmin_retornaSetDeIds() {
    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));

    Set<Integer> ids = service.getCentroIdsDelAdmin(ADMIN_ID);

    assertThat(ids).containsExactly(CENTRO_A);
  }

  // --- validateGrupoBelongsToCentroAdmin ---

  @Test
  void validateGrupo_noLanzaSiGrupoPertenece() {
    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    when(grupoRepository.existsByIdAndCentroIdIn(eq(5), anyCollection())).thenReturn(true);

    service.validateGrupoBelongsToCentroAdmin(ADMIN_ID, 5);
    // no exception
  }

  @Test
  void validateGrupo_lanzaAccessDeniedSiGrupoEsDeOtroCentro() {
    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    when(grupoRepository.existsByIdAndCentroIdIn(eq(99), anyCollection())).thenReturn(false);

    assertThatThrownBy(() -> service.validateGrupoBelongsToCentroAdmin(ADMIN_ID, 99))
        .isInstanceOf(AccessDeniedException.class);
  }

  // --- validateImparticionBelongsToCentroAdmin ---

  @Test
  void validateImparticion_noLanzaSiPertenece() {
    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    when(imparticionRepository.existsByIdAndCentroIdIn(eq(7), anyCollection())).thenReturn(true);

    service.validateImparticionBelongsToCentroAdmin(ADMIN_ID, 7);
  }

  @Test
  void validateImparticion_lanzaAccessDeniedSiEsDeOtroCentro() {
    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    when(imparticionRepository.existsByIdAndCentroIdIn(eq(99), anyCollection())).thenReturn(false);

    assertThatThrownBy(() -> service.validateImparticionBelongsToCentroAdmin(ADMIN_ID, 99))
        .isInstanceOf(AccessDeniedException.class);
  }

  // --- validateMatriculaBelongsToCentroAdmin ---

  @Test
  void validateMatricula_noLanzaSiPertenece() {
    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    when(matriculaRepository.existsByIdAndImparticionCentroIdIn(eq(3), anyCollection()))
        .thenReturn(true);

    service.validateMatriculaBelongsToCentroAdmin(ADMIN_ID, 3);
  }

  @Test
  void validateMatricula_lanzaAccessDeniedSiEsDeOtroCentro() {
    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    when(matriculaRepository.existsByIdAndImparticionCentroIdIn(eq(99), anyCollection()))
        .thenReturn(false);

    assertThatThrownBy(() -> service.validateMatriculaBelongsToCentroAdmin(ADMIN_ID, 99))
        .isInstanceOf(AccessDeniedException.class);
  }

  // --- validateUsuarioGestionable ---

  @Test
  void validateUsuario_noLanzaSiUsuarioTieneRolGestionable() {
    Rol rolProfesor = new Rol();
    rolProfesor.setNombre("ROLE_PROFESOR");

    Usuario objetivo = new Usuario();
    objetivo.setId(20);
    objetivo.setRoles(Set.of(rolProfesor));

    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    when(usuarioRepository.findById(20)).thenReturn(Optional.of(objetivo));

    service.validateUsuarioGestionablePorCentroAdmin(ADMIN_ID, 20);
  }

  @Test
  void validateUsuario_lanzaSiObjetivoTieneRolAdmin() {
    Rol rolAdmin = new Rol();
    rolAdmin.setNombre("ROLE_ADMIN");

    Usuario objetivo = new Usuario();
    objetivo.setId(21);
    objetivo.setRoles(Set.of(rolAdmin));

    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    when(usuarioRepository.findById(21)).thenReturn(Optional.of(objetivo));

    assertThatThrownBy(() -> service.validateUsuarioGestionablePorCentroAdmin(ADMIN_ID, 21))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void validateUsuario_lanzaSiObjetivoTieneRolAdminCentro() {
    Rol rolAdminCentro = new Rol();
    rolAdminCentro.setNombre("ROLE_ADMIN_CENTRO");

    Usuario objetivo = new Usuario();
    objetivo.setId(22);
    objetivo.setRoles(Set.of(rolAdminCentro));

    when(usuarioRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
    when(usuarioRepository.findById(22)).thenReturn(Optional.of(objetivo));

    assertThatThrownBy(() -> service.validateUsuarioGestionablePorCentroAdmin(ADMIN_ID, 22))
        .isInstanceOf(AccessDeniedException.class);
  }
}
