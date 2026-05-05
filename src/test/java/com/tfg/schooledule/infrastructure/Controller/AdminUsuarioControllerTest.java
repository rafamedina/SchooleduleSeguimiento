package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.dto.AdminUsuarioFormDTO;
import com.tfg.schooledule.domain.dto.AdminUsuarioListDTO;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminUsuarioService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminUsuarioController.class)
@Import(AdminUsuarioControllerTest.MethodSecurityTestConfig.class)
class AdminUsuarioControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminUsuarioService adminUsuarioService;
  @MockBean private RolRepository rolRepository;
  @MockBean private CentroRepository centroRepository;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/usuarios").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/usuarios")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_200_retornaVista() throws Exception {
    when(adminUsuarioService.listarTodos()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/usuarios"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/lista"))
        .andExpect(model().attributeExists("usuarios"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_contieneListaUsuarios() throws Exception {
    AdminUsuarioListDTO dto =
        new AdminUsuarioListDTO(
            1, "admin", "Admin", "Test", "admin@tfg.com", true, Set.of(), Set.of());
    when(adminUsuarioService.listarTodos()).thenReturn(List.of(dto));

    mockMvc
        .perform(get("/admin/usuarios"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("usuarios", List.of(dto)));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void nuevo_conAdmin_200_retornaFormulario() throws Exception {
    when(rolRepository.findAll()).thenReturn(List.of());
    when(centroRepository.findAll()).thenReturn(List.of());

    mockMvc
        .perform(get("/admin/usuarios/nuevo"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/formulario"))
        .andExpect(model().attributeExists("form", "roles", "centros"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearUsuario_datosValidos_redirigeLista() throws Exception {
    when(rolRepository.findAll()).thenReturn(List.of());
    when(centroRepository.findAll()).thenReturn(List.of());

    mockMvc
        .perform(
            post("/admin/usuarios/nuevo")
                .param("username", "testuser")
                .param("nombre", "Test")
                .param("apellidos", "User")
                .param("email", "test@tfg.com")
                .param("password", "Password1")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/usuarios"));

    verify(adminUsuarioService).crear(any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearUsuario_passwordDebil_reRenderizaFormulario() throws Exception {
    when(rolRepository.findAll()).thenReturn(List.of());
    when(centroRepository.findAll()).thenReturn(List.of());

    mockMvc
        .perform(
            post("/admin/usuarios/nuevo")
                .param("username", "testuser")
                .param("nombre", "Test")
                .param("apellidos", "User")
                .param("email", "test@tfg.com")
                .param("password", "debil")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "password"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearUsuario_usernameInvalido_reRenderizaFormulario() throws Exception {
    when(rolRepository.findAll()).thenReturn(List.of());
    when(centroRepository.findAll()).thenReturn(List.of());

    mockMvc
        .perform(
            post("/admin/usuarios/nuevo")
                .param("username", "u")
                .param("nombre", "Test")
                .param("apellidos", "User")
                .param("email", "test@tfg.com")
                .param("password", "Password1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "username"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void toggleActivo_conAdmin_redirigeLista() throws Exception {
    mockMvc
        .perform(post("/admin/usuarios/1/toggle-activo").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/usuarios"));

    verify(adminUsuarioService).toggleActivo(1);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void editar_conAdmin_200_retornaFormulario() throws Exception {
    AdminUsuarioFormDTO dto = new AdminUsuarioFormDTO();
    dto.setId(1);
    dto.setUsername("admin");
    when(adminUsuarioService.obtenerParaEditar(1)).thenReturn(dto);
    when(rolRepository.findAll()).thenReturn(List.of());
    when(centroRepository.findAll()).thenReturn(List.of());

    mockMvc
        .perform(get("/admin/usuarios/1/editar"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/formulario"))
        .andExpect(model().attributeExists("form", "roles", "centros"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void actualizarUsuario_datosValidos_redirigeLista() throws Exception {
    when(rolRepository.findAll()).thenReturn(List.of());
    when(centroRepository.findAll()).thenReturn(List.of());

    mockMvc
        .perform(
            post("/admin/usuarios/1/editar")
                .param("username", "testuser")
                .param("nombre", "Test")
                .param("apellidos", "User")
                .param("email", "test@tfg.com")
                .param("password", "")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/usuarios"));

    verify(adminUsuarioService).actualizar(eq(1), any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void actualizarUsuario_usernameDuplicado_muestraError() throws Exception {
    when(rolRepository.findAll()).thenReturn(List.of());
    when(centroRepository.findAll()).thenReturn(List.of());
    doThrow(new IllegalArgumentException("Username ya existe"))
        .when(adminUsuarioService)
        .actualizar(eq(1), any());

    mockMvc
        .perform(
            post("/admin/usuarios/1/editar")
                .param("username", "testuser")
                .param("nombre", "Test")
                .param("apellidos", "User")
                .param("email", "test@tfg.com")
                .param("password", "")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/formulario"))
        .andExpect(model().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearUsuario_usernameDuplicado_muestraError() throws Exception {
    when(rolRepository.findAll()).thenReturn(List.of());
    when(centroRepository.findAll()).thenReturn(List.of());
    doThrow(new IllegalArgumentException("Username ya existe"))
        .when(adminUsuarioService)
        .crear(any());

    mockMvc
        .perform(
            post("/admin/usuarios/nuevo")
                .param("username", "testuser")
                .param("nombre", "Test")
                .param("apellidos", "User")
                .param("email", "test@tfg.com")
                .param("password", "Password1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/usuarios/formulario"))
        .andExpect(model().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void lista_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/usuarios")).andExpect(status().isForbidden());
  }
}
