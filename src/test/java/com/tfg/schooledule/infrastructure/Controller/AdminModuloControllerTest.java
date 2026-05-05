package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminModuloService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminModuloController.class)
@Import(AdminModuloControllerTest.MethodSecurityTestConfig.class)
class AdminModuloControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminModuloService adminModuloService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/modulos").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/modulos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void lista_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/modulos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_200_retornaVista() throws Exception {
    when(adminModuloService.listarTodos()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/modulos"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/lista"))
        .andExpect(model().attributeExists("modulos"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearModulo_codigoBlanco_reRenderizaFormulario() throws Exception {
    mockMvc
        .perform(
            post("/admin/modulos/nuevo")
                .param("codigo", "")
                .param("nombre", "Desarrollo Web")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "codigo"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearModulo_codigoConCaracteresInvalidos_reRenderizaFormulario() throws Exception {
    mockMvc
        .perform(
            post("/admin/modulos/nuevo")
                .param("codigo", "<script>")
                .param("nombre", "Desarrollo Web")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "codigo"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearModulo_datosValidos_redirigeLista() throws Exception {
    mockMvc
        .perform(
            post("/admin/modulos/nuevo")
                .param("codigo", "DAW01")
                .param("nombre", "Desarrollo Web en Entorno Cliente")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos"));

    verify(adminModuloService).crear(any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearModulo_codigoDuplicado_muestraError() throws Exception {
    doThrow(new IllegalArgumentException("Ya existe")).when(adminModuloService).crear(any());

    mockMvc
        .perform(
            post("/admin/modulos/nuevo")
                .param("codigo", "DAW01")
                .param("nombre", "Duplicado")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/modulos/formulario"))
        .andExpect(model().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void toggleActivo_conImparticiones_redirigeLista() throws Exception {
    doThrow(new IllegalStateException("Tiene imparticiones"))
        .when(adminModuloService)
        .toggleActivo(1);

    mockMvc
        .perform(post("/admin/modulos/1/toggle-activo").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos"));
  }
}
