package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminUsuarioFormDTO;
import com.tfg.schooledule.domain.dto.DashboardStatsDTO;
import com.tfg.schooledule.domain.entity.Rol;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.mapper.AdminUsuarioMapper;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.repository.MatriculaRepository;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminUsuarioServiceTest {

  @Mock private UsuarioRepository usuarioRepository;
  @Mock private RolRepository rolRepository;
  @Mock private CentroRepository centroRepository;
  @Mock private CursoAcademicoRepository cursoAcademicoRepository;
  @Mock private AdminUsuarioMapper adminUsuarioMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private MatriculaRepository matriculaRepository;
  @Mock private ImparticionRepository imparticionRepository;

  @InjectMocks private AdminUsuarioService adminUsuarioService;

  private AdminUsuarioFormDTO buildForm(String username, String email, String password) {
    AdminUsuarioFormDTO form = new AdminUsuarioFormDTO();
    form.setUsername(username);
    form.setEmail(email);
    form.setPassword(password);
    form.setNombre("Test");
    form.setApellidos("Usuario");
    form.setRoleIds(Set.of(1));
    form.setCentroIds(Collections.emptySet());
    return form;
  }

  @Test
  void crear_usuarioValido_persisteConPasswordEncoded() {
    AdminUsuarioFormDTO form = buildForm("newuser", "new@tfg.com", "Password1");
    Rol rol = Rol.builder().id(1).nombre("ROLE_ALUMNO").build();

    when(usuarioRepository.existsByUsername("newuser")).thenReturn(false);
    when(usuarioRepository.existsByEmail("new@tfg.com")).thenReturn(false);
    when(rolRepository.findAllById(Set.of(1))).thenReturn(List.of(rol));
    when(centroRepository.findAllById(Collections.emptySet())).thenReturn(List.of());
    when(passwordEncoder.encode("Password1")).thenReturn("$2a$10$hash");

    adminUsuarioService.crear(form);

    verify(usuarioRepository)
        .save(
            argThat(
                u ->
                    "newuser".equals(u.getUsername())
                        && "$2a$10$hash".equals(u.getPasswordHash())
                        && u.getRoles().contains(rol)));
  }

  @Test
  void crear_passwordBlanca_lanzaIllegalArgumentException() {
    AdminUsuarioFormDTO form = buildForm("newuser", "new@tfg.com", "");

    assertThrows(IllegalArgumentException.class, () -> adminUsuarioService.crear(form));
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void crear_usernameExistente_lanzaIllegalArgumentException() {
    AdminUsuarioFormDTO form = buildForm("existente", "otro@tfg.com", "Password1");

    when(usuarioRepository.existsByUsername("existente")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminUsuarioService.crear(form));
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void crear_emailExistente_lanzaIllegalArgumentException() {
    AdminUsuarioFormDTO form = buildForm("nuevo", "existente@tfg.com", "Password1");

    when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
    when(usuarioRepository.existsByEmail("existente@tfg.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminUsuarioService.crear(form));
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void actualizar_passwordNulo_noReHashea() {
    AdminUsuarioFormDTO form = new AdminUsuarioFormDTO();
    form.setUsername("user1");
    form.setEmail("user1@tfg.com");
    form.setPassword(null);
    form.setNombre("User");
    form.setApellidos("One");
    form.setRoleIds(Collections.emptySet());
    form.setCentroIds(Collections.emptySet());

    Usuario existente =
        Usuario.builder()
            .id(1)
            .username("user1")
            .email("user1@tfg.com")
            .passwordHash("$2a$10$original")
            .build();

    when(usuarioRepository.findById(1)).thenReturn(Optional.of(existente));
    when(usuarioRepository.existsByUsernameAndIdNot("user1", 1)).thenReturn(false);
    when(usuarioRepository.existsByEmailAndIdNot("user1@tfg.com", 1)).thenReturn(false);
    when(rolRepository.findAllById(any())).thenReturn(List.of());
    when(centroRepository.findAllById(any())).thenReturn(List.of());

    adminUsuarioService.actualizar(1, form);

    verify(passwordEncoder, never()).encode(any());
    verify(usuarioRepository).save(argThat(u -> "$2a$10$original".equals(u.getPasswordHash())));
  }

  @Test
  void toggleActivo_cambiaEstadoAInverso() {
    Usuario usuario = Usuario.builder().id(1).activo(true).build();
    when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

    adminUsuarioService.toggleActivo(1);

    verify(usuarioRepository).save(argThat(u -> Boolean.FALSE.equals(u.getActivo())));
  }

  @Test
  void toggleActivo_usuarioInactivo_activaUsuario() {
    Usuario usuario = Usuario.builder().id(2).activo(false).build();
    when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario));

    adminUsuarioService.toggleActivo(2);

    verify(usuarioRepository).save(argThat(u -> Boolean.TRUE.equals(u.getActivo())));
  }

  @Test
  void actualizar_usernameExistente_lanzaIllegalArgumentException() {
    AdminUsuarioFormDTO form = buildForm("ocupado", "user@tfg.com", null);
    form.setId(1);

    Usuario existente = Usuario.builder().id(1).username("original").email("user@tfg.com").build();

    when(usuarioRepository.findById(1)).thenReturn(Optional.of(existente));
    when(usuarioRepository.existsByUsernameAndIdNot("ocupado", 1)).thenReturn(true);

    assertThatThrownBy(() -> adminUsuarioService.actualizar(1, form))
        .isInstanceOf(IllegalArgumentException.class);
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void getStats_retornaEstadisticasConNuevosCampos() {
    Usuario activo = Usuario.builder().activo(true).roles(Set.of()).build();
    Usuario inactivo = Usuario.builder().activo(false).roles(Set.of()).build();

    when(usuarioRepository.findAll()).thenReturn(List.of(activo, inactivo));
    when(centroRepository.count()).thenReturn(3L);
    when(cursoAcademicoRepository.findByActivo(true)).thenReturn(Optional.empty());
    when(matriculaRepository.countMatriculasActivas()).thenReturn(15L);
    when(imparticionRepository.count()).thenReturn(4L);

    DashboardStatsDTO stats = adminUsuarioService.getStats();

    assertEquals(2L, stats.totalUsuarios());
    assertEquals(1L, stats.activos());
    assertEquals(1L, stats.inactivos());
    assertEquals(3L, stats.totalCentros());
    assertEquals(15L, stats.totalMatriculasActivas());
    assertEquals(4L, stats.totalImparticiones());
  }
}
