package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminUsuarioFormDTO;
import com.tfg.schooledule.domain.dto.AdminUsuarioListDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.*;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CentroAdminUsuarioServiceTest {

  @Mock private CentroAdminContextService context;
  @Mock private AdminUsuarioService adminUsuarioService;
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private RolRepository rolRepository;

  @InjectMocks private CentroAdminUsuarioService service;

  private static final int ADMIN_ID = 10;
  private static final int CENTRO_A = 1;

  private Centro centroA;
  private Usuario admin;
  private Usuario profesor;
  private Rol rolProfesor;
  private Rol rolAdmin;
  private Rol rolAdminCentro;

  @BeforeEach
  void setUp() {
    centroA = new Centro();
    centroA.setId(CENTRO_A);

    rolProfesor = new Rol();
    rolProfesor.setId(2);
    rolProfesor.setNombre("ROLE_PROFESOR");

    rolAdmin = new Rol();
    rolAdmin.setId(1);
    rolAdmin.setNombre("ROLE_ADMIN");

    rolAdminCentro = new Rol();
    rolAdminCentro.setId(4);
    rolAdminCentro.setNombre("ROLE_ADMIN_CENTRO");

    admin = new Usuario();
    admin.setId(ADMIN_ID);
    admin.setCentros(Set.of(centroA));

    profesor = new Usuario();
    profesor.setId(30);
    profesor.setNombre("Luis");
    profesor.setApellidos("Pérez");
    profesor.setEmail("luis@ies.es");
    profesor.setActivo(true);
    profesor.setRoles(Set.of(rolProfesor));
    profesor.setCentros(Set.of(centroA));
  }

  @Test
  void listarUsuarios_retornaProfesorYAlumnosDelCentro() {
    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(usuarioRepository.findAllByOrderByApellidosAscNombreAsc()).thenReturn(List.of(profesor));

    List<AdminUsuarioListDTO> result = service.listarUsuariosDeCentros(ADMIN_ID);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).nombre()).isEqualTo("Luis");
  }

  @Test
  void listarUsuarios_excluyeAdmins() {
    Usuario adminUsuario = new Usuario();
    adminUsuario.setId(1);
    adminUsuario.setNombre("Admin");
    adminUsuario.setApellidos("Global");
    adminUsuario.setEmail("admin@ies.es");
    adminUsuario.setActivo(true);
    adminUsuario.setRoles(Set.of(rolAdmin));
    adminUsuario.setCentros(Set.of(centroA));

    when(context.getCentroIdsDelAdmin(ADMIN_ID)).thenReturn(Set.of(CENTRO_A));
    when(usuarioRepository.findAllByOrderByApellidosAscNombreAsc())
        .thenReturn(List.of(profesor, adminUsuario));

    List<AdminUsuarioListDTO> result = service.listarUsuariosDeCentros(ADMIN_ID);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).nombre()).isEqualTo("Luis");
  }

  @Test
  void obtenerParaEditar_ok_siUsuarioEsGestionable() {
    doNothing().when(context).validateUsuarioGestionablePorCentroAdmin(ADMIN_ID, 30);
    var dto = new AdminUsuarioFormDTO();
    when(adminUsuarioService.obtenerParaEditar(30)).thenReturn(dto);

    AdminUsuarioFormDTO result = service.obtenerParaEditar(ADMIN_ID, 30);

    assertThat(result).isSameAs(dto);
  }

  @Test
  void obtenerParaEditar_lanza_siUsuarioEsAdmin() {
    doThrow(new AccessDeniedException("no"))
        .when(context)
        .validateUsuarioGestionablePorCentroAdmin(ADMIN_ID, 1);

    assertThatThrownBy(() -> service.obtenerParaEditar(ADMIN_ID, 1))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void crear_ok_conRolProfesor() {
    when(rolRepository.findAllById(anyCollection())).thenReturn(List.of(rolProfesor));
    AdminUsuarioFormDTO form = new AdminUsuarioFormDTO();
    form.setRoleIds(Set.of(2));
    doNothing().when(adminUsuarioService).crear(form);

    service.crear(ADMIN_ID, form);

    verify(adminUsuarioService).crear(form);
  }

  @Test
  void crear_lanza_siIncluyeRolAdmin() {
    when(rolRepository.findAllById(anyCollection())).thenReturn(List.of(rolAdmin));
    AdminUsuarioFormDTO form = new AdminUsuarioFormDTO();
    form.setRoleIds(Set.of(1));

    assertThatThrownBy(() -> service.crear(ADMIN_ID, form))
        .isInstanceOf(AccessDeniedException.class);
    verify(adminUsuarioService, never()).crear(any());
  }

  @Test
  void crear_lanza_siIncluyeRolAdminCentro() {
    when(rolRepository.findAllById(anyCollection())).thenReturn(List.of(rolAdminCentro));
    AdminUsuarioFormDTO form = new AdminUsuarioFormDTO();
    form.setRoleIds(Set.of(4));

    assertThatThrownBy(() -> service.crear(ADMIN_ID, form))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void eliminar_ok_siUsuarioEsGestionable() {
    doNothing().when(context).validateUsuarioGestionablePorCentroAdmin(ADMIN_ID, 30);
    doNothing().when(adminUsuarioService).toggleActivo(30);

    service.eliminar(ADMIN_ID, 30);

    verify(adminUsuarioService).toggleActivo(30);
  }

  @Test
  void eliminar_lanza_siUsuarioEsAdmin() {
    doThrow(new AccessDeniedException("no"))
        .when(context)
        .validateUsuarioGestionablePorCentroAdmin(ADMIN_ID, 1);

    assertThatThrownBy(() -> service.eliminar(ADMIN_ID, 1))
        .isInstanceOf(AccessDeniedException.class);
  }
}
