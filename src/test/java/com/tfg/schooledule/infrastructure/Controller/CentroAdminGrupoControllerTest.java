package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.*;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CentroAdminGrupoController.class)
@Import(CentroAdminGrupoControllerTest.MethodSecurityTestConfig.class)
class CentroAdminGrupoControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private CentroAdminGrupoService centroAdminGrupoService;
  @MockBean private UsuarioRepository usuarioRepository;
  @MockBean private CentroRepository centroRepository;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private UsuarioService usuarioService;
  @MockBean private SecurityAuditLogger securityAuditLogger;
  @MockBean private AdminCursoActivoService adminCursoActivoService;

  private static final String USER = "admin@getafe.es";
  private static final int ADMIN_ID = 10;

  private void stubCurrentAdmin() {
    Usuario user =
        Usuario.builder().id(ADMIN_ID).email(USER).nombre("Admin").apellidos("Getafe").build();
    when(usuarioRepository.findUsuarioByEmail(USER)).thenReturn(Optional.of(user));
  }

  @Test
  void lista_redirigeLLogin_sinAutenticacion() throws Exception {
    mockMvc
        .perform(get("/centro-admin/grupos").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_403_conRolAlumno() throws Exception {
    mockMvc.perform(get("/centro-admin/grupos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = USER, roles = "ADMIN_CENTRO")
  void lista_ok() throws Exception {
    stubCurrentAdmin();
    when(centroAdminGrupoService.listarGruposDeCentros(ADMIN_ID)).thenReturn(List.of());

    mockMvc
        .perform(get("/centro-admin/grupos"))
        .andExpect(status().isOk())
        .andExpect(view().name("centro-admin/grupos/lista"));
  }

  @Test
  @WithMockUser(username = USER, roles = "ADMIN_CENTRO")
  void nuevo_ok_cargaFormulario() throws Exception {
    stubCurrentAdmin();
    when(centroAdminGrupoService.getCentrosDelAdmin(ADMIN_ID)).thenReturn(List.of());
    when(cursoAcademicoRepository.findAllByOrderByNombreAsc()).thenReturn(List.of());
    when(usuarioRepository.findAllProfesoresOrdenados()).thenReturn(List.of());

    mockMvc
        .perform(get("/centro-admin/grupos/nuevo"))
        .andExpect(status().isOk())
        .andExpect(view().name("centro-admin/grupos/formulario"));
  }

  @Test
  @WithMockUser(username = USER, roles = "ADMIN_CENTRO")
  void crear_ok_redirect() throws Exception {
    stubCurrentAdmin();
    doNothing().when(centroAdminGrupoService).crear(eq(ADMIN_ID), any());

    mockMvc
        .perform(
            post("/centro-admin/grupos/nuevo")
                .with(csrf())
                .param("nombre", "1DAW-A")
                .param("centroId", "1")
                .param("cursoAcademicoId", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/centro-admin/grupos"));
  }

  @Test
  @WithMockUser(username = USER, roles = "ADMIN_CENTRO")
  void editar_403_grupoDeOtroCentro() throws Exception {
    stubCurrentAdmin();
    doThrow(new AccessDeniedException("no"))
        .when(centroAdminGrupoService)
        .obtenerParaEditar(ADMIN_ID, 99);

    mockMvc.perform(get("/centro-admin/grupos/99/editar")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = USER, roles = "ADMIN_CENTRO")
  void eliminar_ok_redirect() throws Exception {
    stubCurrentAdmin();
    doNothing().when(centroAdminGrupoService).eliminar(ADMIN_ID, 5);

    mockMvc
        .perform(post("/centro-admin/grupos/5/eliminar").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/centro-admin/grupos"));
  }
}
