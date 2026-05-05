package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminCentroService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminCentroController.class)
@Import(AdminCentroControllerTest.MethodSecurityTestConfig.class)
class AdminCentroControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminCentroService adminCentroService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/centros").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/centros")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void lista_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/centros")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_200_retornaVista() throws Exception {
    when(adminCentroService.listarTodos()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/centros"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/centros/lista"))
        .andExpect(model().attributeExists("centros"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearCentro_nombreBlanco_reRenderizaFormulario() throws Exception {
    mockMvc
        .perform(
            post("/admin/centros/nuevo")
                .param("nombre", "")
                .param("ubicacion", "Calle A")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/centros/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "nombre"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearCentro_datosValidos_redirigeLista() throws Exception {
    mockMvc
        .perform(
            post("/admin/centros/nuevo")
                .param("nombre", "IES Test")
                .param("ubicacion", "Calle B")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/centros"));

    verify(adminCentroService).crear(any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearCentro_nombreDuplicado_muestraError() throws Exception {
    doThrow(new IllegalArgumentException("Ya existe")).when(adminCentroService).crear(any());

    mockMvc
        .perform(
            post("/admin/centros/nuevo")
                .param("nombre", "IES Duplicado")
                .param("ubicacion", "Calle C")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/centros/formulario"))
        .andExpect(model().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void toggleActivo_conGrupos_redirigeLista() throws Exception {
    doThrow(new IllegalStateException("Tiene grupos")).when(adminCentroService).toggleActivo(1);

    mockMvc
        .perform(post("/admin/centros/1/toggle-activo").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/centros"));
  }
}
