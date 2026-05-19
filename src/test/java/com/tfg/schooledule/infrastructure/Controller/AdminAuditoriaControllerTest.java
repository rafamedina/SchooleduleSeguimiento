package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminAuditoriaService;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import com.tfg.schooledule.infrastructure.service.AuditoriaExcelExportService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminAuditoriaController.class)
@Import(AdminAuditoriaControllerTest.MethodSecurityTestConfig.class)
class AdminAuditoriaControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminAuditoriaService adminAuditoriaService;
  @MockBean private AuditoriaExcelExportService auditoriaExcelExportService;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  @Test
  void lista_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/auditoria").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void lista_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/auditoria")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void lista_conProfesor_403() throws Exception {
    mockMvc.perform(get("/admin/auditoria")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_200_retornaVista() throws Exception {
    when(adminAuditoriaService.buscar(null, null, null, null)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/auditoria"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/auditoria/lista"))
        .andExpect(model().attributeExists("registros"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lista_conAdmin_conFiltros_pasaParametrosAlServicio() throws Exception {
    when(adminAuditoriaService.buscar("test@test.com", "Prog", null, null))
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            get("/admin/auditoria")
                .param("alumnoEmail", "test@test.com")
                .param("moduloNombre", "Prog"))
        .andExpect(status().isOk());

    verify(adminAuditoriaService).buscar("test@test.com", "Prog", null, null);
  }

  @Test
  void exportarExcel_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/auditoria/exportar").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void exportarExcel_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/auditoria/exportar")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void exportarExcel_conAdmin_200_retornaOctetStream() throws Exception {
    when(adminAuditoriaService.buscar(null, null, null, null)).thenReturn(Collections.emptyList());
    when(auditoriaExcelExportService.exportar(Collections.emptyList())).thenReturn(new byte[0]);

    mockMvc
        .perform(get("/admin/auditoria/exportar"))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    "Content-Type",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .andExpect(
            header()
                .string(
                    "Content-Disposition",
                    org.hamcrest.Matchers.startsWith("attachment; filename=auditoria_")));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void exportarExcel_conAdmin_conFiltros_pasaParametrosAlServicio() throws Exception {
    when(adminAuditoriaService.buscar("test@test.com", "Prog", null, null))
        .thenReturn(Collections.emptyList());
    when(auditoriaExcelExportService.exportar(Collections.emptyList())).thenReturn(new byte[0]);

    mockMvc
        .perform(
            get("/admin/auditoria/exportar")
                .param("alumnoEmail", "test@test.com")
                .param("moduloNombre", "Prog"))
        .andExpect(status().isOk());

    verify(adminAuditoriaService).buscar("test@test.com", "Prog", null, null);
  }
}
