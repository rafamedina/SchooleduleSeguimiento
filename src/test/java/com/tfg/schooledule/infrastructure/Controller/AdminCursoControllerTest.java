package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.dto.AdminCursoFormDTO;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import com.tfg.schooledule.infrastructure.service.AdminCursoService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminCursoController.class)
@Import(AdminCursoControllerTest.MethodSecurityTestConfig.class)
class AdminCursoControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminCursoService adminCursoService;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/cursos").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/cursos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void lista_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/cursos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_200_retornaVista() throws Exception {
    when(adminCursoService.listarTodos()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/cursos"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/cursos/lista"))
        .andExpect(model().attributeExists("cursos"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearCurso_nombreBlanco_reRenderizaFormulario() throws Exception {
    mockMvc
        .perform(
            post("/admin/cursos/nuevo")
                .param("nombre", "")
                .param("fechaInicio", "2024-09-01")
                .param("fechaFin", "2025-06-30")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/cursos/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "nombre"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearCurso_fechaFinAnteriorAInicio_reRenderizaFormulario() throws Exception {
    mockMvc
        .perform(
            post("/admin/cursos/nuevo")
                .param("nombre", "2024/2025")
                .param("fechaInicio", "2025-06-30")
                .param("fechaFin", "2024-09-01")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/cursos/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "fechaFinValida"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearCurso_datosValidos_redirigeLista() throws Exception {
    mockMvc
        .perform(
            post("/admin/cursos/nuevo")
                .param("nombre", "2024/2025")
                .param("fechaInicio", "2024-09-01")
                .param("fechaFin", "2025-06-30")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/cursos"));

    verify(adminCursoService).crear(any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void activarCurso_redirigeLista() throws Exception {
    mockMvc
        .perform(post("/admin/cursos/1/activar").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/cursos"));

    verify(adminCursoService).activar(1);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void cerrarCurso_redirigeLista() throws Exception {
    mockMvc
        .perform(post("/admin/cursos/1/cerrar").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/cursos"));

    verify(adminCursoService).cerrar(1);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void nuevo_conAdmin_200_retornaFormulario() throws Exception {
    mockMvc
        .perform(get("/admin/cursos/nuevo"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/cursos/formulario"))
        .andExpect(model().attributeExists("form"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void editar_conAdmin_200_retornaFormularioPreRellenado() throws Exception {
    when(adminCursoService.obtenerParaEditar(1)).thenReturn(new AdminCursoFormDTO());

    mockMvc
        .perform(get("/admin/cursos/1/editar"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/cursos/formulario"))
        .andExpect(model().attributeExists("form"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void actualizar_datosValidos_redirigeLista() throws Exception {
    mockMvc
        .perform(
            post("/admin/cursos/1/editar")
                .param("nombre", "2025/2026")
                .param("fechaInicio", "2025-09-01")
                .param("fechaFin", "2026-06-30")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/cursos"));

    verify(adminCursoService).actualizar(eq(1), any());
  }
}
