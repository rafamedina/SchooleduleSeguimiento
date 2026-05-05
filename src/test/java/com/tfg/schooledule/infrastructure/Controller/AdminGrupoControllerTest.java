package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminGrupoService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminGrupoController.class)
@Import(AdminGrupoControllerTest.MethodSecurityTestConfig.class)
class AdminGrupoControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminGrupoService adminGrupoService;
  @MockBean private CentroRepository centroRepository;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/grupos").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/grupos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_200_retornaVista() throws Exception {
    when(adminGrupoService.listarTodos()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/grupos"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/grupos/lista"))
        .andExpect(model().attributeExists("grupos"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearGrupo_nombreBlanco_reRenderizaFormulario() throws Exception {
    when(centroRepository.findAllByOrderByNombreAsc()).thenReturn(Collections.emptyList());
    when(cursoAcademicoRepository.findAllByOrderByNombreAsc()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/admin/grupos/nuevo")
                .param("nombre", "")
                .param("centroId", "1")
                .param("cursoAcademicoId", "1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/grupos/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "nombre"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearGrupo_datosValidos_redirigeLista() throws Exception {
    mockMvc
        .perform(
            post("/admin/grupos/nuevo")
                .param("nombre", "1DAW-A")
                .param("centroId", "1")
                .param("cursoAcademicoId", "1")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/grupos"));

    verify(adminGrupoService).crear(any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearGrupo_nombreDuplicado_muestraError() throws Exception {
    doThrow(new IllegalArgumentException("Ya existe")).when(adminGrupoService).crear(any());
    when(centroRepository.findAllByOrderByNombreAsc()).thenReturn(Collections.emptyList());
    when(cursoAcademicoRepository.findAllByOrderByNombreAsc()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/admin/grupos/nuevo")
                .param("nombre", "1DAW-A")
                .param("centroId", "1")
                .param("cursoAcademicoId", "1")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/grupos/formulario"))
        .andExpect(model().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void eliminarGrupo_conImparticiones_redirigeLista() throws Exception {
    doThrow(new IllegalStateException("Tiene imparticiones")).when(adminGrupoService).eliminar(1);

    mockMvc
        .perform(post("/admin/grupos/1/eliminar").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/grupos"));
  }
}
